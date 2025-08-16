package ltd.matrixstudios.alchemist.util.menu.buttons

import ltd.matrixstudios.alchemist.util.Chat
import ltd.matrixstudios.alchemist.util.menu.Button
import ltd.matrixstudios.alchemist.util.skull.SkullUtil
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack

class SkullButton(
    val texture: String,
    val description: MutableList<String>,
    val name: String
) : Button() {

    private var body: ((Player, Int, ClickType) -> Unit)? = null

    fun setBody(body: ((Player, Int, ClickType) -> Unit)?): SkullButton {
        this.body = body
        return this
    }

    override fun getButtonItem(player: Player): ItemStack? {
        // Create a player skull with custom texture, display name, and lore
        return SkullUtil.applyCustomHead(
            skull = ItemStack(Material.SKULL_ITEM),
            base64 = texture,
            displayName = name,
            lore = description
        )
    }

    override fun getMaterial(player: Player): Material = Material.SKULL_ITEM

    override fun getDescription(player: Player): MutableList<String> = description

    override fun getDisplayName(player: Player): String = Chat.format(name)

    override fun getData(player: Player): Short = 3

    override fun onClick(player: Player, slot: Int, type: ClickType) {
        body?.invoke(player, slot, type)
    }
}
