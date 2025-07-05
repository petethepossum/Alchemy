package ltd.matrixstudios.alchemist.essentials.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.*
import org.bukkit.entity.Player

@CommandAlias("speed|setspeed")
@CommandPermission("alchemist.essentials.speed")
class SpeedCommand : BaseCommand() {

    @Default
    @CommandCompletion("0|1|2|3|4|5|6|7|8|9|10")
    fun setSpeed(player: Player, @Name("speed") speed: Int) {
        if (speed !in 0..10) {
            player.sendMessage("§cSpeed must be between 0 and 10.")
            return
        }
//below fuctionality is taken from holypvp hcf core
        val convertedSpeed = getSpeed(speed, player.isFlying)
        if (player.isFlying) {
            player.flySpeed = convertedSpeed
            player.sendMessage("§aYour flying speed has been set to §e$speed§a (internal speed: §e$convertedSpeed§a).")
        } else {
            player.walkSpeed = convertedSpeed
            player.sendMessage("§aYour walking speed has been set to §e$speed§a (internal speed: §e$convertedSpeed§a).")
        }
    }

    @Subcommand("reset")
    fun resetSpeed(player: Player) {
        player.walkSpeed = 0.2f
        player.flySpeed = 0.1f
        player.sendMessage("§aYour speed has been reset to default.")
    }

    private fun getSpeed(speed: Int, isFly: Boolean): Float {
        val defaultSpeed = if (isFly) 0.1f else 0.2f
        val maxSpeed = 1.0f
        return if (speed < 1) {
            defaultSpeed * speed
        } else {
            val ratio = (speed - 1).toFloat() / 9.0f * (maxSpeed - defaultSpeed)
            ratio + defaultSpeed
        }
    }
}
