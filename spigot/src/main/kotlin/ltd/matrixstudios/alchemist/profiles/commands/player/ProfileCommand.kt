package ltd.matrixstudios.alchemist.profiles.commands.player

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.Default
import ltd.matrixstudios.alchemist.models.profile.GameProfile
import ltd.matrixstudios.alchemist.profiles.commands.player.menu.PlayerInformationMenu
import ltd.matrixstudios.alchemist.service.profiles.ProfileGameService
import ltd.matrixstudios.alchemist.util.Chat
import org.bukkit.entity.Player


@CommandAlias("profile|myprofile")
class ProfileCommand : BaseCommand() {
    @Default
    fun onProfile(player: Player) {
        val profile: GameProfile? = ProfileGameService.byId(player.uniqueId)
        if (profile == null) {
            player.sendMessage(Chat.format("&cCould not find your profile!"))
            return
        }
        PlayerInformationMenu.PlayerSelfInformationMenu(player, profile).openMenu()
    }
}