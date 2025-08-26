package ltd.matrixstudios.alchemist.service.session

import io.github.nosequel.data.DataStoreType
import ltd.matrixstudios.alchemist.Alchemist
import ltd.matrixstudios.alchemist.models.profile.GameProfile
import ltd.matrixstudios.alchemist.models.sessions.Session
import ltd.matrixstudios.alchemist.service.GeneralizedService
import org.bson.Document
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

object SessionService : GeneralizedService {

    var handler = Alchemist.dataHandler.createStoreType<String, Session>(Alchemist.getDataStoreMethod())
    private val rawCollection = Alchemist.MongoConnectionPool.getCollection("session")

    var cache = ConcurrentHashMap<UUID, List<Session>>()

    fun save(session: Session) {
        CompletableFuture.runAsync {
            handler.store(session.randomId, session)
        }
    }

    fun loadIntoCache(profile: GameProfile) {
        val filter = Document("player", profile.uuid.toString())

        val documents = rawCollection.find(filter)
        val sessions = mutableListOf<Session>()

        for (document in documents) {
            sessions.add(Alchemist.gson.fromJson(document.toJson(), Session::class.java))
        }

        cache[profile.uuid] = sessions
    }

    // ----------------------------
    // 🔹 Heartbeat + Cleanup
    // ----------------------------

    fun heartbeat(playerId: UUID) {
        val sessions = cache[playerId] ?: return
        val active = sessions.maxByOrNull { it.loggedInAt } ?: return

        if (active.leftAt == 0L) {
            active.lastHeartbeat = System.currentTimeMillis()
            save(active)
        }
    }

    fun cleanupInactiveSessions(maxIdleMinutes: Long = 2) {
        CompletableFuture.runAsync {
            val cutoff = System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(maxIdleMinutes)

            val filter = Document("leftAt", null)
                .append("lastHeartbeat", Document("\$lt", cutoff))

            val stale = rawCollection.find(filter)

            for (doc in stale) {
                val session = Alchemist.gson.fromJson(doc.toJson(), Session::class.java)
                session.leftAt = cutoff
                save(session)
            }
        }
    }
}
