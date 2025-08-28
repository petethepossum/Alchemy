package ltd.matrixstudios.alchemist.profiles.commands.player

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import ltd.matrixstudios.alchemist.api.AlchemistAPI
import ltd.matrixstudios.alchemist.redis.RedisVanishStatusService
import ltd.matrixstudios.alchemist.service.ranks.RankService
import ltd.matrixstudios.alchemist.util.Chat
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.entity.Player
import java.util.concurrent.CompletableFuture

class ListCommand : BaseCommand() {

    @CommandAlias("list|players|online|who")
    fun list(sender: CommandSender) {
        val isStaff = sender is Player && sender.hasPermission("alchemist.staff")

        val onlinePlayers = Bukkit.getOnlinePlayers().toList()

        CompletableFuture.supplyAsync {
            val playersToShow = onlinePlayers
                .filter { player ->
                    val vanished = RedisVanishStatusService.isVanished(player.uniqueId)
                    if (isStaff) true else !vanished
                }
                .sortedByDescending { AlchemistAPI.getRankWeight(it.uniqueId) }

            val formattedPlayers = playersToShow.take(350).joinToString("&f, ") { player ->
                val isVanished = RedisVanishStatusService.isVanished(player.uniqueId)
                val vanishPrefix = if (isVanished) "&7[V] " else ""
                val rankDisplay = AlchemistAPI.getRankDisplay(player.uniqueId)
                Chat.format("$vanishPrefix$rankDisplay")
            }


            val onlineCount = onlinePlayers.count {
                val vanished = RedisVanishStatusService.isVanished(it.uniqueId)
                if (isStaff) true else !vanished
            }

            Triple(formattedPlayers, playersToShow.size, onlineCount)
        }.thenAccept { (formattedPlayers, totalPlayers, onlineCount) ->
            val coloredRankNames = RankService.getRanksInOrder().joinToString("&f, ") { it.color + it.displayName }

            sender.sendMessage(Chat.format(" "))
            sender.sendMessage(Chat.format(coloredRankNames))
            sender.sendMessage(Chat.format("&f($onlineCount/${Bukkit.getMaxPlayers()}&f) $formattedPlayers"))

            if (totalPlayers > 350) {
                sender.sendMessage(Chat.format("&c(Only showing first 350 entries...)"))
            }

            sender.sendMessage(" ")
        }
    }
}