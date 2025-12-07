package ltd.matrixstudios.alchemist.redis

import com.sun.jmx.snmp.EnumRowStatus.active
import org.apache.commons.pool2.impl.GenericObjectPoolConfig
import redis.clients.jedis.Jedis
import redis.clients.jedis.JedisPool
import redis.clients.jedis.JedisPoolConfig
import redis.clients.jedis.exceptions.JedisConnectionException
import redis.clients.jedis.exceptions.JedisException
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong

object RedisHealthMonitor {
    private var healthCheckExecutor: ScheduledExecutorService? = null
    private val isHealthy = AtomicBoolean(false)
    private val lastHealthCheck = AtomicLong(0)
    private val connectionFailures = AtomicLong(0)
    private var currentPool: JedisPool? = null

    // Connection pool configuration
    fun createOptimizedPool(
        host: String,
        port: Int,
        password: String?,
        username: String?
    ): JedisPool {
        val poolConfig = JedisPoolConfig().apply {
            maxTotal = RedisConfig.maxTotalConnections
            maxIdle = RedisConfig.maxIdleConnections
            minIdle = RedisConfig.minIdleConnections
            testOnBorrow = true
            testOnReturn = true
            testWhileIdle = true
            timeBetweenEvictionRunsMillis = 30000
            minEvictableIdleTimeMillis = 60000
            maxWaitMillis = RedisConfig.maxWaitTime.toLong()
            blockWhenExhausted = true
        }

        return JedisPool(poolConfig, host, port, RedisConfig.connectionTimeout, password, username)
    }

    fun setPool(pool: JedisPool) {
        currentPool = pool
    }

    fun startHealthMonitoring() {
        if (healthCheckExecutor != null) {
            return
        }

        healthCheckExecutor = Executors.newSingleThreadScheduledExecutor { r ->
            val thread = Thread(r, "Redis-Health-Monitor")
            thread.isDaemon = true
            thread
        }

        healthCheckExecutor?.scheduleAtFixedRate({
            performHealthCheck()
        }, 0, RedisConfig.healthCheckInterval, TimeUnit.SECONDS)

        println("[Redis] Health monitoring started (interval: ${RedisConfig.healthCheckInterval}s)")
    }

    fun stopHealthMonitoring() {
        healthCheckExecutor?.shutdown()
        healthCheckExecutor = null
        println("[Redis] Health monitoring stopped")
    }

    // Method that can be called from Spigot commands to trigger a manual health check
    fun performManualHealthCheck(): HealthCheckResult {
        val startTime = System.currentTimeMillis()
        val isConnected = testConnection()
        val responseTime = System.currentTimeMillis() - startTime

        if (isConnected) {
            isHealthy.set(true)
            connectionFailures.set(0)
            lastHealthCheck.set(System.currentTimeMillis())

            if (responseTime > RedisConfig.slowQueryThreshold) {
                println("[Redis] Manual health check slow: ${responseTime}ms")
            }
        } else {
            isHealthy.set(false)
            connectionFailures.incrementAndGet()
            println("[Redis] Manual health check failed")

            if (connectionFailures.get() >= RedisConfig.maxReconnectAttempts) {
                attemptReconnection()
            }
        }

        return HealthCheckResult(
            isHealthy = isConnected,
            responseTime = responseTime,
            timestamp = System.currentTimeMillis(),
            connectionFailures = connectionFailures.get(),
            isSlow = responseTime > RedisConfig.slowQueryThreshold
        )
    }

    private fun performHealthCheck() {
        try {
            val startTime = System.currentTimeMillis()
            val isConnected = testConnection()
            val responseTime = System.currentTimeMillis() - startTime

            if (isConnected) {
                isHealthy.set(true)
                connectionFailures.set(0)
                lastHealthCheck.set(System.currentTimeMillis())

                if (responseTime > RedisConfig.slowQueryThreshold) {
                    println("[Redis] Health check slow: ${responseTime}ms")
                }
            } else {
                isHealthy.set(false)
                connectionFailures.incrementAndGet()
                println("[Redis] Health check failed")

                if (connectionFailures.get() >= RedisConfig.maxReconnectAttempts) {
                    attemptReconnection()
                }
            }
        } catch (e: Exception) {
            isHealthy.set(false)
            connectionFailures.incrementAndGet()
            println("[Redis] Health check error: ${e.message}")
        }
    }

    private fun testConnection(): Boolean {
        return try {
            currentPool?.resource?.use { jedis ->
                jedis.ping() == "PONG"
            } ?: false
        } catch (e: Exception) {
            false
        }
    }

    fun attemptReconnection() {
        println("[Redis] Attempting to reconnect...")
        try {
            // Force pool to close and recreate
            currentPool?.close()
            // Note: You'll need to implement pool recreation logic in RedisPacketManager
            println("[Redis] Reconnection attempted")
        } catch (e: Exception) {
            println("[Redis] Reconnection failed: ${e.message}")
        }
    }

    fun isHealthy(): Boolean = isHealthy.get()

    fun getLastHealthCheck(): Long = lastHealthCheck.get()

    fun getConnectionFailures(): Long = connectionFailures.get()

    fun getHealthStatus(): String {
        return if (isHealthy.get()) {
            "&aHealthy"
        } else {
            "&cUnhealthy (${connectionFailures.get()} failures)"
        }
    }

    fun getConnectionStats(): Map<String, Any> {
        return mapOf(
            "healthy" to isHealthy.get(),
            "lastHealthCheck" to lastHealthCheck.get(),
            "connectionFailures" to connectionFailures.get(),
            "poolStats" to getPoolStats()
        )
    }

    private fun getPoolStats(): Map<String, Any> {
        return try {
            val pool = currentPool
            if (pool != null) {
                val active = pool.numActive
                val idle = pool.numIdle
                val waiters = pool.numWaiters
                val total = active + idle

                mapOf(
                    "activeConnections" to active,
                    "idleConnections" to idle,
                    "totalConnections" to total,
                    "waitingForConnection" to waiters
                )
            } else {
                mapOf("error" to "Pool not initialized")
            }
        } catch (e: Exception) {
            mapOf("error" to (e.message ?: "Unknown error"))
        }
    }


    // Data class for health check results that can be used by Spigot commands
    data class HealthCheckResult(
        val isHealthy: Boolean,
        val responseTime: Long,
        val timestamp: Long,
        val connectionFailures: Long,
        val isSlow: Boolean
    )
}
