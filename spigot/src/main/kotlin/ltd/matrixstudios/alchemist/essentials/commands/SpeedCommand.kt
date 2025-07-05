package ltd.matrixstudios.alchemist.essentials.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.*
import org.bukkit.entity.Player

@CommandAlias("setspeed|speed")
@CommandPermission("alchemist.essentials.speed")
class SpeedCommand : BaseCommand() {

    @Default
    @CommandCompletion("0.1|0.2|0.5|1.0")
    fun setSpeed(player: Player, @Name("speed") speed: Float) {
        if (speed !in 0.0f..1.0f) {
            player.sendMessage("§cSpeed value must be between 0.0 and 1.0.")
            return
        }

        if (player.isFlying) {
            player.flySpeed = speed
            player.sendMessage("§aYour flying speed has been set to §e$speed§a.")
        } else {
            player.walkSpeed = speed
            player.sendMessage("§aYour ground speed has been set to §e$speed§a.")
        }
    }
    @Subcommand("reset")
    fun resetSpeed(player: Player) {
        player.walkSpeed = 0.2f
        player.flySpeed = 0.1f
        player.sendMessage("§aYour speed has been reset to default.")
    }
}