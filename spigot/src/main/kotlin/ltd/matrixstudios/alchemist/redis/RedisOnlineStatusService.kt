package ltd.matrixstudios.alchemist.redis

import ltd.matrixstudios.alchemist.redis.RedisPacketManager
import java.util.*

object RedisOnlineStatusService {
    private val pool = RedisPacketManager.pool

    fun markOnline(uuid: UUID, server: String) {
        pool.resource.use { jedis ->
            jedis.setex("online:$uuid", 60, server) // 60-second TTL
        }
    }

    fun markOffline(uuid: UUID) {
        pool.resource.use { jedis ->
            jedis.del("online:$uuid")
        }
    }

    fun isOnline(uuid: UUID): Boolean {
        pool.resource.use { jedis ->
            return jedis.exists("online:$uuid")
        }
    }

    fun getOnlineServer(uuid: UUID): String? {
        pool.resource.use { jedis ->
            return jedis.get("online:$uuid")
        }
    }
}
