package ltd.matrixstudios.alchemist.util.menu.buttons

import ltd.matrixstudios.alchemist.util.Chat
import ltd.matrixstudios.alchemist.util.menu.Button
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta

class SimpleActionButton(
    val material: Material,
    val description: MutableList<String>,
    val name: String,
    val data: Short,
) : Button() {

    var body: ((Player, Int, ClickType) -> Unit)? = null
    val item: ItemStack = ItemStack(material, 1, data)

    init {
        val meta = item.itemMeta
        meta.setDisplayName(Chat.format(name))
        meta.lore = description
        item.itemMeta = meta
    }

    fun setBody(body: ((Player, Int, ClickType) -> Unit)?): SimpleActionButton {
        return this.apply { this.body = body }
    }

    fun glow(): SimpleActionButton {
        val meta = item.itemMeta ?: return this
        meta.addEnchant(Enchantment.DURABILITY, 1, true)
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS)
        item.itemMeta = meta
        return this
    }



    override fun getMaterial(player: Player): Material {
        return item.type
    }

    override fun getDescription(player: Player): MutableList<String> {
        return item.itemMeta?.lore?.toMutableList() ?: mutableListOf()
    }

    override fun getDisplayName(player: Player): String {
        return item.itemMeta?.displayName ?: Chat.format(name)
    }

    override fun getData(player: Player): Short {
        return item.durability
    }

    override fun onClick(player: Player, slot: Int, type: ClickType) {
        body?.invoke(player, slot, type)
    }
}