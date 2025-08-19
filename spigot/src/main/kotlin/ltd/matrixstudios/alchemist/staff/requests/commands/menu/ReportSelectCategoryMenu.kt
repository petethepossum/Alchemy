package ltd.matrixstudios.alchemist.staff.requests.commands.menu

import ltd.matrixstudios.alchemist.Alchemist
import ltd.matrixstudios.alchemist.models.report.ReportModel
import ltd.matrixstudios.alchemist.service.reports.ReportService
import ltd.matrixstudios.alchemist.staff.requests.handlers.RequestHandler
import ltd.matrixstudios.alchemist.util.Chat
import ltd.matrixstudios.alchemist.util.InputPrompt
import ltd.matrixstudios.alchemist.util.menu.Button
import ltd.matrixstudios.alchemist.util.menu.Menu
import ltd.matrixstudios.alchemist.util.menu.buttons.SimpleActionButton
import ltd.matrixstudios.alchemist.models.report.ReportStatus

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import java.util.concurrent.TimeUnit

class ReportSelectCategoryMenu(val player: Player) : Menu(player)
{
    init
    {
        staticSize = 18
        placeholder = true
    }
//this is messy should change it
override fun getButtons(player: Player): MutableMap<Int, Button> {
    val allReports = ReportService.getAll().get()
    val activeReports = allReports.filter { !it.isArchived }

    val buttons = mutableMapOf<Int, Button>()

    fun createCategoryButton(
        slot: Int,
        material: Material,
        name: String,
        lore: List<String>,
        filter: (ReportModel) -> Boolean
    ) {
        val reports = activeReports.filter(filter).toMutableList()
        buttons[slot] = SimpleActionButton(material, lore.map(Chat::format).toMutableList(), name, 0.toShort())
            .setBody { player, _, _ -> ShowReportsMenu(player, reports).updateMenu() }
    }

    createCategoryButton(
        0,
        Material.ANVIL,
        "&aAll Reports (&f${activeReports.size}&a)",
        listOf("&7View all reports across all servers"),
        { true }
    )

    createCategoryButton(
        2,
        Material.BOOK,
        "&eCurrent Server",
        listOf("&7View reports from your current server, {$Alchemist.globalServer.displayName}"),
        { it.server.equals(Alchemist.globalServer.displayName, ignoreCase = true) }
    )

    createCategoryButton(
        4,
        Material.NETHER_STAR,
        "&bPast Hour",
        listOf("&7Reports issued in the past hour"),
        { it.issuedAt >= System.currentTimeMillis() - TimeUnit.HOURS.toMillis(1) }
    )

    createCategoryButton(
        6,
        Material.ARROW,
        "&cReported Player is Online",
        listOf("&7Reports where the reported player is currently online"),
        { Bukkit.getPlayer(it.issuedTo) != null }
    )

    val openReports = activeReports.filter { it.status == ReportStatus.OPEN }.toMutableList()
    val openReportsButton = SimpleActionButton(
        Material.COMPASS,
        mutableListOf(Chat.format("&7View only reports that are currently open")),
        "&dOnly Open Reports",
        0.toShort()
    ).setBody { player, _, _ -> ShowReportsMenu(player, openReports).updateMenu() }

    if (openReports.isNotEmpty()) {
        openReportsButton.glow()
    }

    buttons[8] = openReportsButton


    return buttons
}




    override fun getTitle(player: Player): String
    {
        return "Select a Report Category"
    }
}