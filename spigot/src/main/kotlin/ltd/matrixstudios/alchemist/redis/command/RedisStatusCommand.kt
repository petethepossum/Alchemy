package ltd.matrixstudios.alchemist.redis.command

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Subcommand
import ltd.matrixstudios.alchemist.redis.RedisHealthMonitor
import ltd.matrixstudios.alchemist.redis.RedisPacketManager
import ltd.matrixstudios.alchemist.redis.RedisConfig
import ltd.matrixstudios.alchemist.util.Chat
import ltd.matrixstudios.alchemist.util.TimeUtil
import org.bukkit.command.CommandSender
import java.util.concurrent.TimeUnit

@CommandAlias("redisstatus")
@CommandPermission("alchemist.admin")
object RedisStatusCommand : BaseCommand() {

    @Default
    fun default(sender: CommandSender) {
        info(sender)
    }

    @Subcommand("info")
    fun info(player: CommandSender) {
        player.sendMessage(Chat.format("&7&m--------------------------"))
        player.sendMessage(Chat.format("&4&lRedis Status Dashboard"))
        player.sendMessage(Chat.format("&7&m--------------------------"))

        // Connection info
        player.sendMessage(Chat.format("&e&lConnection Information"))
        player.sendMessage(Chat.format("&cHost&7: &f${RedisConfig.host}"))
        player.sendMessage(Chat.format("&cPort&7: &f${RedisConfig.port}"))
        player.sendMessage(Chat.format("&cUsername&7: &f${RedisConfig.username ?: "None"}"))
        player.sendMessage(Chat.format("&cPassword&7: &f${if (RedisConfig.password != null) "***" else "None"}"))

        // Health status
        player.sendMessage(Chat.format("&7&m--------------------------"))
        player.sendMessage(Chat.format("&e&lHealth Status"))
        player.sendMessage(Chat.format("&cStatus&7: &f${RedisPacketManager.getHealthStatus()}"))
        player.sendMessage(Chat.format("&cInitialized&7: &f${if (RedisPacketManager.isInitialized()) "Yes" else "No"}"))

        // Connection statistics
        val stats = RedisPacketManager.getConnectionStats()
        val lastHealthCheck = stats["lastHealthCheck"] as? Long ?: 0L
        val failures = stats["connectionFailures"] as? Long ?: 0L

        if (lastHealthCheck > 0) {
            val timeSinceLastCheck = System.currentTimeMillis() - lastHealthCheck
            player.sendMessage(Chat.format("&cLast Health Check&7: &f${TimeUtil.formatDuration(timeSinceLastCheck)} ago"))
        }

        if (failures > 0) {
            player.sendMessage(Chat.format("&cConnection Failures&7: &f$failures"))
        }

        // Pool statistics
        val poolStats = stats["poolStats"] as? Map<*, *>
        if (poolStats != null) {
            player.sendMessage(Chat.format("&7&m--------------------------"))
            player.sendMessage(Chat.format("&e&lConnection Pool"))
            player.sendMessage(Chat.format("&cActive&7: &f${poolStats["activeConnections"] ?: "N/A"}"))
            player.sendMessage(Chat.format("&cIdle&7: &f${poolStats["idleConnections"] ?: "N/A"}"))
            player.sendMessage(Chat.format("&cTotal&7: &f${poolStats["totalConnections"] ?: "N/A"}"))
            player.sendMessage(Chat.format("&cWaiting&7: &f${poolStats["waitingForConnection"] ?: "N/A"}"))
        }

        // Configuration
        player.sendMessage(Chat.format("&7&m--------------------------"))
        player.sendMessage(Chat.format("&e&lConfiguration"))
        player.sendMessage(Chat.format("&cMax Total Connections&7: &f${RedisConfig.maxTotalConnections}"))
        player.sendMessage(Chat.format("&cMax Idle Connections&7: &f${RedisConfig.maxIdleConnections}"))
        player.sendMessage(Chat.format("&cHealth Check Interval&7: &f${RedisConfig.healthCheckInterval}s"))
        player.sendMessage(Chat.format("&cMax Retries&7: &f${RedisConfig.maxRetries}"))
        player.sendMessage(Chat.format("&cOnline Status TTL&7: &f${RedisConfig.onlineStatusTTL}s"))

        player.sendMessage(Chat.format("&7&m--------------------------"))
    }

    @Subcommand("health")
    fun health(player: CommandSender) {
        player.sendMessage(Chat.format("&7&m--------------------------"))
        player.sendMessage(Chat.format("&4&lRedis Health Check"))
        player.sendMessage(Chat.format("&7&m--------------------------"))

        try {
            player.sendMessage(Chat.format("&ePerforming manual health check..."))

            // Perform manual health check using the commons module
            val healthResult = RedisHealthMonitor.performManualHealthCheck()

            player.sendMessage(Chat.format("&cHealth Status&7: &f${if (healthResult.isHealthy) "&aHealthy" else "&cUnhealthy"}"))
            player.sendMessage(Chat.format("&cResponse Time&7: &f${healthResult.responseTime}ms"))
            player.sendMessage(Chat.format("&cTimestamp&7: &f${TimeUtil.formatDuration(System.currentTimeMillis() - healthResult.timestamp)} ago"))
            player.sendMessage(Chat.format("&cConnection Failures&7: &f${healthResult.connectionFailures}"))

            if (healthResult.isSlow) {
                player.sendMessage(Chat.format("&e⚠ Response time is above threshold (${RedisConfig.slowQueryThreshold}ms)"))
            }

            // Test basic operations if healthy
            if (healthResult.isHealthy) {
                player.sendMessage(Chat.format("&7&m--------------------------"))
                player.sendMessage(Chat.format("&e&lOperation Tests"))

                // Test ping
                try {
                    val pingStart = System.currentTimeMillis()
                    RedisPacketManager.executeWithRetryAndReturn { jedis ->
                        jedis.ping()
                    }
                    val pingTime = System.currentTimeMillis() - pingStart
                    player.sendMessage(Chat.format("&cPing Test&7: &a✓ &f(${pingTime}ms)"))
                } catch (e: Exception) {
                    player.sendMessage(Chat.format("&cPing Test&7: &c✗ &f${e.message}"))
                }

                // Test online players count
                try {
                    val countStart = System.currentTimeMillis()
                    val count = RedisPacketManager.executeWithRetryAndReturn { jedis ->
                        jedis.keys("Alchemist:online:*").size
                    }
                    val countTime = System.currentTimeMillis() - countStart
                    player.sendMessage(Chat.format("&cOnline Players Count&7: &a✓ &f$count (${countTime}ms)"))
                } catch (e: Exception) {
                    player.sendMessage(Chat.format("&cOnline Players Count&7: &c✗ &f${e.message}"))
                }
            }

        } catch (e: Exception) {
            player.sendMessage(Chat.format("&cHealth check failed&7: &c${e.message}"))
        }

        player.sendMessage(Chat.format("&7&m--------------------------"))
    }

