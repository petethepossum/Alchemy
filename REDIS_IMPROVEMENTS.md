# Redis Status Tracking Improvements

This document outlines the improvements made to the Redis status tracking system in your Alchemist plugin.

## Overview

The Redis implementation has been significantly enhanced with:
- **Health Monitoring**: Continuous connection health checks
- **Automatic Reconnection**: Self-healing connection management
- **Better Error Handling**: Comprehensive exception handling with retry logic
- **Performance Optimization**: Connection pooling and batch operations
- **Administrative Tools**: Commands for monitoring and managing Redis

## Configuration

**Important**: This system works with your existing Redis configuration in `config.yml`. No changes to your current Redis settings are required!

### Your Existing Redis Configuration (Required)

```yaml
redis:
  host: "127.0.0.1"
  port: 6379
  username: ""
  password: ""
```

### Optional Advanced Configuration

You can optionally add advanced settings to your existing Redis section:

```yaml
redis:
  host: "127.0.0.1"
  port: 6379
  username: ""
  password: ""
  # Advanced Redis configuration (optional)
  advanced:
    # Connection pool settings
    maxTotalConnections: 20
    maxIdleConnections: 10
    minIdleConnections: 2
    connectionTimeout: 2000
    maxWaitTime: 5000
    
    # Health monitoring settings
    healthCheckInterval: 30
    maxReconnectAttempts: 5
    slowQueryThreshold: 1000
    
    # Retry settings
    maxRetries: 3
    retryBackoffMultiplier: 1000
    
    # TTL settings (in seconds)
    onlineStatusTTL: 60
    vanishStatusTTL: 0
```

**Note**: If you don't add the `advanced` section, the system will use sensible defaults that work well for most servers.

## New Features

### 1. Redis Health Monitor (`RedisHealthMonitor`)

**Location**: `commons/src/main/kotlin/ltd/matrixstudios/alchemist/redis/RedisHealthMonitor.kt`

- **Automatic Health Checks**: Runs every 30 seconds by default (configurable)
- **Connection Pool Optimization**: Uses your Redis connection settings
- **Failure Tracking**: Monitors connection failures and attempts reconnection
- **Performance Monitoring**: Tracks response times and alerts on slow queries

### 2. Enhanced Redis Packet Manager (`RedisPacketManager`)

**Location**: `commons/src/main/kotlin/ltd/matrixstudios/alchemist/redis/RedisPacketManager.kt`

- **Retry Mechanism**: Automatic retry with exponential backoff (configurable)
- **Connection Testing**: Validates connections on startup
- **Health Status**: Provides real-time connection health information
- **Graceful Shutdown**: Proper cleanup of resources

### 3. Improved Online Status Service (`RedisOnlineStatusService`)

**Location**: `commons/src/main/kotlin/ltd/matrixstudios/alchemist/redis/RedisOnlineStatusService.kt`

- **Async Operations**: Non-blocking Redis operations
- **Batch Processing**: Efficient handling of multiple operations
- **Fallback Handling**: Graceful degradation when Redis is unavailable
- **Performance Metrics**: Connection pool utilization tracking

### 4. Redis Configuration (`RedisConfig`)

**Location**: `commons/src/main/kotlin/ltd/matrixstudios/alchemist/redis/RedisConfig.kt`

- **Reads Your Existing Config**: Automatically loads from your `config.yml`
- **Sensible Defaults**: Works even without advanced configuration
- **Validation**: Configuration error checking
- **Centralized Settings**: All Redis settings in one place

## Commands

### Redis Command (`/redis`)

**Permission**: `alchemist.owner`

Provides basic Redis information:
- Connection status
- Packet statistics
- Online player count
- Server information

### Redis Status Command (`/redisstatus`)

**Permission**: `alchemist.admin`

#### Subcommands:

- **`/redisstatus info`** - Detailed connection and configuration information
- **`/redisstatus health`** - Real-time health check with operation testing
- **`/redisstatus reconnect`** - Force reconnection attempt
- **`/redisstatus stats`** - Connection pool utilization and statistics

## Health Monitoring

### Automatic Health Checks

The system automatically:
- Pings Redis every 30 seconds (configurable via `healthCheckInterval`)
- Tracks response times
- Monitors connection pool utilization
- Attempts automatic reconnection on failures

### Health Status Indicators

- **Healthy**: Redis is responding normally
- **Unhealthy**: Connection issues detected
- **Connection Failures**: Number of failed connection attempts

