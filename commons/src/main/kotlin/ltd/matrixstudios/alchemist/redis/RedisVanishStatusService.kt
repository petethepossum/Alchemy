package ltd.matrixstudios.alchemist.redis

import java.util.*
import java.util.logging.Logger

object RedisVanishStatusService {
    private val logger = Logger.getLogger("RedisVanishStatusService")

    private fun getKey(uuid: UUID): String = "Alchemist:vanish:$uuid"

    fun setVanished(uuid: UUID) {
        try {
            if (RedisConfig.vanishStatusTTL > 0) {
                RedisPacketManager.executeWithRetry(RedisConfig.maxRetries) { jedis ->
                    jedis.setex(getKey(uuid), RedisConfig.vanishStatusTTL.toLong(), "true")
                }
            } else {
                RedisPacketManager.executeWithRetry(RedisConfig.maxRetries) { jedis ->
                    jedis.set(getKey(uuid), "true")
                }
            }
        } catch (e: Exception) {
            logger.warning("Failed to set vanished status for player $uuid: ${e.message}")
        }
    }

    fun delVanished(uuid: UUID) {
        try {
            RedisPacketManager.executeWithRetry(RedisConfig.maxRetries) { jedis ->
                jedis.del(getKey(uuid))
            }
        } catch (e: Exception) {
            logger.warning("Failed to remove vanished status for player $uuid: ${e.message}")
        }
    }

    fun isVanished(uuid: UUID): Boolean {
        return try {
            RedisPacketManager.executeWithRetryAndReturn(RedisConfig.maxRetries) { jedis ->
                jedis.exists(getKey(uuid))
            }
        } catch (e: Exception) {
            logger.warning("Failed to check vanished status for player $uuid: ${e.message}")
            // Default to not vanished if Redis is unavailable
            false
        }
    }
}