    @Subcommand("reconnect")
    fun reconnect(player: CommandSender) {
        player.sendMessage(Chat.format("&7&m--------------------------"))
        player.sendMessage(Chat.format("&4&lRedis Reconnection"))
        player.sendMessage(Chat.format("&7&m--------------------------"))

        try {
            player.sendMessage(Chat.format("&eAttempting to reconnect to Redis..."))

            // Force health monitor to attempt reconnection
            RedisHealthMonitor.attemptReconnection()

            player.sendMessage(Chat.format("&aReconnection attempt completed"))
            player.sendMessage(Chat.format("&cNew Status&7: &f${RedisPacketManager.getHealthStatus()}"))

        } catch (e: Exception) {
            player.sendMessage(Chat.format("&cReconnection failed&7: &c${e.message}"))
        }

        player.sendMessage(Chat.format("&7&m--------------------------"))
    }

    @Subcommand("stats")
    fun stats(player: CommandSender) {
        player.sendMessage(Chat.format("&7&m--------------------------"))
        player.sendMessage(Chat.format("&4&lRedis Statistics"))
        player.sendMessage(Chat.format("&7&m--------------------------"))

        val stats = RedisPacketManager.getConnectionStats()

        // Connection history
        val lastHealthCheck = stats["lastHealthCheck"] as? Long ?: 0L
        val failures = stats["connectionFailures"] as? Long ?: 0L

        if (lastHealthCheck > 0) {
            val uptime = System.currentTimeMillis() - lastHealthCheck
            val uptimeFormatted = TimeUtil.formatDuration(uptime)
            player.sendMessage(Chat.format("&cUptime&7: &f$uptimeFormatted"))
        }

        player.sendMessage(Chat.format("&cTotal Failures&7: &f$failures"))

        // Pool utilization
        val poolStats = stats["poolStats"] as? Map<*, *>
        if (poolStats != null) {
            val active = poolStats["activeConnections"] as? Int ?: 0
            val total = poolStats["totalConnections"] as? Int ?: 0
            val utilization = if (total > 0) (active * 100.0 / total) else 0.0

            player.sendMessage(Chat.format("&7&m--------------------------"))
            player.sendMessage(Chat.format("&e&lPool Utilization"))
            player.sendMessage(Chat.format("&cActive/Total&7: &f$active/$total"))
            player.sendMessage(Chat.format("&cUtilization&7: &f${String.format("%.1f", utilization)}%"))

            if (utilization > 80.0) {
                player.sendMessage(Chat.format("&e⚠ High connection pool utilization"))
            }
        }

        player.sendMessage(Chat.format("&7&m--------------------------"))
    }

    @Subcommand("test")
    fun test(player: CommandSender) {
        player.sendMessage(Chat.format("&7&m--------------------------"))
        player.sendMessage(Chat.format("&4&lRedis Connection Test"))
        player.sendMessage(Chat.format("&7&m--------------------------"))

        try {
            player.sendMessage(Chat.format("&eTesting Redis connection..."))

            // Test basic connection
            val startTime = System.currentTimeMillis()
            val isConnected = RedisPacketManager.isHealthy()
            val responseTime = System.currentTimeMillis() - startTime

            if (isConnected) {
                player.sendMessage(Chat.format("&a✓ Connection successful (${responseTime}ms)"))

                // Test a simple Redis operation
                try {
                    val testKey = "test:connection:${System.currentTimeMillis()}"
                    RedisPacketManager.executeWithRetry { jedis ->
                        jedis.setex(testKey, 10, "test")
                    }

                    val value = RedisPacketManager.executeWithRetryAndReturn { jedis ->
                        jedis.get(testKey)
                    }

                    if (value == "test") {
                        player.sendMessage(Chat.format("&a✓ Read/Write test successful"))
                    } else {
                        player.sendMessage(Chat.format("&c✗ Read/Write test failed"))
                    }

                    // Clean up test key
                    RedisPacketManager.executeWithRetry { jedis ->
                        jedis.del(testKey)
                    }

                } catch (e: Exception) {
                    player.sendMessage(Chat.format("&c✗ Read/Write test failed: ${e.message}"))
                }

            } else {
                player.sendMessage(Chat.format("&c✗ Connection failed"))
            }

        } catch (e: Exception) {
            player.sendMessage(Chat.format("&c✗ Test failed: ${e.message}"))
        }

        player.sendMessage(Chat.format("&7&m--------------------------"))
    }
}
