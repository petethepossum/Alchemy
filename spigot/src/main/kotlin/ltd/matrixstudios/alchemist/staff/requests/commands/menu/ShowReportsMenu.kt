package ltd.matrixstudios.alchemist.staff.requests.commands.menu

import ltd.matrixstudios.alchemist.api.AlchemistAPI
import ltd.matrixstudios.alchemist.models.report.ReportModel
import ltd.matrixstudios.alchemist.staff.requests.commands.menu.ReportActionMenu.Companion.formatStatus
import ltd.matrixstudios.alchemist.util.Chat
import ltd.matrixstudios.alchemist.util.menu.Button
import ltd.matrixstudios.alchemist.util.menu.type.BorderedPaginatedMenu
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemFlag
import java.util.*

class ShowReportsMenu(
    val player: Player,
    val allReports: MutableList<ReportModel>,
    var showArchived: Boolean = true
) : BorderedPaginatedMenu(player) {

    override fun getTitle(player: Player): String {
        return "Viewing Active Reports"
    }

    override fun getPagesButtons(player: Player): MutableMap<Int, Button> {
        val buttons = mutableMapOf<Int, Button>()
        val visibleReports = allReports

        visibleReports.forEachIndexed { i, report ->
            buttons[i] = ReportsButton(report)
        }

        return buttons
    }



    class ReportsButton(val model: ReportModel) : Button() {
        override fun getMaterial(player: Player): Material {
            return if (model.isArchived) Material.CLAY_BALL else Material.PAPER
        }
        override fun getData(player: Player): Short {
            return 0
        }


        override fun getDisplayName(player: Player): String {
            val prefix = if (model.isArchived) "&7[Archived] " else "&a"
            return Chat.format("$prefix${Date(model.issuedAt)}")
        }

        override fun getDescription(player: Player): MutableList<String> {
            return mutableListOf(
                Chat.format("&8Report ID: #${model.numericId}"),
                " ",
                Chat.format("&eReason: &f${model.reason}"),
                Chat.format("&eIssued On: &f${model.server}"),
                Chat.format("&eIssuer: &f${AlchemistAPI.getRankDisplay(model.issuer)}"),
                Chat.format("&eIssued To: &f${AlchemistAPI.getRankDisplay(model.issuedTo)}"),
                Chat.format("&eStatus: &f${formatStatus(model.status)}"),
                " ",
                Chat.format("&aClick to open the Report Handler"),
                Chat.format("&c&oRight-Click to teleport to the reported player")
            )
        }

        override fun getButtonItem(player: Player): ItemStack {
            val item = ItemStack(getMaterial(player))
            val meta = item.itemMeta
            meta?.displayName = getDisplayName(player)
            meta?.lore = getDescription(player)
            if (model.isArchived) {
                meta?.addEnchant(Enchantment.DURABILITY, 1, true)
                meta?.addItemFlags(ItemFlag.HIDE_ENCHANTS)
            }
            item.itemMeta = meta
            return item
        }

        override fun onClick(player: Player, slot: Int, type: ClickType) {
            ReportActionMenu(player, model).openMenu()
            if (type.isRightClick) {
                val target = Bukkit.getPlayer(model.issuedTo)
                if (target != null) {
                    player.performCommand("jtp ${target.name}")
                    player.sendMessage(Chat.format("&aTeleported to ${target.name}"))
                } else {
                    player.sendMessage(Chat.format("&cPlayer is not online."))
                }
            }
        }
    }
}