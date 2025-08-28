package ltd.matrixstudios.alchemist.punishment.commands.menu.sub

import com.cryptomorin.xseries.XMaterial
import ltd.matrixstudios.alchemist.api.AlchemistAPI
import ltd.matrixstudios.alchemist.models.profile.GameProfile
import ltd.matrixstudios.alchemist.punishment.commands.menu.PunishMenu
import ltd.matrixstudios.alchemist.util.Chat
import ltd.matrixstudios.alchemist.util.items.ItemBuilder
import ltd.matrixstudios.alchemist.util.menu.Button
import ltd.matrixstudios.alchemist.util.menu.Menu
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack
import java.util.concurrent.TimeUnit

class PermanentPunishmentsMenu(
    val target: GameProfile,
    val viewer: Player,
    val reason: String?
) : Menu(viewer) {

    init {
        staticSize = 54
    }

    private val finalReason = reason ?: "Unspecified"

    override fun getTitle(player: Player): String {
        return Chat.format("&4Permanent Punishments")
    }

    override fun getButtons(player: Player): MutableMap<Int, Button> {
        val buttons = mutableMapOf<Int, Button>()

        val permanentBans = mapOf(
            "Cheating" to XMaterial.DIAMOND_SWORD,
            "Malicious Client" to XMaterial.REDSTONE_BLOCK,
            "Exploiting" to XMaterial.TNT,
            "Inappropriate Name" to XMaterial.NAME_TAG,
            "Blacklist Evasion" to XMaterial.BARRIER,
            "Impersonating Staff" to XMaterial.COMMAND_BLOCK
        )

        val permanentMutes = mapOf(
            "Spamming" to XMaterial.LIME_WOOL,
            "Doxing" to XMaterial.PAPER,
            "Advertising" to XMaterial.GOLDEN_APPLE,
            "Toxic Behavior" to XMaterial.WITHER_SKELETON_SKULL
        )

        val blacklists = mapOf(
            "IP Blacklist" to XMaterial.BEDROCK,
            "Repeated Ban Evasion" to XMaterial.NETHERITE_BLOCK,
            "Exploiting" to XMaterial.OBSIDIAN
        )


        buttons.putAll(createPunishmentButtons(permanentBans, "ban", 10, finalReason))
        buttons.putAll(createPunishmentButtons(permanentMutes, "mute", 19, finalReason))
        buttons.putAll(createPunishmentButtons(blacklists, "blacklist", 28, finalReason))

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

    private fun createPunishmentButtons(reasons: Map<String, XMaterial>, punishmentType: String, startSlot: Int, customReason: String): Map<Int, Button> {
        val buttons = mutableMapOf<Int, Button>()
        var slot = startSlot
        for ((reason, material) in reasons) {
            buttons[slot++] = object : Button() {
                private val combinedReason = if (customReason == "Unspecified") reason else "$reason - $customReason"
                private val item = ItemBuilder(material.parseMaterial()!!).name(Chat.format("&c&l$reason")).build()
                private val lore = listOf(
                    "&cThis is a permanent punishment.",
                    "&7Reason: &f$combinedReason",
                    "",
                    "&aClick to punish this player."
                ).map { Chat.format(it) }

                override fun getButtonItem(player: Player): ItemStack {
                    return ItemBuilder.copyOf(item).setLore(lore).build()
                }

                override fun getMaterial(player: Player): Material { return material.parseMaterial()!! }
                override fun getDescription(player: Player): MutableList<String> { return lore.toMutableList() }
                override fun getDisplayName(player: Player): String { return Chat.format("&c&l$reason") }
                override fun getData(player: Player): Short { return 0 }

                override fun onClick(player: Player, slot: Int, type: ClickType) {
                    player.closeInventory()
                    player.performCommand("$punishmentType ${target.username} $combinedReason [P]")
                }
            }
        }
        return buttons
    }
}