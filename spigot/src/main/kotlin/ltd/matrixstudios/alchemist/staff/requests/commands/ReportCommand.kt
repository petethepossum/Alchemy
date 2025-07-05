package ltd.matrixstudios.alchemist.staff.requests.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.*
import ltd.matrixstudios.alchemist.Alchemist
import ltd.matrixstudios.alchemist.api.AlchemistAPI
import ltd.matrixstudios.alchemist.redis.AsynchronousRedisSender
import ltd.matrixstudios.alchemist.staff.requests.commands.menu.ReportSelectCategoryMenu
import ltd.matrixstudios.alchemist.staff.requests.handlers.RequestHandler
import ltd.matrixstudios.alchemist.staff.requests.packets.ReportPacket
import ltd.matrixstudios.alchemist.staff.requests.report.ReportModel
import ltd.matrixstudios.alchemist.util.Chat
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.*

class ReportCommand : BaseCommand() {

    @CommandAlias("reports|viewreports")
    @CommandPermission("alchemist.staff")
    fun viewReports(player: Player) {
        ReportSelectCategoryMenu(player).openMenu()
    }

    @CommandAlias("report")
    @CommandCompletion("@gameprofile")
    fun request(player: Player, @Name("player")other:String, @Name("reason")rzn: String) {
        try {
            val targetPlayer = Bukkit.getPlayer(other) ?: run {
                player.sendMessage(Chat.format("&cThe specified player is not online or does not exist."))
                return
            }

            if (player.name.equals(targetPlayer.name, ignoreCase = true)) {
                player.sendMessage(Chat.format("&cYou cannot report yourself!"))
                return
            }

            if (RequestHandler.isOnReportCooldown(player)) {
                player.sendMessage(Chat.format("&cYou are on cooldown. Please wait before reporting again!"))
                return
            }

            if (rzn.isBlank()) {
                player.sendMessage(Chat.format("&cYou must provide a valid reason for the report."))
                return
            }

            val display = AlchemistAPI.getRankDisplay(player.uniqueId)
            val otherDisplay = AlchemistAPI.getRankDisplay(targetPlayer.uniqueId)
            val server = Alchemist.globalServer.displayName

            AsynchronousRedisSender.send(
                ReportPacket(
                    "&9[Report] &7[$server] &b$display &7has reported &f$otherDisplay\n     &9Reason: &7$rzn",
                    ReportModel(
                        UUID.randomUUID(),
                        rzn,
                        player.uniqueId,
                        targetPlayer.uniqueId,
                        server,
                        System.currentTimeMillis()
                    )
                )
            )

            RequestHandler.reportCooldowns[player.uniqueId] = System.currentTimeMillis()
            player.sendMessage(Chat.format("&aYour report has been successfully sent to online staff members!"))
        } catch (e: Exception) {
            player.sendMessage(Chat.format("&cAn unexpected error occurred while processing your report. Please try again later."))
            e.printStackTrace()
        }
    }
}
