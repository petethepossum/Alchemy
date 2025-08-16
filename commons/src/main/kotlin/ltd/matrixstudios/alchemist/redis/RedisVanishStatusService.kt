package ltd.matrixstudios.alchemist.redis

import ltd.matrixstudios.alchemist.redis.RedisPacketManager
import java.util.*

object RedisVanishStatusService {
    private val pool = RedisPacketManager.pool

    private fun getKey(uuid: UUID): String = "Alchemist:vanish:$uuid"

    fun setVanished(uuid: UUID) {
        pool.resource.use { jedis ->
            jedis.set(getKey(uuid), "true")
        }
    }

    fun delVanished(uuid: UUID) {
        pool.resource.use { jedis ->
            jedis.del(getKey(uuid))
        }
    }

    fun isVanished(uuid: UUID): Boolean {
        pool.resource.use { jedis ->
            return jedis.exists(getKey(uuid))
        }
    }
}
