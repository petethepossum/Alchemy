package ltd.matrixstudios.alchemist.redis

object RedisConfig {
    // Connection settings - these will be loaded from your existing config.yml
    var host: String = "127.0.0.1"
    var port: Int = 6379
    var username: String? = null
    var password: String? = null
    
    // Connection pool settings - using sensible defaults
    var maxTotalConnections: Int = 20
    var maxIdleConnections: Int = 10
    var minIdleConnections: Int = 2
    var connectionTimeout: Int = 2000
    var maxWaitTime: Int = 5000
    
    // Health monitoring settings - using sensible defaults
    var healthCheckInterval: Long = 30L // seconds
    var maxReconnectAttempts: Int = 5
    var slowQueryThreshold: Long = 1000L // milliseconds
    
    // Retry settings - using sensible defaults
    var maxRetries: Int = 3
    var retryBackoffMultiplier: Long = 1000L // milliseconds
    
    // TTL settings - using sensible defaults
    var onlineStatusTTL: Int = 60 // seconds
    var vanishStatusTTL: Int = 0 // 0 = no expiration
    
    // Configuration loading methods that work without Spigot API
    fun loadBasicConfig(host: String, port: Int, username: String?, password: String?) {
        this.host = host
        this.port = port
        this.username = username.takeIf { it?.isNotBlank() == true }
        this.password = password.takeIf { it?.isNotBlank() == true }
        
        // Log the configuration being used
        println("[Redis] Basic configuration loaded:")
        println("[Redis] Host: $host")
        println("[Redis] Port: $port")
        println("[Redis] Username: ${username ?: "None"}")
        println("[Redis] Password: ${if (password != null) "***" else "None"}")
    }
    
    fun loadAdvancedConfig(
        maxTotalConnections: Int = 20,
        maxIdleConnections: Int = 10,
        minIdleConnections: Int = 2,
        connectionTimeout: Int = 2000,
        maxWaitTime: Int = 5000,
        healthCheckInterval: Long = 30L,
        maxReconnectAttempts: Int = 5,
        slowQueryThreshold: Long = 1000L,
        maxRetries: Int = 3,
        retryBackoffMultiplier: Long = 1000L,
        onlineStatusTTL: Int = 60,
        vanishStatusTTL: Int = 0
    ) {
        this.maxTotalConnections = maxTotalConnections
        this.maxIdleConnections = maxIdleConnections
        this.minIdleConnections = minIdleConnections
        this.connectionTimeout = connectionTimeout
        this.maxWaitTime = maxWaitTime
        this.healthCheckInterval = healthCheckInterval
        this.maxReconnectAttempts = maxReconnectAttempts
        this.slowQueryThreshold = slowQueryThreshold
        this.maxRetries = maxRetries
        this.retryBackoffMultiplier = retryBackoffMultiplier
        this.onlineStatusTTL = onlineStatusTTL
        this.vanishStatusTTL = vanishStatusTTL
        
        println("[Redis] Advanced configuration loaded:")
        println("[Redis] Max Total Connections: $maxTotalConnections")
        println("[Redis] Health Check Interval: ${healthCheckInterval}s")
        println("[Redis] Max Retries: $maxRetries")
        println("[Redis] Online Status TTL: ${onlineStatusTTL}s")
    }
    
    fun getConnectionString(): String {
        return if (username != null && password != null) {
            "redis://$username:$password@$host:$port"
        } else {
            "redis://$host:$port"
        }
    }
    
    fun isConfigured(): Boolean {
        return host.isNotBlank() && port > 0
    }
    
    fun validate(): List<String> {
        val errors = mutableListOf<String>()
        
        if (host.isBlank()) {
            errors.add("Redis host cannot be blank")
        }
        
        if (port <= 0 || port > 65535) {
            errors.add("Redis port must be between 1 and 65535")
        }
        
        if (maxTotalConnections <= 0) {
            errors.add("Max total connections must be positive")
        }
        
        if (maxIdleConnections > maxTotalConnections) {
            errors.add("Max idle connections cannot exceed max total connections")
        }
        
        if (minIdleConnections > maxIdleConnections) {
            errors.add("Min idle connections cannot exceed max idle connections")
        }
        
        if (healthCheckInterval <= 0) {
            errors.add("Health check interval must be positive")
        }
        
        if (maxRetries < 0) {
            errors.add("Max retries cannot be negative")
        }
        
        return errors
    }
}
