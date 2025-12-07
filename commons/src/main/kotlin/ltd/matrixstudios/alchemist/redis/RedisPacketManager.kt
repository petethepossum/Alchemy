package ltd.matrixstudios.alchemist.redis

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.LongSerializationPolicy
import redis.clients.jedis.Jedis
import redis.clients.jedis.JedisPool
import redis.clients.jedis.exceptions.JedisConnectionException
import redis.clients.jedis.exceptions.JedisException
import java.util.concurrent.CompletableFuture

object RedisPacketManager {
    lateinit var pool: JedisPool
    private var isInitialized = false

    var gson: Gson = GsonBuilder()
        .setLongSerializationPolicy(LongSerializationPolicy.STRING)
        .serializeNulls()
        .create()

    fun load(host: String, port: Int, password: String?, username: String?) {
        try {
            // Create optimized connection pool
            pool = RedisHealthMonitor.createOptimizedPool(host, port, password, username)

            // Set the pool in the health monitor
            RedisHealthMonitor.setPool(pool)

            // Test connection
            testConnection()

            // Start health monitoring
            RedisHealthMonitor.startHealthMonitoring()

            isInitialized = true
            println("[Redis] Successfully connected to $host:$port")

        } catch (e: Exception) {
            println("[Redis] Failed to connect to $host:$port: ${e.message}")
            throw e
        }
    }

    private fun testConnection() {
        pool.resource.use { jedis ->
            val response = jedis.ping()
            if (response != "PONG") {
                throw JedisException("Redis ping failed: expected PONG, got $response")
            }
        }
    }

    fun isInitialized(): Boolean = isInitialized

    fun isHealthy(): Boolean = RedisHealthMonitor.isHealthy()

    fun getHealthStatus(): String = RedisHealthMonitor.getHealthStatus()

    fun getConnectionStats(): Map<String, Any> = RedisHealthMonitor.getConnectionStats()

    /**
     * Execute an operation with automatic retry on JedisConnectionException.
     */
    fun executeWithRetry(
        maxRetries: Int = RedisConfig.maxRetries,
        operation: (Jedis) -> Unit
    ) {
        var attempt = 1
        var lastException: Exception? = null

        while (attempt <= maxRetries) {
            try {
                pool.resource.use { jedis ->
                    operation(jedis)
                }
                return // success
            } catch (e: JedisConnectionException) {
                lastException = e
                if (attempt < maxRetries) {
                    val delay = RedisConfig.retryBackoffMultiplier * attempt
                    println("[Redis] Connection attempt $attempt failed, retrying in ${delay}ms...")
                    Thread.sleep(delay)
                    attempt++
                } else {
                    throw e
                }
            } catch (e: Exception) {
                // Non-connection exceptions: do not retry
                throw e
            }
        }

        throw lastException ?: RuntimeException("All retry attempts failed")
    }

    /**
     * Execute an operation with automatic retry and return a value.
     */
    fun <T> executeWithRetryAndReturn(
        maxRetries: Int = RedisConfig.maxRetries,
        operation: (Jedis) -> T
    ): T {
        var attempt = 1
        var lastException: Exception? = null

        while (attempt <= maxRetries) {
            try {
                return pool.resource.use { jedis ->
                    operation(jedis)
                }
            } catch (e: JedisConnectionException) {
                lastException = e
                if (attempt < maxRetries) {
                    val delay = RedisConfig.retryBackoffMultiplier * attempt
                    println("[Redis] Connection attempt $attempt failed, retrying in ${delay}ms...")
                    Thread.sleep(delay)
                    attempt++
                } else {
                    throw e
                }
            } catch (e: Exception) {
                // Non-connection exceptions: do not retry
                throw e
            }
        }

        throw lastException ?: RuntimeException("All retry attempts failed")
    }

    fun executeAsync(operation: (Jedis) -> Unit): CompletableFuture<Void> {
        return CompletableFuture.runAsync {
            executeWithRetry(operation = operation)
        }
    }

    fun <T> executeAsyncWithReturn(operation: (Jedis) -> T): CompletableFuture<T> {
        return CompletableFuture.supplyAsync {
            executeWithRetryAndReturn(operation = operation)
        }
    }

    fun shutdown() {
        try {
            RedisHealthMonitor.stopHealthMonitoring()
            pool.close()
            isInitialized = false
            println("[Redis] Connection pool closed")
        } catch (e: Exception) {
            println("[Redis] Error during shutdown: ${e.message}")
        }
    }
}
