package ltd.matrixstudios.alchemist.commands.alts

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Name
import ltd.matrixstudios.alchemist.api.AlchemistAPI
import ltd.matrixstudios.alchemist.commands.alts.menu.AltsMenu
import ltd.matrixstudios.alchemist.models.profile.GameProfile
import ltd.matrixstudios.alchemist.punishments.PunishmentType
import ltd.matrixstudios.alchemist.service.expirable.PunishmentService
import ltd.matrixstudios.alchemist.util.Chat
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.text.SimpleDateFormat
import java.util.*

class AltsCommand : BaseCommand() {

    @CommandAlias("alts")
    @CommandPermission("alchemist.alts")
    @CommandCompletion("@gameprofile")
    fun listAll(player: Player, @Name("target") profile: GameProfile) {
        profile.getAltAccounts().thenAccept { alts ->
            AltsMenu(player, profile, alts).updateMenu()
            sendAltsChat(player, profile, alts)
        }
    }

    fun sendAltsChat(player: Player, target: GameProfile, alts: List<GameProfile>) {
        val name = Chat.format(AlchemistAPI.getRankDisplay(target.uuid))
        player.sendMessage(
            "Alts for ${name}: &7[&aOnline&f, &7Offline&f, &fMuted&f, &cBan&f, &4Blacklist&7]"
                .replace("&", "§")
        )

        val sdf = SimpleDateFormat("dd/MM/yyyy hh:mm:ss a")

        alts.forEach { alt ->
            val activePunishments = PunishmentService.getFromCache(alt.uuid)
                .filter { it.expirable.isActive() }
            val punishmentType = activePunishments.firstOrNull()?.punishmentType
            val isOnline = Bukkit.getPlayer(alt.uuid) != null

            val colorCode = when {
                punishmentType == PunishmentType.BLACKLIST.name -> "§4"
                punishmentType == PunishmentType.BAN.name -> "§c"
                punishmentType == PunishmentType.MUTE.name -> "§f"
                isOnline -> "§a"
                else -> "§7"
            }

            val hoverText = StringBuilder()
            hoverText.append("&eName: ${AlchemistAPI.getRankDisplay(alt.uuid)}\n")
            hoverText.append("&eLast Seen:&d ${sdf.format(Date(alt.lastSeenAt))}\n")
            hoverText.append("&e---------------------\n")

            if (alt.ip == target.ip) {
                hoverText.append("&aCurrently matching ${AlchemistAPI.getRankDisplay(target.uuid)}\n")
            } else {
                hoverText.append("&cCurrently not matching ${AlchemistAPI.getRankDisplay(target.uuid)}\n")
            }

            if (activePunishments.isNotEmpty()) {
                val first = activePunishments.first()
                hoverText.append("&eA &r${first.getGrantable().niceName} &eis active on this account\n")
            }

            hoverText.append("&e---------------------\n")
            hoverText.append("${AlchemistAPI.getRankDisplay(alt.uuid)} &eCurrent IP Info:\n")
            hoverText.append("&eFirst Login: ${alt.firstLoginAt?.let { sdf.format(Date(it)) } ?: "Unknown"}\n")
            hoverText.append("&eLast Login: ${alt.lastLoginAt?.let { sdf.format(Date(it)) } ?: "Unknown"}\n")
            hoverText.append("${AlchemistAPI.getRankDisplay(target.uuid)} &eMatching IP Info:\n")
            hoverText.append("&eFirst Login: ${target.firstLoginAt?.let { sdf.format(Date(it)) } ?: "Unknown"}\n")
            hoverText.append("&eLast Login: ${target.lastLoginAt?.let { sdf.format(Date(it)) } ?: "Unknown"}\n")
            hoverText.append("&e---------------------\n")



            val component = TextComponent(colorCode + alt.username)
            component.hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(hoverText.toString().replace("&", "§")))
            player.spigot().sendMessage(component)
        }
    }
}
