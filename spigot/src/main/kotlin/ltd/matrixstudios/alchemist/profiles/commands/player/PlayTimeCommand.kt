package ltd.matrixstudios.alchemist.profiles.commands.player

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.*
import ltd.matrixstudios.alchemist.models.profile.GameProfile
import ltd.matrixstudios.alchemist.service.profiles.ProfileGameService
import ltd.matrixstudios.alchemist.util.Chat
import ltd.matrixstudios.alchemist.util.TimeUtil
import org.bukkit.entity.Player

@CommandAlias("playtime")
class PlayTimeCommand : BaseCommand() {

    @Default
    fun ownPlaytime(player: Player) {
        val profile = ProfileGameService.byId(player.uniqueId)
        if (profile == null) {
            player.sendMessage(Chat.format("&cCould not find your profile!"))
            return
        }
        val formatted = TimeUtil.formatDuration(profile.playtimeMillis)
        player.sendMessage(Chat.format("&aYour playtime: &f$formatted"))
    }

    @CommandPermission("alchemist.staff")
    @CommandCompletion("@gameprofile")
    fun otherPlaytime(player: Player, @Name("target") target: GameProfile) {
        val formatted = TimeUtil.formatDuration(target.playtimeMillis)
        player.sendMessage(Chat.format("&a${target.username}'s playtime: &f$formatted"))
    }
}