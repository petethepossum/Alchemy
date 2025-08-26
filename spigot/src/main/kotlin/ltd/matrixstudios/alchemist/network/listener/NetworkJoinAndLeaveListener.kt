package ltd.matrixstudios.alchemist.network.listener

import ltd.matrixstudios.alchemist.Alchemist
import ltd.matrixstudios.alchemist.api.AlchemistAPI
import ltd.matrixstudios.alchemist.redis.RedisOnlineStatusService
import ltd.matrixstudios.alchemist.service.profiles.ProfileGameService
import ltd.matrixstudios.alchemist.service.session.SessionService
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerPreLoginEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class NetworkJoinAndLeaveListener : Listener {

    // Track session start times for calculating playtime
    private val sessionStart = ConcurrentHashMap<UUID, Long>()

    @EventHandler
    fun asyncJoin(e: AsyncPlayerPreLoginEvent) {
        val profile = AlchemistAPI.syncFindProfile(e.uniqueId) ?: return

        profile.metadata.addProperty("server", Alchemist.globalServer.id)
        RedisOnlineStatusService.markOnline(profile.uuid, Alchemist.globalServer.id)
        profile.lastSeenAt = System.currentTimeMillis()
        profile.updateLoginTimes(System.currentTimeMillis())

        // Store the session start time
        sessionStart[e.uniqueId] = System.currentTimeMillis()

        ProfileGameService.save(profile)
    }

    @EventHandler
    fun disconnect(e: PlayerQuitEvent) {
        RedisOnlineStatusService.markOffline(e.player.uniqueId)

        AlchemistAPI.quickFindProfile(e.player.uniqueId).thenApply {
            if (it != null) {
                it.metadata.addProperty("server", "None")
                it.lastSeenAt = System.currentTimeMillis()

                // Calculate and add session playtime
                val start = sessionStart.remove(e.player.uniqueId)
                if (start != null) {
                    val sessionMillis = System.currentTimeMillis() - start
                    it.playtimeMillis += sessionMillis
              //debug pt
                println("Adjusted playtime for ${it.username}: ${it.playtimeMillis}ms")
                }

                if (it.currentSession != null) {
                    it.currentSession!!.leftAt = System.currentTimeMillis()
                    SessionService.save(it.currentSession!!)
                    it.currentSession = null
                }

                ProfileGameService.save(it)
            }
        }
    }
}
