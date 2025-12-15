package ltd.matrixstudios.alchemist.chatsnap.menu

import com.cryptomorin.xseries.XMaterial
import ltd.matrixstudios.alchemist.api.AlchemistAPI
import ltd.matrixstudios.alchemist.chatsnap.ChatCache
import ltd.matrixstudios.alchemist.models.chatsnap.ChatSnap
import ltd.matrixstudios.alchemist.models.profile.GameProfile
import ltd.matrixstudios.alchemist.util.Chat
import ltd.matrixstudios.alchemist.util.menu.Button
import ltd.matrixstudios.alchemist.util.menu.buttons.PlaceholderButton
import ltd.matrixstudios.alchemist.util.menu.buttons.SimpleActionButton
import ltd.matrixstudios.alchemist.util.skull.SkullUtil
import ltd.matrixstudios.alchemist.util.menu.pagination.PaginatedMenu
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import java.text.SimpleDateFormat
import java.util.*

class ChatSnapViewMenu(player: Player, private val snap: ChatSnap, private val targetProfile: GameProfile? = null) :
    PaginatedMenu(27, player) {

    private val dateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault())

    override fun getHeaderItems(player: Player): MutableMap<Int, Button> {
        val buttons = mutableMapOf<Int, Button>()

        val fillerMat = XMaterial.LIGHT_GRAY_STAINED_GLASS_PANE.parseMaterial() ?: Material.STAINED_GLASS_PANE
        for (i in 0 until 35) {
            buttons[i] = PlaceholderButton(fillerMat, mutableListOf(), "", 7)
        }

        val ownerProfile = targetProfile ?: AlchemistAPI.syncFindProfile(snap.owner)
        val ownerName = ownerProfile?.username ?: "Unknown"
        val created = dateFormat.format(Date(snap.createdAt))
        val skullDesc = mutableListOf(
            " ",
            Chat.format("&7Owner: &f$ownerName"),
            Chat.format("&7Created: &f$created"),
            Chat.format("&7Messages: &f${snap.messages.size}"),
            " "
        )

        buttons[4] = object : Button() {
            override fun getButtonItem(player: Player) =
                SkullUtil.generate(ownerName, Chat.format("&6$ownerName's ChatSnap"), skullDesc)

            override fun getMaterial(player: Player) = XMaterial.PLAYER_HEAD.parseMaterial() ?: Material.SKULL_ITEM
            override fun getDescription(player: Player) = skullDesc
            override fun getDisplayName(player: Player) = Chat.format("&6$ownerName's ChatSnap")
            override fun getData(player: Player) = 3.toShort()
            override fun onClick(player: Player, slot: Int, type: ClickType) {}
        }

        return buttons
    }

    override fun getPagesButtons(player: Player): MutableMap<Int, Button> {
        val buttons = mutableMapOf<Int, Button>()
        var slotIndex = 9

        for ((i, msg) in snap.messages.withIndex()) {
            val lore = mutableListOf(
                " ",
                Chat.format("&7Message ${i + 1}"),
                Chat.format("&f$msg"),
                " ",
                Chat.format("&aLeft-click &7to send this line in chat"),
            )

            buttons[slotIndex] = SimpleActionButton(
                Material.PAPER,
                lore,
                "&eLine ${i + 1}",
                0
            ).setBody { p, _, clickType ->
                if (clickType == ClickType.LEFT) {
                    p.closeInventory()
                    p.chat(msg)
                }
            }

            slotIndex++
            if (slotIndex >= 26) break
        }

        if (snap.messages.isEmpty()) {
            buttons[13] = SimpleActionButton(
                Material.PAPER,
                mutableListOf(" ", Chat.format("&cNo messages recorded."), " "),
                "&cEmpty",
                0
            ).setBody { _, _, _ -> }
        }

        return buttons
    }

    override fun getTitle(player: Player): String {
        val ownerProfile = targetProfile ?: AlchemistAPI.syncFindProfile(snap.owner)
        val ownerName = ownerProfile?.username ?: "Unknown"
        val idLabel = if (snap.numericId > 0) "#${snap.numericId}" else "&7(unsaved)"
        return Chat.format("&6ChatSnap &8- &7$idLabel")
    }
}
