package ltd.matrixstudios.alchemist.staff.requests.commands.menu

import ltd.matrixstudios.alchemist.Alchemist
import ltd.matrixstudios.alchemist.api.AlchemistAPI
import ltd.matrixstudios.alchemist.models.report.ReportModel
import ltd.matrixstudios.alchemist.models.report.ReportStatus
import ltd.matrixstudios.alchemist.packets.StaffGeneralMessagePacket
import ltd.matrixstudios.alchemist.redis.AsynchronousRedisSender
import ltd.matrixstudios.alchemist.service.reports.ReportService
import ltd.matrixstudios.alchemist.util.Chat
import ltd.matrixstudios.alchemist.util.InputPrompt
import ltd.matrixstudios.alchemist.util.menu.Button
import ltd.matrixstudios.alchemist.util.menu.Menu
import ltd.matrixstudios.alchemist.util.menu.buttons.SimpleActionButton
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player

class ReportActionMenu(val player: Player, val report: ReportModel) : Menu(player) {
    init {
        staticSize = 27
        placeholder = true
    }

    companion object {
        fun formatStatus(status: ReportStatus): String {
            return when (status) {
                ReportStatus.OPEN -> Chat.format("&aOPEN")
                ReportStatus.IN_PROGRESS -> Chat.format("&eIN PROGRESS")
                ReportStatus.RESOLVED -> Chat.format("&cRESOLVED")
                ReportStatus.CLOSED -> Chat.format("&c&l&nCLOSED")
            }
        }
    }

    override fun getTitle(player: Player): String {
        val status = formatStatus(report.status)
        return if (report.handledBy == player.uniqueId) {
            "§lHandling Report #${report.numericId} §7[$status]"
        } else {
            "Report #${report.numericId} §7[$status]"
        }
    }

    override fun getButtons(player: Player): MutableMap<Int, Button> {
        val reporterName = Bukkit.getOfflinePlayer(report.issuer).name ?: "Unknown"
        val reportedName = Bukkit.getOfflinePlayer(report.issuedTo).name ?: "Unknown"
        val handlerName = report.handledBy?.let { Bukkit.getOfflinePlayer(it).name } ?: "None"
        val isHandler = report.handledBy == player.uniqueId

        val buttons: MutableMap<Int, Button> = mutableMapOf()

        val handlerButton = SimpleActionButton(
            Material.NAME_TAG,
            mutableListOf(Chat.format("&7Assign or unassign yourself as handler \n ${if (isHandler) "&cClick to stop handling" else "&aClick to start handling"} this report")),
            "&dToggle Handler",
            0.toShort()
        ).setBody { _, _, _ ->
            report.handledBy = if (isHandler) null else player.uniqueId
            ReportService.save(report)
            val msg = if (report.handledBy == null) "&cYou are no longer handling this report"
            else "&aYou are now handling this report"
            player.sendMessage(Chat.format(msg))
            openMenu()
        }

        if (isHandler) {
            handlerButton.glow()
        }

        buttons[4] = SimpleActionButton(
            Material.PAPER,
            mutableListOf(
                Chat.format("&7Reporter: &f${AlchemistAPI.getRankDisplay(report.issuer)}"),
                Chat.format("&7Reported: &f${AlchemistAPI.getRankDisplay(report.issuedTo)}"),
                Chat.format("&7Server: &f${report.server}"),
                Chat.format("&7Status: &f${formatStatus(report.status)}"),
                Chat.format("&7Handler: &f$handlerName")
            ),
            "&bReport Info",
            0
        ).setBody { _, _, _ -> }

        buttons[11] = SimpleActionButton(
            Material.EMERALD,
            mutableListOf(
                Chat.format("&7Cycle report status"),
                Chat.format("&7Current: ${formatStatus(report.status)}")
            ),
            formatStatus(report.status),
            0
        ).setBody { _, _, _ ->
            report.status = when (report.status) {
                ReportStatus.OPEN -> ReportStatus.IN_PROGRESS
                ReportStatus.IN_PROGRESS -> ReportStatus.RESOLVED
                ReportStatus.RESOLVED -> ReportStatus.OPEN
                ReportStatus.CLOSED -> ReportStatus.OPEN
            }
            ReportService.save(report)
            player.sendMessage(Chat.format("&aStatus of #${report.numericId} updated to ${formatStatus(report.status)}"))
            openMenu()
        }

        buttons[13] = handlerButton

        buttons[15] = SimpleActionButton(
            Material.BOOK_AND_QUILL,
            mutableListOf(Chat.format("&7Add a note or comment to this report")),
            "&eAdd Note",
            0
        ).setBody { _, _, _ ->
            InputPrompt()
                .withText(Chat.format("&eType your note for this report:"))
                .acceptInput { note ->
                    report.notes.add("[${player.name}] $note")
                    ReportService.save(report)
                    player.sendMessage(Chat.format("&aNote added to report"))
                    openMenu()
                }.start(player)
        }

        buttons[17] = SimpleActionButton(
            Material.CHEST,
            report.notes.mapIndexed { i, note -> Chat.format("&7${i + 1}. $note") }.take(5).toMutableList(),
            "&6View Notes",
            0
        ).setBody { _, _, _ ->
            player.sendMessage(Chat.format("&eShowing first ${report.notes.size.coerceAtMost(5)} notes:"))
            report.notes.take(5).forEachIndexed { i, note ->
                player.sendMessage(Chat.format("&7${i + 1}. &f$note"))
            }
        }

        buttons[26] = SimpleActionButton(
            Material.BARRIER,
            mutableListOf(
                Chat.format("&cClose this report so it no longer appears in the active list"),
                Chat.format("&7You can reopen it later from this menu")
            ),
            "&4Close Report",
            0
        ).setBody { _, _, _ ->
            report.status = ReportStatus.CLOSED
            ReportService.save(report)
            player.sendMessage(Chat.format("&cReport #${report.numericId} has been &cclosed."))
            val server = Alchemist.globalServer.displayName
            val displayExec = AlchemistAPI.getRankDisplay(player.uniqueId)
            AsynchronousRedisSender.send(StaffGeneralMessagePacket("&b[S] &3[$server] $displayExec &3has closed Report &b#${report.numericId}"))
            player.closeInventory()
        }

        if (report.status == ReportStatus.CLOSED) {
            val reopenButton = SimpleActionButton(
                Material.ENDER_PEARL,
                mutableListOf(Chat.format("&7Reopen this report to make it active again")),
                "&aReopen Report",
                0
            ).setBody { _, _, _ ->
                InputPrompt()
                    .withText(Chat.format("&eType &aCONFIRM &eto reopen this report."))
                    .acceptInput { input ->
                        if (input.equals("CONFIRM", ignoreCase = true)) {
                            report.status = ReportStatus.OPEN
                            ReportService.save(report)
                            player.sendMessage(Chat.format("&aReport #${report.numericId} has been reopened."))
                            openMenu()
                        } else {
                            player.sendMessage(Chat.format("&cReopen cancelled."))
                        }
                    }.start(player)
            }
            reopenButton.glow()
            buttons[25] = reopenButton
        }

        return buttons
    }
}