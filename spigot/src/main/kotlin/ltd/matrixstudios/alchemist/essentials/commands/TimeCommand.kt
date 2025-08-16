package ltd.matrixstudios.alchemist.essentials.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.*
import org.bukkit.entity.Player
import ltd.matrixstudios.alchemist.util.Chat

@CommandPermission("alchemist.essentials.time")
class TimeCommand : BaseCommand() {

    @CommandAlias("time|settime")
    @Syntax("<day|night|sunset|midnight>")
    fun setServerTime(
        player: Player,
        @Name("time") time: String
    ) {
        val timeValue = when (time.lowercase()) {
            "day" -> 1000
            "night" -> 13000
            "sunset", "noon" -> 6000
            "midnight" -> 18000
            else -> {
                player.sendMessage(Chat.format("&cInvalid time. Use &eday&c, &enight&c, &esunset&c, or &emidnight&c."))
                return
            }
        }

        player.world.setTime(timeValue.toLong())
        player.sendMessage(Chat.format("&aTime in the world has been set to &e$time&a."))
    }

    @CommandAlias("ptime")
    @CommandPermission("alchemist.essentials.ptime")
    @Syntax("<day|night|midnight|reset|ticks>")
    fun setPersonalTime(
        player: Player,
        @Name("time") time: String
    ) {
        when (time.lowercase()) {
            "day" -> {
                player.setPlayerTime(1000, false)
                player.sendMessage(Chat.format("&aYour personal time has been set to &eDay&a."))
            }
            "night" -> {
                player.setPlayerTime(13000, false)
                player.sendMessage(Chat.format("&aYour personal time has been set to &eNight&a."))
            }
            "sunset", "noon" -> {
                player.setPlayerTime(6000, false)
                player.sendMessage(Chat.format("&aYour personal time has been set to &e$time&a."))
            }
            "midnight" -> {
                player.setPlayerTime(18000, false)
                player.sendMessage(Chat.format("&aYour personal time has been set to &eMidnight&a."))
            }
            "reset" -> {
                player.resetPlayerTime()
                player.sendMessage(Chat.format("&aYour personal time has been &ereset to server default&a."))
            }
            else -> {
                val ticks = time.toLongOrNull()
                if (ticks != null && ticks in 0..24000) {
                    player.setPlayerTime(ticks, false)
                    player.sendMessage(Chat.format("&aYour personal time has been set to &e$ticks ticks&a."))
                } else {
                    player.sendMessage(Chat.format("&cInvalid time. Use &eday&c, &enight&c, &emidnight&c, &ereset&c, or a number between &e0-24000&c."))
                }
            }
        }
    }
}
