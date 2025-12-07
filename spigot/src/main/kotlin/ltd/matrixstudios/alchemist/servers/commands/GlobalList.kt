package ltd.matrixstudios.alchemist.profiles.commands.player

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import ltd.matrixstudios.alchemist.api.AlchemistAPI
import ltd.matrixstudios.alchemist.redis.RedisOnlineStatusService
import ltd.matrixstudios.alchemist.service.server.UniqueServerService
import ltd.matrixstudios.alchemist.util.Chat
import org.bukkit.entity.Player
import java.util.*

class GlobalListCommand : BaseCommand() {

    @CommandAlias("globallist|glist|globalwho|gplayers")
    @CommandPermission("alchemist.staff.globallist")
    fun globallist(player: Player) {
        val servers = UniqueServerService.getValues()
        val allPlayers = mutableSetOf<UUID>()

        servers.forEach { server ->
            allPlayers.addAll(server.players)
        }

        player.sendMessage(Chat.format("&eThere are &f${allPlayers.size} &eplayers online globally!"))

        for (server in servers) {
            val players = mutableListOf<String>()

            for (uuid in server.players) {
                val profile = AlchemistAPI.syncFindProfile(uuid) ?: continue
                val display = AlchemistAPI.getRankDisplay(uuid)
                players.add(display)
            }

            val formattedPlayers = if (players.isNotEmpty()) {
                players.joinToString("&7, &f")
            } else {
                "&7None"
            }

            player.sendMessage(Chat.format("&e${server.displayName} &7(${server.players.size}): &f"))
            player.sendMessage(Chat.format("&f$formattedPlayers"))
        }
    }
}
