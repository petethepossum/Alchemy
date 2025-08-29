package ltd.matrixstudios.alchemist.profiles.commands.player

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.Default
import ltd.matrixstudios.alchemist.models.profile.GameProfile
import ltd.matrixstudios.alchemist.settings.menu.CategoryMenu
import ltd.matrixstudios.alchemist.service.profiles.ProfileGameService
import org.bukkit.entity.Player

@CommandAlias("settings|options|prefs|preferences")
class PrefsCommand : BaseCommand() {

    @Default
    fun openSettings(player: Player) {
        val profile: GameProfile? = ProfileGameService.byId(player.uniqueId)
        if (profile != null) {
            CategoryMenu(profile, player).openMenu()
        }
        else {
            player.sendMessage("§cCould not find your profile!")
        }
    }
}
