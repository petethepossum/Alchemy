package ltd.matrixstudios.alchemist.chatsnap

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.ArrayList

object ChatCache : Listener {

    const val MAX_PER_PLAYER = 100

    private val cache = ConcurrentHashMap<UUID, ArrayDeque<String>>()

    @EventHandler
    fun onPlayerChat(event: AsyncPlayerChatEvent) {
        val uuid = event.player.uniqueId
        val deque = cache.computeIfAbsent(uuid) { ArrayDeque() }
        val line = "${event.player.name}: ${event.message}"

        synchronized(deque) {
            if (deque.size >= MAX_PER_PLAYER) {
                deque.removeFirst()
            }
            deque.addLast(line)
        }
    }

    fun getLastMessages(uuid: UUID, count: Int): List<String> {
        val deque = cache[uuid] ?: return emptyList()
        synchronized(deque) {
            val list = ArrayList(deque)
            val take = if (count >= list.size) list.size else count
            return list.takeLast(take)
        }
    }
}
