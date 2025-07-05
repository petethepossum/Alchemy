package ltd.matrixstudios.alchemist.party

import ltd.matrixstudios.alchemist.service.party.PartyService
import ltd.matrixstudios.alchemist.util.Chat
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent

class PartyDisconnectListener : Listener {

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        val uuid = event.player.uniqueId

        PartyService.getParty(uuid).thenAccept { party ->
            if (party != null && party.leader == uuid) {
                // Notify all members
                party.members.keys.forEach { memberUUID ->
                    val member = Bukkit.getPlayer(memberUUID)
                    member?.sendMessage(Chat.format("&cThe party has been disbanded because the leader logged out."))
                }

                // Also notify leader (in case they were offline)
                val leaderPlayer = Bukkit.getPlayer(uuid)
                leaderPlayer?.sendMessage(Chat.format("&cYou have disbanded your party by logging out."))

                // Remove party from datastore and cache
                PartyService.handler.deleteAsync(party.id)
                PartyService.backingPartyCache.remove(party.id)
            }
        }
    }
}
