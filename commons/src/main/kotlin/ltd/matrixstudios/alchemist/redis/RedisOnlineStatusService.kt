package ltd.matrixstudios.alchemist.redis

import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.logging.Logger

// TODO fix last online status service so that it uses redis instead of mongo
object RedisOnlineStatusService {

    private val logger = Logger.getLogger("RedisOnlineStatusService")

    private fun getKey(uuid: UUID): String = "Alchemist:online:$uuid"


    fun markOnline(uuid: UUID, server: String) {
        try {
            RedisPacketManager.executeWithRetry(RedisConfig.maxRetries) { jedis ->
                jedis.setex(getKey(uuid), RedisConfig.onlineStatusTTL.toLong(), server)
            }
        } catch (e: Exception) {
            logger.warning("Failed to mark player $uuid as online on server $server: ${e.message}")
        }
    }

    fun markOffline(uuid: UUID) {
        try {
            RedisPacketManager.executeWithRetry(RedisConfig.maxRetries) { jedis ->
                jedis.del(getKey(uuid))
            }
        } catch (e: Exception) {
            logger.warning("Failed to mark player $uuid as offline: ${e.message}")
        }
    }

    fun isOnline(uuid: UUID): Boolean {
        return try {
            RedisPacketManager.executeWithRetryAndReturn(RedisConfig.maxRetries) { jedis ->
                jedis.exists(getKey(uuid))
            }
        } catch (e: Exception) {
            logger.warning("Failed to check online status for player $uuid: ${e.message}")
            // Default to offline if Redis is unavailable
            false
        }
    }

    fun updateOnlineServer(uuid: UUID, server: String) {
        try {
            RedisPacketManager.executeWithRetry(RedisConfig.maxRetries) { jedis ->
                jedis.setex(getKey(uuid), RedisConfig.onlineStatusTTL.toLong(), server)
            }
        } catch (e: Exception) {
            logger.warning("Failed to update online server for player $uuid to $server: ${e.message}")
        }
    }

    fun getOnlineServer(uuid: UUID): String? {
        return try {
            RedisPacketManager.executeWithRetryAndReturn(RedisConfig.maxRetries) { jedis ->
                jedis.get(getKey(uuid))
            }
        } catch (e: Exception) {
            logger.warning("Failed to get online server for player $uuid: ${e.message}")
            null
        }
    }


    fun markOnlineAsync(uuid: UUID, server: String): CompletableFuture<Void> {
        return CompletableFuture.runAsync {
            markOnline(uuid, server)
        }
    }

    fun markOfflineAsync(uuid: UUID): CompletableFuture<Void> {
        return CompletableFuture.runAsync {
            markOffline(uuid)
        }
    }

    fun isOnlineAsync(uuid: UUID): CompletableFuture<Boolean> {
        return CompletableFuture.supplyAsync {
            isOnline(uuid)
        }
    }

    fun markMultipleOnline(players: Map<UUID, String>) {
        try {
            RedisPacketManager.executeWithRetry(RedisConfig.maxRetries) { jedis ->
                val pipeline = jedis.pipelined()
                players.forEach { (uuid, server) ->
                    pipeline.setex(getKey(uuid), RedisConfig.onlineStatusTTL.toLong(), server)
                }
                pipeline.sync()
            }
        } catch (e: Exception) {
            logger.warning("Failed to mark multiple players as online: ${e.message}")
            // Fallback to individual operations
            players.forEach { (uuid, server) ->
                markOnline(uuid, server)
            }
        }
    }


    fun getOnlinePlayersCount(): Int {
        return try {
            RedisPacketManager.executeWithRetryAndReturn(RedisConfig.maxRetries) { jedis ->
                jedis.keys("Alchemist:online:*").size
            }
        } catch (e: Exception) {
            logger.warning("Failed to get online players count: ${e.message}")
            0
        }
    }


    fun getOnlinePlayers(): Set<UUID> {
        return try {
            RedisPacketManager.executeWithRetryAndReturn(RedisConfig.maxRetries) { jedis ->
                jedis.keys("Alchemist:online:*").mapNotNull { key ->
                    try {
                        UUID.fromString(key.substringAfter("Alchemist:online:"))
                    } catch (_: IllegalArgumentException) {
                        null
                    }
                }.toSet()
            }
        } catch (e: Exception) {
            logger.warning("Failed to get online players: ${e.message}")
            emptySet()
        }
    }


    fun cleanupExpiredKeys() {
        try {
            RedisPacketManager.executeWithRetry(RedisConfig.maxRetries) { jedis ->
                val keys = jedis.keys("Alchemist:online:*")
                keys.forEach { key ->
                    if (!jedis.exists(key)) {
                        jedis.del(key)
                    }
                }
            }
        } catch (e: Exception) {
            logger.warning("Failed to cleanup expired keys: ${e.message}")
        }
    }
}
