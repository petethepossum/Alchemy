package ltd.matrixstudios.alchemist.settings.menu

import com.cryptomorin.xseries.XSound
import ltd.matrixstudios.alchemist.models.profile.GameProfile
import ltd.matrixstudios.alchemist.util.Chat
import ltd.matrixstudios.alchemist.util.items.ItemBuilder
import ltd.matrixstudios.alchemist.util.menu.Button
import ltd.matrixstudios.alchemist.util.menu.Menu
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType

class CategoryMenu(
    private val profile: GameProfile,
    player: Player
) : Menu(player) {

    private val categories: List<String> = profile.allProfileSettings().toList().sorted()

    override fun getTitle(player: Player) = Chat.format("&6Settings Categories")

    override fun size(buttons: Map<Int, Button>): Int {
        val count = categories.size.coerceAtLeast(1)
        return ((count - 1) / 9 + 1) * 9
    }

    override fun getButtons(player: Player): MutableMap<Int, Button> {
        val buttons = mutableMapOf<Int, Button>()

        if (categories.isEmpty()) {
            buttons[13] = object : Button() {
                override fun getMaterial(player: Player) = Material.BARRIER

                override fun getDisplayName(player: Player) =
                    Chat.format("&cNo Settings Categories")

                override fun getDescription(player: Player) = mutableListOf(
                    Chat.format("&7You don't have any settings categories yet.")
                )

                override fun getButtonItem(player: Player) = ItemBuilder.of(Material.BARRIER)
                    .name(getDisplayName(player))
                    .setLore(getDescription(player))
                    .build()

                override fun getData(player: Player): Short = 0

                override fun onClick(player: Player, slot: Int, type: ClickType) {
                    // no-op
                }
            }
            return buttons
        }

        categories.forEachIndexed { index, category ->
            buttons[index] = object : Button() {
                override fun getMaterial(player: Player) = Material.PAPER

                override fun getDisplayName(player: Player) =
                    Chat.format("&e$category")

                override fun getDescription(player: Player) = mutableListOf(
                    Chat.format("&7Click to open &e$category &7settings")
                )

                override fun getButtonItem(player: Player) = ItemBuilder.of(Material.PAPER)
                    .name(getDisplayName(player))
                    .setLore(getDescription(player))
                    .build()

                override fun getData(player: Player): Short = 0

                override fun onClick(player: Player, slot: Int, type: ClickType) {
                    CategorySettingsMenu(profile, player, category, this@CategoryMenu).openMenu()
                }
            }
        }

        return buttons
    }
}


class CategorySettingsMenu(
    private val profile: GameProfile,
    private val player: Player,
    private val category: String,
    private val parentMenu: Menu? = null
) : Menu(player) {

    private val settingsKeys: List<String> = profile
        .getAllSettingsForCategory(category)
        .toList()
        .sorted()

    override fun getTitle(player: Player) = Chat.format("&6$category Settings")

    // IMPORTANT: override the framework size() so your dynamic sizing is actually used
    override fun size(buttons: Map<Int, Button>): Int {
        // include back button slot if parent exists
        val count = settingsKeys.size + if (parentMenu != null) 1 else 0
        val rows = ((count - 1).coerceAtLeast(0) / 9 + 1)
        return rows * 9
    }

    override fun getButtons(player: Player): MutableMap<Int, Button> {
        val buttons = mutableMapOf<Int, Button>()

        // Back button (if parent exists)
        parentMenu?.let {
            buttons[0] = object : Button() {
                override fun getMaterial(player: Player) = Material.ARROW

                override fun getDisplayName(player: Player) =
                    Chat.format("&cBack")

                override fun getDescription(player: Player) =
                    mutableListOf(Chat.format("&7Return to categories"))

                override fun getButtonItem(player: Player) = ItemBuilder.of(Material.ARROW)
                    .name(getDisplayName(player))
                    .setLore(getDescription(player))
                    .build()

                override fun getData(player: Player): Short = 0

                override fun onClick(player: Player, slot: Int, type: ClickType) {
                    parentMenu.openMenu()
                }
            }
        }

        val startIndex = if (parentMenu != null) 1 else 0

        // No keys in this category
        if (settingsKeys.isEmpty()) {
            buttons[startIndex] = object : Button() {
                override fun getMaterial(player: Player) = Material.BARRIER

                override fun getDisplayName(player: Player) =
                    Chat.format("&cNo settings in this category")

                override fun getDescription(player: Player) =
                    mutableListOf(Chat.format("&7This category has no toggleable settings yet."))

                override fun getButtonItem(player: Player) = ItemBuilder.of(Material.BARRIER)
                    .name(getDisplayName(player))
                    .setLore(getDescription(player))
                    .build()

                override fun getData(player: Player): Short = 0

                override fun onClick(player: Player, slot: Int, type: ClickType) {
                    // no-op
                }
            }
            return buttons
        }

        // Toggle buttons
        settingsKeys.forEachIndexed { index, key ->
            buttons[startIndex + index] = object : Button() {

                override fun getMaterial(player: Player) = Material.LEVER

                override fun getDisplayName(player: Player): String {
                    val enabled = profile.getProfileSetting(category, key)
                    val status = if (enabled) "&aEnabled" else "&cDisabled"
                    return Chat.format("&e$key &7($status)")
                }

                override fun getDescription(player: Player) = mutableListOf(
                    Chat.format("&7Click to toggle &e$key")
                )

                override fun getButtonItem(player: Player) = ItemBuilder.of(Material.LEVER)
                    .name(getDisplayName(player))
                    .setLore(getDescription(player))
                    .build()

                override fun getData(player: Player): Short = 0

                override fun onClick(player: Player, slot: Int, type: ClickType) {
                    val newValue = !profile.getProfileSetting(category, key)
                    profile.setProfileSetting(category, key, newValue)

                    player.sendMessage(
                        Chat.format("&7$key is now ${if (newValue) "&aEnabled" else "&cDisabled"}")
                    )

                    XSound.UI_BUTTON_CLICK.play(player, 1f, 1f)

                    // Refresh this category menu
                    CategorySettingsMenu(profile, player, category, parentMenu).openMenu()
                }
            }
        }

        return buttons
    }
}
