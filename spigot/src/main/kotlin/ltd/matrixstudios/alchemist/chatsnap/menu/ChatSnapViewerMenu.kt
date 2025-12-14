package ltd.matrixstudios.alchemist.chatsnap.menu
import com.cryptomorin.xseries.XMaterial
import ltd.matrixstudios.alchemist.api.AlchemistAPI
import ltd.matrixstudios.alchemist.models.chatsnap.ChatSnap
import ltd.matrixstudios.alchemist.models.profile.GameProfile
import ltd.matrixstudios.alchemist.util.Chat
import ltd.matrixstudios.alchemist.util.menu.Button
import ltd.matrixstudios.alchemist.util.menu.buttons.SimpleActionButton
import ltd.matrixstudios.alchemist.util.skull.SkullUtil
import ltd.matrixstudios.alchemist.util.menu.pagination.PaginatedMenu
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import java.text.SimpleDateFormat
import java.util.*
import ltd.matrixstudios.alchemist.util.menu.Menu

class ChatSnapViewerMenu(
    player: Player,
    private val snaps: List<ChatSnap>,
    private val targetProfile: GameProfile? = null
) : PaginatedMenu(27, player) {

    private val dateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault())

    override fun getHeaderItems(player: Player): MutableMap<Int, Button> {
        val buttons = mutableMapOf<Int, Button>()

        val ownerUuid = snaps.firstOrNull()?.owner
        val ownerProfile = targetProfile ?: ownerUuid?.let { AlchemistAPI.syncFindProfile(it) }
        val ownerName = ownerProfile?.username ?: "Unknown"

        val skullDesc = mutableListOf(" ", Chat.format("&7Viewing chat snaps for &f$ownerName"), " ")

        buttons[4] = object : Button() {
            override fun getButtonItem(player: Player) =
                SkullUtil.generate(ownerName, Chat.format("&6$ownerName's chat snaps"), skullDesc)

            override fun getMaterial(player: Player) = XMaterial.PLAYER_HEAD.parseMaterial() ?: Material.SKULL_ITEM
            override fun getDescription(player: Player) = skullDesc
            override fun getDisplayName(player: Player) = Chat.format("&6$ownerName's chat snaps")
            override fun getData(player: Player) = 3.toShort()
            override fun onClick(player: Player, slot: Int, type: ClickType) {}
        }

        return buttons
    }

    override fun getPagesButtons(player: Player): MutableMap<Int, Button> {
        val buttons = mutableMapOf<Int, Button>()
        var index = 0

        for (snap in snaps) {
            val created = dateFormat.format(Date(snap.createdAt))
            val firstLine = snap.messages.firstOrNull() ?: ""
            val lore = mutableListOf(
                Chat.format("&6&m---------------------------"),
                Chat.format("&eID: &f#${snap.numericId} &7(${created})"),
                Chat.format("&eMessages: &f${snap.messages.size}"),
                Chat.format("&6&m---------------------------"),
                Chat.format("&7Preview:"),
                Chat.format("&f$firstLine"),
                " ",
                Chat.format("&a&lLeft-Click &7to view full snap")
            )

            val button = SimpleActionButton(Material.PAPER, lore, "&eChat snap #${snap.numericId}", 0)
                .setBody { viewer, slot, clickType ->
                    if (clickType == ClickType.LEFT) {
                        ChatSnapViewMenu(viewer, snap, targetProfile).updateMenu()
                    }
                }

            buttons[index++] = button
        }

        return buttons
    }

    override fun getTitle(player: Player): String {
        val ownerUuid = snaps.firstOrNull()?.owner
        val ownerProfile = targetProfile ?: ownerUuid?.let { AlchemistAPI.syncFindProfile(it) }
        val ownerName = ownerProfile?.username ?: "Unknown"
        return Chat.format(AlchemistAPI.getRankDisplay(player.uniqueId) + "&6's Chatsnap")
    }
}
