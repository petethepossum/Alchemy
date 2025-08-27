package ltd.matrixstudios.alchemist.staff.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import ltd.matrixstudios.alchemist.api.AlchemistAPI
import ltd.matrixstudios.alchemist.redis.RedisOnlineStatusService
import ltd.matrixstudios.alchemist.redis.RedisVanishStatusService
import ltd.matrixstudios.alchemist.service.server.UniqueServerService
import ltd.matrixstudios.alchemist.util.Chat
import org.bukkit.entity.Player
import java.util.*

class OnlineStaffCommand : BaseCommand() {

    @CommandAlias("onlinestaff|globalstaff|stafflist")
    @CommandPermission("alchemist.staff.list")
    fun onlineStaff(player: Player) {
        val allPlayers = mutableSetOf<UUID>()
        val msgs = mutableListOf<String>()

        for (server in UniqueServerService.getValues()) {
            allPlayers.addAll(server.players)
        }

        for (uuid in allPlayers) {
            val profile = AlchemistAPI.syncFindProfile(uuid) ?: continue
            if (!profile.getCurrentRank().staff) continue

            val redisServer = RedisOnlineStatusService.getOnlineServer(uuid)
            val serverName = if (redisServer != null) {
                UniqueServerService.byId(redisServer.lowercase(Locale.getDefault()))?.displayName
                    ?: "&cUnknown"
            } else {
                "&cUnknown"
            }

            val isVanished = RedisVanishStatusService.isVanished(uuid)
            val vanishPrefix = if (isVanished) "&7[V] " else ""

            msgs.add("&7- $vanishPrefix${AlchemistAPI.getRankDisplay(uuid)} &eis currently &aonline &eat &f$serverName")
        }

        player.sendMessage(Chat.format("&e&lOnline Staff Members&7:"))
        msgs.forEach { player.sendMessage(Chat.format(it)) }
    }
}
