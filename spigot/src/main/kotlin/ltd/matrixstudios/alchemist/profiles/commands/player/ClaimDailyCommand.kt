package ltd.matrixstudios.alchemist.profiles.commands.player

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.Default
import ltd.matrixstudios.alchemist.service.profiles.ProfileGameService
import ltd.matrixstudios.alchemist.util.Chat
import ltd.matrixstudios.alchemist.util.DailyRewardUtil
import org.bukkit.entity.Player

@CommandAlias("claimdaily|daily|dreward")
class ClaimDailyCommand : BaseCommand() {

    @Default
    fun onClaim(player: Player) {
        val profile = ProfileGameService.byId(player.uniqueId)

        if (profile == null) {
            player.sendMessage(Chat.format("&cCould not load your profile! This should never happen!"))
            return
        }

        DailyRewardUtil.handleDailyRewardClaim(player, profile)
    }
}
