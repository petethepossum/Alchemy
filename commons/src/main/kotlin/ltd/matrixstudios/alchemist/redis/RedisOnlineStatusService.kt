package ltd.matrixstudios.alchemist.redis

import ltd.matrixstudios.alchemist.redis.RedisPacketManager
import java.util.*
//TODO fix last online status service so that is uses redis instead of mongo
object RedisOnlineStatusService {
    private val pool = RedisPacketManager.pool

    private fun getKey(uuid: UUID): String = "Alchemist:online:$uuid"

    fun markOnline(uuid: UUID, server: String) {
        pool.resource.use { jedis ->
            jedis.setex(getKey(uuid), 60, server) // 60-second TTL
        }
    }

    fun markOffline(uuid: UUID) {
        pool.resource.use { jedis ->
            jedis.del(getKey(uuid))
        }
    }

    fun isOnline(uuid: UUID): Boolean {
        pool.resource.use { jedis ->
            return jedis.exists(getKey(uuid))
        }
    }

    fun updateOnlineServer(uuid: UUID, server: String) {
        pool.resource.use { jedis ->
            jedis.setex(getKey(uuid), 60, server)
        }
    }

    fun getOnlineServer(uuid: UUID): String? {
        pool.resource.use { jedis ->
            return jedis.get(getKey(uuid))
        }
    }
}
