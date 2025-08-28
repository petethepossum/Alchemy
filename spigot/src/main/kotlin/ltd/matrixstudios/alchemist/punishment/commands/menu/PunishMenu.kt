package ltd.matrixstudios.alchemist.punishment.commands.menu

import com.cryptomorin.xseries.XMaterial
import ltd.matrixstudios.alchemist.api.AlchemistAPI
import ltd.matrixstudios.alchemist.models.profile.GameProfile
import ltd.matrixstudios.alchemist.punishment.commands.menu.sub.PermanentPunishmentsMenu
import ltd.matrixstudios.alchemist.punishment.commands.menu.sub.TemporaryPunishmentsMenu
import ltd.matrixstudios.alchemist.service.profiles.ProfileGameService
import ltd.matrixstudios.alchemist.util.Chat
import ltd.matrixstudios.alchemist.util.TimeUtil
import ltd.matrixstudios.alchemist.util.items.ItemBuilder
import ltd.matrixstudios.alchemist.util.menu.Button
import ltd.matrixstudios.alchemist.util.menu.Menu
import ltd.matrixstudios.alchemist.util.skull.SkullUtil
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack
import java.util.concurrent.TimeUnit

class PunishMenu(
    val target: GameProfile,
    val viewer: Player,
    val reason: String?
) : Menu(viewer) {

    init {
        staticSize = 45
    }

    companion object {
        val FILLER_GLASS: ItemStack = ItemBuilder(XMaterial.BLACK_STAINED_GLASS_PANE.parseMaterial()!!)
            .name(" ")
            .build()
    }

    override fun getTitle(player: Player): String {
        return Chat.format("&7Punishing: ${AlchemistAPI.getRankDisplay(target.uuid)}")
    }

    override fun getButtons(player: Player): MutableMap<Int, Button> {
        val buttons = mutableMapOf<Int, Button>()
        val finalReason = reason ?: "Unspecified"

        for (i in 0 until staticSize!!) {
            if (i < 10 || i > 34 || i % 9 == 0 || (i + 1) % 9 == 0) {
                buttons[i] = FillerButton()
            }
        }

        buttons[4] = SkullButton(target)


        buttons[21] = SimpleButton(
            XMaterial.REDSTONE_BLOCK.parseItem()!!,
            "&c&lPermanent Punishments",
            listOf("&7Click to view permanent ban and", "&7blacklist options.", "", "&7Reason: &f$finalReason")
        ) {
            viewer.closeInventory()
            PermanentPunishmentsMenu(target, viewer, reason).openMenu()
        }


        buttons[23] = SimpleButton(
            XMaterial.IRON_SWORD.parseItem()!!,
            "&e&lTemporary Punishments",
            listOf("&7Click to view temporary ban and", "&7mute options with pre-set durations.", "", "&7Reason: &f$finalReason")
        ) {
            viewer.closeInventory()
            TemporaryPunishmentsMenu(target, viewer, reason).openMenu()
        }

        buttons[38] = SimpleButton(
            XMaterial.OAK_SIGN.parseItem()!!,
            "&e&lWarn Player",
            listOf("&7Issue a warning.", "&7Reason: &f$finalReason")
        ) { it.performCommand("warn ${target.username} $finalReason") }

        buttons[40] = SimpleButton(
            XMaterial.BOOK.parseItem()!!,
            "&b&lPunishment History &7AP: &7(&f${target.getActivePunishments().size}&7)",
            listOf("&7View ${target.username}'s past punishments.")
        ) { it.performCommand("history ${target.username}") }

        buttons[42] = SimpleButton(
            XMaterial.WRITABLE_BOOK.parseItem()!!,
            "&d&lNotes",
            listOf("&7View or add notes for this player.")
        ) { it.performCommand("notes ${target.username}") }

        return buttons
    }

    private class FillerButton : Button() {
        override fun getButtonItem(player: Player): ItemStack {
            return FILLER_GLASS
        }

        override fun getMaterial(player: Player): Material { return FILLER_GLASS.type }
        override fun getDescription(player: Player): MutableList<String>? { return mutableListOf() }
        override fun getDisplayName(player: Player): String? { return " " }
        override fun getData(player: Player): Short { return FILLER_GLASS.durability }

        override fun onClick(player: Player, slot: Int, type: ClickType) {}
    }

    private class SkullButton(val target: GameProfile) : Button() {

        val profile = ProfileGameService.byId(target.uuid)
        val playtime = TimeUtil.formatDuration(profile?.playtimeMillis ?: 0L)
        val currentserver = profile?.getRedisServerDisplay()

        override fun getButtonItem(player: Player): ItemStack {
            val name = Chat.format("&ePunishing " + AlchemistAPI.getRankDisplay(target.uuid))
            val loreString = Chat.format("&eRank: " + AlchemistAPI.getPlayerRankString(target.uuid)) +
                    "\n" + Chat.format("&ePlay Time: &f${playtime}") +
                    "\n" + Chat.format("&eCurrent Server: &f${currentserver}")

            val loreList = loreString.split("\n")
            return SkullUtil.generate(target.username, name, loreList)
        }

        override fun getMaterial(player: Player): Material { return Material.SKULL_ITEM }
        override fun getDescription(player: Player): MutableList<String>? { return mutableListOf() }
        override fun getDisplayName(player: Player): String { return Chat.format("&e${target.username}") }
        override fun getData(player: Player): Short { return 3 }

        override fun onClick(player: Player, slot: Int, type: ClickType) {}
    }

    class SimpleButton(
        private val item: ItemStack,
        private val name: String,
        private val lore: List<String> = emptyList(),
        private val action: (Player) -> Unit
    ) : Button() {

        override fun getButtonItem(player: Player): ItemStack {
            return ItemBuilder.copyOf(item)
                .name(Chat.format(name))
                .setLore(lore.map { Chat.format(it) })
                .build()
        }

        override fun getMaterial(player: Player): Material { return item.type }
        override fun getDescription(player: Player): MutableList<String> { return lore.map { Chat.format(it) }.toMutableList() }
        override fun getDisplayName(player: Player): String { return Chat.format(name) }
        override fun getData(player: Player): Short { return item.durability }

        override fun onClick(player: Player, slot: Int, type: ClickType) {
            player.closeInventory()
            action.invoke(player)
        }
    }
}