package ltd.matrixstudios.alchemist.staff.mode

import ltd.matrixstudios.alchemist.redis.RedisPacketManager
import com.cryptomorin.xseries.XMaterial
import ltd.matrixstudios.alchemist.serialize.type.ItemStackAdapter
import ltd.matrixstudios.alchemist.util.Chat
import ltd.matrixstudios.alchemist.util.items.ItemBuilder
import ltd.matrixstudios.alchemist.util.skull.SkullUtil
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player

object StaffItems
{

    val COMPASS = ItemBuilder.of(XMaterial.COMPASS.parseMaterial()!!).name("&bCompass").build()
    val INVENTORY_INSPECT = ItemBuilder.of(XMaterial.BOOK.parseMaterial()!!).name("&bInspect Inventory").build()
    val RANDOMTP = ItemBuilder.of(XMaterial.BEACON.parseMaterial()!!).name("&bRandom TP").build()
    val WORLDEDIT_AXE = ItemBuilder.of(XMaterial.WOODEN_AXE.parseMaterial()!!).name("&bWorldedit Axe").build()
    val BETTER_VIEW = ItemBuilder.of(XMaterial.GRAY_CARPET.parseMaterial()!!).name("&bBetter View").build()
    val VANISH = ItemBuilder.of(XMaterial.GRAY_DYE.parseMaterial()!!).name("&bUnvanish").build()
    val UNVANISH = ItemBuilder.of(XMaterial.LIME_DYE.parseMaterial()!!).name("&bVanish").build()
    val FREEZE = ItemBuilder.of(XMaterial.ICE.parseMaterial()!!).name("&bFreeze Player").build()
    val LAST_PVP = ItemBuilder.of(XMaterial.EMERALD.parseMaterial()!!).name("&bLast PvP").build()
    val EDIT_MOD_MODE = ItemBuilder.of(XMaterial.EMERALD.parseMaterial()!!).name(Chat.format("&a&lEdit Mod Mode")).build()

    val ITEMS_IN_LIST = listOf(
        COMPASS,
        INVENTORY_INSPECT,
        RANDOMTP,
        WORLDEDIT_AXE,
        BETTER_VIEW,
        VANISH,
        UNVANISH,
        FREEZE,
        LAST_PVP,
        EDIT_MOD_MODE
    )

    var lastPvP: Location? = null

    fun equip(player: Player)
    {
        val resource = RedisPacketManager.pool.resource

        resource.use {
            val item = it.hget("Alchemist:ModMode:", player.uniqueId.toString())

            if (item != null)
            {
                val items = ItemStackAdapter.itemStackArrayFromBase64(item)

                player.inventory.contents = items
            } else
            {
                player.inventory.setItem(0, COMPASS)
                player.inventory.setItem(1, INVENTORY_INSPECT)
                player.inventory.setItem(2, RANDOMTP)
                player.inventory.setItem(3, BETTER_VIEW)

                if (player.hasPermission("alchemist.staffmode.worldedit"))
                {
                    player.inventory.setItem(4, WORLDEDIT_AXE)
                }

                player.inventory.setItem(
                    6,
                    ItemBuilder.copyOf(SkullUtil.generate(player.name, "")).name("&bOnline Staff").build()
                )
                player.inventory.setItem(7, VANISH)
                player.inventory.setItem(8, FREEZE)
            }
        }

        player.inventory.setItem(22, EDIT_MOD_MODE)

    }

}