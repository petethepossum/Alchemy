package ltd.matrixstudios.alchemist.punishment.commands.menu.sub

import com.cryptomorin.xseries.XMaterial
import ltd.matrixstudios.alchemist.api.AlchemistAPI
import ltd.matrixstudios.alchemist.models.profile.GameProfile
import ltd.matrixstudios.alchemist.punishment.commands.menu.PunishMenu
import ltd.matrixstudios.alchemist.util.Chat
import ltd.matrixstudios.alchemist.util.items.ItemBuilder
import ltd.matrixstudios.alchemist.util.menu.Button
import ltd.matrixstudios.alchemist.util.menu.Menu
import ltd.matrixstudios.alchemist.util.TimeUtil
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack
import java.util.concurrent.TimeUnit

class TemporaryPunishmentsMenu(
    val target: GameProfile,
    val viewer: Player,
    val reason: String?
) : Menu(viewer) {

    init {
        staticSize = 54
    }

    private val finalReason = reason ?: "Unspecified"

    override fun getTitle(player: Player): String {
        return Chat.format("&eTemporary Punishments")
    }

    override fun getButtons(player: Player): MutableMap<Int, Button> {
        val buttons = mutableMapOf<Int, Button>()

        val temporaryMutes = listOf(
            Triple("Spamming", TimeUnit.HOURS.toMillis(1), XMaterial.PAPER),
            Triple("Harassment", TimeUnit.DAYS.toMillis(1), XMaterial.BOOK),
            Triple("General Chat Offence", TimeUnit.DAYS.toMillis(7), XMaterial.WRITABLE_BOOK),
            Triple("Inappropriate Links", TimeUnit.DAYS.toMillis(30), XMaterial.MAP),
            Triple("Advertising", TimeUnit.DAYS.toMillis(90), XMaterial.OAK_SIGN)
        )

        val temporaryBans = listOf(
            Triple("X-Ray", TimeUnit.DAYS.toMillis(7), XMaterial.IRON_ORE),
            Triple("Fly Hacking", TimeUnit.DAYS.toMillis(14), XMaterial.FEATHER),
            Triple("Aura/Killaura", TimeUnit.DAYS.toMillis(30), XMaterial.IRON_SWORD),
            Triple("Griefing", TimeUnit.DAYS.toMillis(90), XMaterial.DIAMOND_PICKAXE),
            Triple("Logged out whilst frozen", TimeUnit.DAYS.toMillis(30), XMaterial.IRON_BARS)
        )

        var muteSlot = 10
        buttons.putAll(createTempPunishmentButtons(temporaryMutes, "tempmute", muteSlot, finalReason))

        var banSlot = 19
        buttons.putAll(createTempPunishmentButtons(temporaryBans, "tempban", banSlot, finalReason))

        buttons[49] = PunishMenu.SimpleButton(
            ItemBuilder(XMaterial.ARROW.parseMaterial()!!).name(Chat.format("&cBack to main menu")).build(),
            "&cBack to main menu",
            emptyList()
        ) {
            viewer.closeInventory()
            PunishMenu(target, viewer, reason).openMenu()
        }

        return buttons
    }

    private fun convertMillisToCommandString(millis: Long): String {
        val days = TimeUnit.MILLISECONDS.toDays(millis)
        val hours = TimeUnit.MILLISECONDS.toHours(millis)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millis)

        return when {
            days > 0 -> "${days}d"
            hours > 0 -> "${hours}h"
            minutes > 0 -> "${minutes}m"
            else -> "1m"
        }
    }

    private fun createTempPunishmentButtons(reasons: List<Triple<String, Long, XMaterial>>, punishmentType: String, startSlot: Int, customReason: String): Map<Int, Button> {
        val buttons = mutableMapOf<Int, Button>()
        var slot = startSlot
        for ((reason, duration, material) in reasons) {
            buttons[slot++] = object : Button() {
                private val combinedReason = if (customReason == "Unspecified") reason else "$reason - $customReason"
                private val formattedDuration = TimeUtil.formatDuration(duration)
                private val commandDuration = convertMillisToCommandString(duration)
                private val item = ItemBuilder(material.parseMaterial()!!).name(Chat.format("&e&l$reason")).build()
                private val lore = listOf(
                    "&7Duration: &f$formattedDuration",
                    "&7Reason: &f$combinedReason",
                    "",
                    "&aClick to punish this player."
                ).map { Chat.format(it) }

                override fun getButtonItem(player: Player): ItemStack {
                    return ItemBuilder.copyOf(item).setLore(lore).build()
                }

                override fun getMaterial(player: Player): Material { return material.parseMaterial()!! }
                override fun getDescription(player: Player): MutableList<String> { return lore.toMutableList() }
                override fun getDisplayName(player: Player): String { return Chat.format("&e&l$reason") }
                override fun getData(player: Player): Short { return 0 }

                override fun onClick(player: Player, slot: Int, type: ClickType) {
                    player.closeInventory()
                    player.performCommand("$punishmentType ${target.username} $commandDuration $combinedReason [P]")
                }
            }
        }
        return buttons
    }
}