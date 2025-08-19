package ltd.matrixstudios.alchemist.staff.requests.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.*
import ltd.matrixstudios.alchemist.Alchemist
import ltd.matrixstudios.alchemist.api.AlchemistAPI
import ltd.matrixstudios.alchemist.models.report.ReportModel
import ltd.matrixstudios.alchemist.models.report.ReportStatus
import ltd.matrixstudios.alchemist.packets.StaffGeneralMessagePacket
import ltd.matrixstudios.alchemist.redis.AsynchronousRedisSender
import ltd.matrixstudios.alchemist.service.reports.ReportService
import ltd.matrixstudios.alchemist.staff.requests.commands.menu.ReportActionMenu
import ltd.matrixstudios.alchemist.util.Chat
import org.bukkit.entity.Player

@CommandAlias("reporthandle|rh")
@CommandPermission("alchemist.staff")
class ReportHandleCommand : BaseCommand() {

    @Default
    @CommandCompletion("@reportIds")
    @Syntax("<reportId>")
    fun handleReport(player: Player, @Name("reportId") reportId: Int) {
        val report = ReportService.getAll().get().find { it.numericId == reportId }

        if (report == null) {
            player.sendMessage(Chat.format("&cNo report found with ID #$reportId"))
            return
        }

        ReportActionMenu(player, report).openMenu()
    }
}

@CommandAlias("reportinfo|ri")
@CommandPermission("alchemist.staff")
class ReportInfoCommand : BaseCommand() {

    @Default
    @CommandCompletion("@reportIds")
    @Syntax("<reportId>")
    fun reportInfo(player: Player, @Name("reportId") reportId: Int) {
        val report = ReportService.getAll().get().find { it.numericId == reportId }

        if (report == null) {
            player.sendMessage(Chat.format("&cNo report found with ID #$reportId"))
            return
        }

        player.sendMessage(Chat.format("&aReport Info:"))
        player.sendMessage(Chat.format("&7ID: &f#${report.numericId}"))
        player.sendMessage(Chat.format("&7Reason: &f${report.reason}"))
        player.sendMessage(Chat.format("&7Reporter: &f${AlchemistAPI.getRankDisplay(report.issuer)}"))
        player.sendMessage(Chat.format("&7Target: &f${AlchemistAPI.getRankDisplay(report.issuedTo)}"))
        player.sendMessage(Chat.format("&7Status: &f${report.status.name}"))
    }
}

@CommandAlias("reportclose|rc")
@CommandPermission("alchemist.staff")
class ReportCloseCommand : BaseCommand() {

    @Default
    @CommandCompletion("@reportIds")
    @Syntax("<reportId>")
    fun closeReport(player: Player, @Name("reportId") reportId: Int) {
        val report = ReportService.getAll().get().find { it.numericId == reportId }

        if (report == null) {
            player.sendMessage(Chat.format("&cNo report found with ID #$reportId"))
            return
        }

        if (report.status == ReportStatus.CLOSED) {
            player.sendMessage(Chat.format("&cReport #$reportId is already closed."))
            return
        }

        report.status = ReportStatus.CLOSED
        ReportService.save(report)
        player.sendMessage(Chat.format("&aReport #$reportId has been closed."))

        val server = Alchemist.globalServer.displayName
        val displayExec = AlchemistAPI.getRankDisplay(player.uniqueId)
        AsynchronousRedisSender.send(
            StaffGeneralMessagePacket("&b[S] &3[$server] $displayExec &3has closed Report &b#${report.numericId}")
        )
    }
}