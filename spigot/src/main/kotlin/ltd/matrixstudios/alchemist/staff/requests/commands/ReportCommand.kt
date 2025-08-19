package ltd.matrixstudios.alchemist.staff.requests.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.*
import ltd.matrixstudios.alchemist.Alchemist
import ltd.matrixstudios.alchemist.api.AlchemistAPI
import ltd.matrixstudios.alchemist.redis.AsynchronousRedisSender
import ltd.matrixstudios.alchemist.staff.requests.commands.menu.ReportSelectCategoryMenu
import ltd.matrixstudios.alchemist.staff.requests.handlers.RequestHandler
import ltd.matrixstudios.alchemist.staff.requests.packets.ReportPacket
import ltd.matrixstudios.alchemist.models.report.ReportModel
import ltd.matrixstudios.alchemist.models.report.ReportStatus
import ltd.matrixstudios.alchemist.service.reports.ReportIdService
import ltd.matrixstudios.alchemist.service.reports.ReportService
import ltd.matrixstudios.alchemist.staff.requests.commands.menu.ReportActionMenu
import ltd.matrixstudios.alchemist.util.Chat
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.*

@CommandAlias("report")
class ReportCommand : BaseCommand() {

    @Default
    @CommandCompletion("@gameprofile")
    fun onReport(
        player: Player,
        @Name("target") targetName: String,
        @Name("reason") reasonArgs: Array<String>
    ) {
        val target = Bukkit.getPlayer(targetName)
        if (target == null) {
            player.sendMessage(Chat.format("&cThat player is not online."))
            return
        }

        if (target.uniqueId == player.uniqueId) {
            player.sendMessage(Chat.format("&cYou cannot report yourself!"))
            return
        }

        val reason = reasonArgs.joinToString(" ")

        val currentServer = Alchemist.globalServer.displayName
        val display = AlchemistAPI.getRankDisplay(player.uniqueId)
        val otherDisplay = AlchemistAPI.getRankDisplay(target.uniqueId)

        val report = ReportModel(
            UUID.randomUUID(),
            reason,
            player.uniqueId,
            target.uniqueId,
            currentServer,
            System.currentTimeMillis(),
            ReportStatus.OPEN
        ).apply {
            numericId = ReportIdService.nextId()
        }






        AsynchronousRedisSender.send(
            ReportPacket(
                "&9[Report] &7[$currentServer] &b$display &7has reported &f$otherDisplay\n     &9Reason: &7$reason+\n &9Status: &7$report.status",
                report
            )
        )

        ReportService.save(report)

        player.sendMessage(Chat.format("&aReport #${report.numericId} has been submitted."))



        RequestHandler.reportCooldowns[player.uniqueId] = System.currentTimeMillis()
        player.sendMessage(Chat.format("&aYour report has been sent to every online staff member!"))

    }

    @CommandAlias("reports|viewreports")
    @CommandPermission("alchemist.staff")
    fun viewReports(player: Player)
    {
        ReportSelectCategoryMenu(player).openMenu()
    }
//TODO REGISTER THIS CMD

}
