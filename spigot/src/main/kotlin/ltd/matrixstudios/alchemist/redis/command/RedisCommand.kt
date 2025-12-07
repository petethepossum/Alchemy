package ltd.matrixstudios.alchemist.redis.command

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Subcommand
import ltd.matrixstudios.alchemist.Alchemist
import ltd.matrixstudios.alchemist.AlchemistSpigotPlugin
import ltd.matrixstudios.alchemist.redis.AsynchronousRedisSender
import ltd.matrixstudios.alchemist.redis.LocalPacketPubSub
import ltd.matrixstudios.alchemist.redis.RedisPacketManager
import ltd.matrixstudios.alchemist.redis.RedisConfig
import ltd.matrixstudios.alchemist.util.Chat
import ltd.matrixstudios.alchemist.util.TimeUtil
import org.bukkit.command.CommandSender

object RedisCommand : BaseCommand()
{

    @CommandAlias("redis")
    @CommandPermission("alchemist.owner")
    fun redis(player: CommandSender)
    {
        player.sendMessage(Chat.format("&7&m--------------------------"))
        player.sendMessage(Chat.format("&4&lRedis &7❘ &fInformation"))
        player.sendMessage(Chat.format("&7&m--------------------------"))
        
        // Basic connection status
        val isConnected = !RedisPacketManager.pool.isClosed
        val isHealthy = RedisPacketManager.isHealthy()
        val healthStatus = RedisPacketManager.getHealthStatus()
        
        player.sendMessage(Chat.format("&cConnection Status&7: &f${if (isConnected) "&aConnected" else "&cDisconnected"}"))
        player.sendMessage(Chat.format("&cHealth Status&7: &f$healthStatus"))
        
        // Health monitoring details
        if (RedisPacketManager.isInitialized()) {
            val connectionStats = RedisPacketManager.getConnectionStats()
            val lastHealthCheck = connectionStats["lastHealthCheck"] as? Long ?: 0L
            val failures = connectionStats["connectionFailures"] as? Long ?: 0L
            
            if (lastHealthCheck > 0) {
                val timeSinceLastCheck = System.currentTimeMillis() - lastHealthCheck
                player.sendMessage(Chat.format("&cLast Health Check&7: &f${TimeUtil.formatDuration(timeSinceLastCheck)} ago"))
            }
            
            if (failures > 0) {
                player.sendMessage(Chat.format("&cConnection Failures&7: &f$failures"))
            }
            
            // Pool statistics
            val poolStats = connectionStats["poolStats"] as? Map<*, *>
            if (poolStats != null) {
                player.sendMessage(Chat.format("&7&m--------------------------"))
                player.sendMessage(Chat.format("&e&lConnection Pool Stats"))
                player.sendMessage(Chat.format("&cActive Connections&7: &f${poolStats["activeConnections"] ?: "N/A"}"))
                player.sendMessage(Chat.format("&cIdle Connections&7: &f${poolStats["idleConnections"] ?: "N/A"}"))
                player.sendMessage(Chat.format("&cTotal Connections&7: &f${poolStats["totalConnections"] ?: "N/A"}"))
                player.sendMessage(Chat.format("&cWaiting for Connection&7: &f${poolStats["waitingForConnection"] ?: "N/A"}"))
            }
        }
        
        player.sendMessage(Chat.format("&7&m--------------------------"))
        player.sendMessage(Chat.format("&e&lNetwork Information"))
        player.sendMessage(Chat.format("&cListening On&7: &fAlchemist||Packets||%packet%"))
        val totalPackets = AsynchronousRedisSender.totalPacketCount
        val receivedPackets = LocalPacketPubSub.received
        player.sendMessage(Chat.format("&cTotal Packets Sent&7: &f${totalPackets}"))
        player.sendMessage(Chat.format("&cTotal Packets Received&7: &f${receivedPackets}"))
        player.sendMessage(
            Chat.format(
                "&cConnected for&7: &f${
                    TimeUtil.formatDuration(
                        System.currentTimeMillis().minus(AlchemistSpigotPlugin.instance.launchedAt)
                    )
                }"
            )
        )
        player.sendMessage(Chat.format("&cPort&7: &f${Alchemist.redisConnectionPort}"))
        
        // Online players count with better error handling
        val onlinePlayers = try {
            if (RedisPacketManager.isHealthy()) {
                RedisPacketManager.executeWithRetryAndReturn { jedis ->
                    jedis.keys("Alchemist:online:*").size
                }
            } else {
                -2 // Health check failed
            }
        } catch (ex: Exception) {
            -1
        }
        
        val onlineStatus = when (onlinePlayers) {
            -2 -> "&eHealth Check Failed"
            -1 -> "&cError"
            else -> onlinePlayers.toString()
        }
        
        player.sendMessage(Chat.format("&cOnline Players (Redis)&7: &f$onlineStatus"))
        player.sendMessage(Chat.format("&7&m--------------------------"))
        
        player.sendMessage(Chat.format("&eUse &c/redisstatus health &efor detailed health check"))
        player.sendMessage(Chat.format("&eUse &c/redisstatus info &efor full status dashboard"))
    }
    
    @Subcommand("health")
    fun health(player: CommandSender) {
        player.sendMessage(Chat.format("&7&m--------------------------"))
        player.sendMessage(Chat.format("&4&lRedis Quick Health Check"))
        player.sendMessage(Chat.format("&7&m--------------------------"))
        
        try {
            // Perform quick health check
            val healthResult = ltd.matrixstudios.alchemist.redis.RedisHealthMonitor.performManualHealthCheck()
            
            player.sendMessage(Chat.format("&cStatus&7: &f${if (healthResult.isHealthy) "&a✓ Healthy" else "&c✗ Unhealthy"}"))
            player.sendMessage(Chat.format("&cResponse Time&7: &f${healthResult.responseTime}ms"))
            player.sendMessage(Chat.format("&cFailures&7: &f${healthResult.connectionFailures}"))
            
            if (healthResult.isSlow) {
                player.sendMessage(Chat.format("&e⚠ Slow response (above ${RedisConfig.slowQueryThreshold}ms threshold)"))
            }
            
        } catch (e: Exception) {
            player.sendMessage(Chat.format("&c✗ Health check failed: ${e.message}"))
        }
        
        player.sendMessage(Chat.format("&7&m--------------------------"))
    }
}