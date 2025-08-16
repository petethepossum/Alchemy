package ltd.matrixstudios.alchemist.staff.commands.menu

import com.cryptomorin.xseries.XMaterial
import ltd.matrixstudios.alchemist.api.AlchemistAPI
import ltd.matrixstudios.alchemist.models.profile.GameProfile
import ltd.matrixstudios.alchemist.punishments.PunishmentType
import ltd.matrixstudios.alchemist.util.Chat
import ltd.matrixstudios.alchemist.util.menu.Button
import ltd.matrixstudios.alchemist.util.menu.Menu
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType

class StaffLeaderboardMenu(
    val player: Player,
    val preLoadedButtons: MutableMap<Int, Button>
) : Menu(player) {

    init {
        staticSize = 27
        placeholder = true
    }

    override fun getButtons(player: Player): MutableMap<Int, Button> = preLoadedButtons

    override fun getTitle(player: Player): String = "Viewing Staff Leaderboards"

    class LeaderboardPunishmentButton(
        val type: PunishmentType,
        val users: MutableList<GameProfile>
    ) : Button() {

        override fun getMaterial(player: Player): Material {
            val colorName = AlchemistAPI.getWoolColor(type.color).name.uppercase()
            val materialName = "${colorName}_WOOL"
            val xMat = XMaterial.matchXMaterial(materialName).orElse(XMaterial.WHITE_WOOL)
            return xMat.parseMaterial() ?: XMaterial.WHITE_WOOL.parseMaterial()!!
        }

        override fun getDescription(player: Player): MutableList<String> {
            val desc = mutableListOf<String>()
            desc.add(Chat.format("&8&m---------------------------"))

            users.forEachIndexed { index, profile ->
                val slotNum = index + 1
                desc.add(
                    Chat.format(
                        "${getColoredSlot(slotNum)} &7- ${profile.getRankDisplay()} &8(${profile.getExecutedCountByType(type)})"
                    )
                )
            }

            desc.add(Chat.format("&8&m---------------------------"))
            return desc
        }

        private fun getColoredSlot(index: Int): String = when (index) {
            1 -> "&6#1"
            2 -> "&7#2"
            3 -> "&c#3"
            else -> "&a#$index"
        }

        override fun getDisplayName(player: Player): String =
            Chat.format("${type.color}${type.niceName} Leaderboard &7(Top 10)")

        override fun getData(player: Player): Short = 0

        override fun onClick(player: Player, slot: Int, type: ClickType) {}
    }
}
