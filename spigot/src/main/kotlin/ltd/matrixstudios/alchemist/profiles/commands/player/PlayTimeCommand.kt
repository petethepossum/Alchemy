package ltd.matrixstudios.alchemist.profiles.commands.player

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.*
import ltd.matrixstudios.alchemist.api.AlchemistAPI
import ltd.matrixstudios.alchemist.models.profile.GameProfile
import ltd.matrixstudios.alchemist.service.profiles.ProfileGameService
import ltd.matrixstudios.alchemist.util.Chat
import ltd.matrixstudios.alchemist.util.TimeUtil
import org.bukkit.entity.Player

@CommandAlias("playtime")
class PlayTimeCommand : BaseCommand() {

    @Default
    @CommandCompletion("@gameprofile")
    fun playtime(player: Player, @Name("target") @Optional targetProfile: GameProfile?) {
        val finalProfile = targetProfile ?: ProfileGameService.byId(player.uniqueId)

        if (finalProfile == null) {
            player.sendMessage(Chat.format("&cCould not find your profile! This should not happen"))
            return
        }

        if (finalProfile.uuid != player.uniqueId && !player.hasPermission("alchemist.staff")) {
            player.sendMessage(Chat.format("&cYou do not have permission to view other players' playtime."))
            return
        }

        val targetName = if (finalProfile.uuid == player.uniqueId) {
            "Your"
        } else {
            val rankColor = finalProfile.getCurrentRank().color
            AlchemistAPI.getRankDisplay(finalProfile.uuid) + "$rankColor's&a"
        }

        val formatted = TimeUtil.formatDuration(finalProfile.playtimeMillis)

        player.sendMessage(Chat.format("&a$targetName playtime: &f$formatted"))
    }
}