### Performance Alerts

- **Slow Query Warning**: Alerts when response time exceeds threshold
- **High Pool Utilization**: Warns when connection pool is heavily used
- **Connection Failures**: Tracks and reports connection issues

## Error Handling

### Retry Mechanism

- **Automatic Retries**: Up to 3 attempts by default (configurable via `maxRetries`)
- **Exponential Backoff**: Increasing delays between retry attempts
- **Connection Exception Handling**: Specific handling for connection issues
- **Graceful Degradation**: Fallback behavior when Redis is unavailable

### Exception Types

- **JedisConnectionException**: Network or connection issues
- **JedisException**: Redis-specific errors
- **General Exceptions**: Other runtime errors

## Performance Optimizations

### Connection Pooling

- **Optimized Pool Settings**: Uses your Redis connection settings
- **Connection Testing**: Validates connections before use
- **Resource Management**: Proper cleanup and resource handling

### Batch Operations

- **Pipeline Support**: Efficient handling of multiple Redis operations
- **Async Operations**: Non-blocking Redis calls
- **Connection Reuse**: Minimizes connection overhead

## Monitoring and Debugging

### Console Logging

The system provides detailed console logging:
- Connection status changes
- Health check results
- Performance warnings
- Error details

### Metrics Available

- Connection pool statistics
- Health check timing
- Failure counts
- Response times
- Pool utilization

## Best Practices

### 1. Configuration

- Your existing Redis configuration will work immediately
- Add advanced settings only if you need to tune performance
- Use health monitoring for production environments

### 2. Monitoring

- Regularly check `/redisstatus health`
- Monitor console logs for warnings
- Set up alerts for connection failures

### 3. Performance

- Use batch operations when possible
- Monitor connection pool utilization
- Adjust pool settings based on usage patterns

## Troubleshooting

### Common Issues

1. **Connection Refused**
   - Check Redis server status
   - Verify your existing host/port configuration
   - Check firewall settings

2. **Slow Response Times**
   - Monitor Redis server performance
   - Check network latency
   - Review connection pool settings

3. **High Failure Rates**
   - Check Redis server logs
   - Verify your existing authentication credentials
   - Monitor server resources

### Debug Commands

- `/redis` - Basic status information
- `/redisstatus health` - Detailed health check
- `/redisstatus stats` - Performance metrics

## Migration Notes

### Existing Code Compatibility

- **100% Backward Compatible**: All existing Redis operations continue to work
- **No Config Changes Required**: Works with your current `config.yml`
- **New Features Are Optional**: Advanced settings are completely optional
- **Performance Impact**: Minimal overhead from health monitoring

### What Happens on Startup

1. System reads your existing Redis configuration
2. Applies sensible defaults for any missing advanced settings
3. Starts health monitoring automatically
4. Logs the configuration being used

## Support

For issues or questions about the Redis improvements:
1. Check the console logs for error messages
2. Use the diagnostic commands (`/redisstatus`)
3. Verify your existing Redis configuration
4. Check Redis server status and logs

## Quick Start

1. **No configuration changes needed** - the system works with your existing Redis setup
2. Restart your server
3. Use `/redis` to see basic status
4. Use `/redis health` for quick health check
5. Use `/redisstatus health` for detailed health information
6. Monitor console logs for health monitoring messages

## Technical Details

### Platform-Agnostic Design

The Redis improvements are designed with a clean separation of concerns:

- **Commons Module** - Pure Kotlin/Java with no platform dependencies
  - Uses standard `println()` for logging
  - Standard Java logging (`Logger`) for warnings
  - No imports of Spigot, Bungee, or Velocity APIs
  - Works on any Java/Kotlin platform

- **Platform Plugins** - Handle platform-specific functionality
  - Spigot: Chat commands, colored console messages, health check triggers
  - Bungee: Basic Redis configuration loading
  - Velocity: Basic Redis configuration loading

### Configuration Loading Flow

```
config.yml → Platform Plugin → RedisConfig.loadBasicConfig() → Redis Services
     ↓
Advanced Section → RedisConfig.loadAdvancedConfig() → Enhanced Settings
```

### Dependencies

- **Commons Module**: No external dependencies, pure Kotlin
- **Platform Plugins**: Use their native configuration systems
- **Redis Services**: All use the centralized RedisConfig object

### Platform Support

- **Spigot**: Full support with advanced configuration options and admin commands
- **Bungee**: Full support with basic configuration
- **Velocity**: Full support with basic configuration
