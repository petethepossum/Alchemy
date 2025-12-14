package ltd.matrixstudios.alchemist.service.chatsnap

import ltd.matrixstudios.alchemist.Alchemist
import ltd.matrixstudios.alchemist.models.chatsnap.ChatSnap
import org.bson.Document
import java.util.*
import java.util.concurrent.ConcurrentHashMap

object ChatSnapService {

    private val handler = Alchemist.dataHandler.createStoreType<UUID, ChatSnap>(Alchemist.getDataStoreMethod())
    private val cache = ConcurrentHashMap<Int, ChatSnap>()
    private val collection = Alchemist.MongoConnectionPool.getCollection("chatsnap")

    /**
     * Call this once on plugin enable to warm the cache.
     */
    fun loadIntoCache(limit: Int = 1000) {
        // Optional: only warm recent N snaps for memory reasons
        val cursor = collection.find().limit(limit)
        cursor.forEach { doc ->
            val snap = Alchemist.gson.fromJson(doc.toJson(), ChatSnap::class.java)
            if (snap.numericId != 0) {
                cache[snap.numericId] = snap
            }
        }
    }

    fun save(chatSnap: ChatSnap) {
        if (chatSnap.numericId == 0) {
            chatSnap.numericId = getNextNumericId()
        }

        handler.storeAsync(chatSnap.id, chatSnap)
        cache[chatSnap.numericId] = chatSnap
    }

    private fun getNextNumericId(): Int {
        // 1000–9999, re-roll if taken in cache
        val used = cache.keys.toSet()
        var id: Int
        do {
            id = (1000..9999).random()
        } while (id in used)
        return id
    }

    fun byNumericId(id: Int): ChatSnap? {
        // Try cache first
        cache[id]?.let { return it }

        // Fallback to DB
        val query = Document("numericId", id)
        val found = collection.find(query).first() ?: return null
        val snap = Alchemist.gson.fromJson(found.toJson(), ChatSnap::class.java)
        cache[id] = snap
        return snap
    }

    fun delete(chatSnap: ChatSnap) {
        handler.delete(chatSnap.id)
        if (chatSnap.numericId != 0) cache.remove(chatSnap.numericId)
    }

    fun deleteByNumericId(id: Int): Boolean {
        val snap = byNumericId(id) ?: return false
        delete(snap)
        return true
    }

    /**
     * Return all saved ChatSnaps for an owner (cache + DB),
     * sorted descending by createdAt.
     */
    fun getByOwner(owner: UUID, limit: Int = 50): List<ChatSnap> {
        val snaps = mutableListOf<ChatSnap>()

        // Cache
        snaps.addAll(cache.values.filter { it.owner == owner })

        // DB
        val query = Document("owner", owner.toString())
        val found = collection.find(query)
        found.forEach { doc ->
            val cs = Alchemist.gson.fromJson(doc.toJson(), ChatSnap::class.java)
            if (snaps.none { it.id == cs.id }) snaps.add(cs)
        }

        return snaps
            .sortedByDescending { it.createdAt }
            .take(limit)
    }
}
