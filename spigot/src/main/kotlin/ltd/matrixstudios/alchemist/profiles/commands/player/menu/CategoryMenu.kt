package ltd.matrixstudios.alchemist.settings.menu

import ltd.matrixstudios.alchemist.models.profile.GameProfile
import ltd.matrixstudios.alchemist.util.Chat
import ltd.matrixstudios.alchemist.util.items.ItemBuilder
import ltd.matrixstudios.alchemist.util.menu.Button
import ltd.matrixstudios.alchemist.util.menu.Menu
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType


class CategoryMenu(
    private val profile: GameProfile,
    player: Player
) : Menu(player) {

    private val categories = profile.profileSettings.entrySet().map { it.key }

    override fun getTitle(player: Player) = "&6Settings Categories"
    fun size(player: Player) = ((categories.size - 1) / 9 + 1) * 9

    override fun getButtons(player: Player): MutableMap<Int, Button> {
        val buttons = mutableMapOf<Int, Button>()

        categories.forEachIndexed { index, category ->
            buttons[index] = object : Button() {
                override fun getMaterial(player: Player) = Material.PAPER
                override fun getDisplayName(player: Player) = Chat.format("&e$category")
                override fun getDescription(player: Player) = mutableListOf(
                    Chat.format("&7Click to open $category settings")
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


/**
 * Shows all settings for a specific category.
 * Example: category = "StaffSettings" -> shows all keys under profileSettings["StaffSettings"]
 */
class CategorySettingsMenu(
    private val profile: GameProfile,
    private val player: Player,
    private val category: String,
    private val parentMenu: Menu? = null
) : Menu(player) {

    private val settingsKeys = profile.getAllSettingsForCategory(category).toList()

    override fun getTitle(player: Player) = "&6$category Settings"
    fun size(player: Player) = ((settingsKeys.size - 1) / 9 + 1) * 9 + if (parentMenu != null) 1 else 0

    override fun getButtons(player: Player): MutableMap<Int, Button> {
        val buttons = mutableMapOf<Int, Button>()

        // Back button if parent menu exists
        parentMenu?.let {
            buttons[0] = object : Button() {
                override fun getMaterial(player: Player) = Material.ARROW
                override fun getDisplayName(player: Player) = Chat.format("&cBack")
                override fun getDescription(player: Player) = mutableListOf(Chat.format("&7Return to categories"))
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

        // Add toggle buttons for each key in the category
        settingsKeys.forEachIndexed { index, key ->
            buttons[startIndex + index] = object : Button() {
                override fun getMaterial(player: Player) = Material.LEVER
                override fun getDisplayName(player: Player): String {
                    val status = if (profile.getProfileSetting(category, key)) "&aEnabled" else "&cDisabled"
                    return Chat.format("&e$key &7($status)")
                }

                override fun getDescription(player: Player) = mutableListOf(
                    Chat.format("&7Click to toggle $key")
                )

                override fun getButtonItem(player: Player) = ItemBuilder.of(Material.LEVER)
                    .name(getDisplayName(player))
                    .setLore(getDescription(player))
                    .build()

                override fun getData(player: Player): Short = 0

                override fun onClick(player: Player, slot: Int, type: ClickType) {
                    val newValue = !profile.getProfileSetting(category, key)
                    profile.setProfileSetting(category, key, newValue)
                    player.sendMessage(Chat.format("&7$key is now ${if (newValue) "&aEnabled" else "&cDisabled"}"))
                    player.playSound(player.location, Sound.CLICK, 1f, 1f)
                    openMenu() // refresh
                }
            }
        }

        return buttons
    }
}
