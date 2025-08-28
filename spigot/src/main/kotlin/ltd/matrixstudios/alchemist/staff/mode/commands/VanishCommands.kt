package ltd.matrixstudios.alchemist.staff.mode.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import ltd.matrixstudios.alchemist.Alchemist
import ltd.matrixstudios.alchemist.AlchemistSpigotPlugin
import ltd.matrixstudios.alchemist.redis.AsynchronousRedisSender
import ltd.matrixstudios.alchemist.staff.alerts.StaffActionAlertPacket
import ltd.matrixstudios.alchemist.staff.mode.StaffSuiteManager
import ltd.matrixstudios.alchemist.staff.mode.StaffSuiteVisibilityHandler
import ltd.matrixstudios.alchemist.redis.RedisVanishStatusService
import ltd.matrixstudios.alchemist.util.Chat
import org.bukkit.entity.Player
import org.bukkit.metadata.FixedMetadataValue

class VanishCommands : BaseCommand()
{

    @CommandAlias("vanish|v|byebye")
    @CommandPermission("alchemist.staffmode")
    fun vanish(player: Player)
    {
        if (RedisVanishStatusService.isVanished(player.uniqueId))
        {
            player.removeMetadata("vanish", AlchemistSpigotPlugin.instance)
            RedisVanishStatusService.delVanished(player.uniqueId)
            StaffSuiteVisibilityHandler.onDisableVisbility(player)
            player.sendMessage(Chat.format("&cYou have come out of vanish!"))
            AsynchronousRedisSender.send(StaffActionAlertPacket("has unvanished", player.name, Alchemist.globalServer.id))
        } else
        {
            player.setMetadata("vanish", FixedMetadataValue(AlchemistSpigotPlugin.instance, true))
            RedisVanishStatusService.setVanished(player.uniqueId)
            StaffSuiteVisibilityHandler.onEnableVisibility(player)
            player.sendMessage(Chat.format("&aYou have entered vanish!"))
            AsynchronousRedisSender.send(StaffActionAlertPacket("has vanished", player.name, Alchemist.globalServer.id))
        }
    }

    @CommandAlias("?vis|qvis|amivanished|visible")
    @CommandPermission("alchemist.staffmode")
    fun qvis(player: Player)
    {
        val modded = StaffSuiteManager.isModMode(player)
        val vanish = player.hasMetadata("vanish")
        val redisvanish = RedisVanishStatusService.isVanished(player.uniqueId)

        player.sendMessage(Chat.format("&6ModMode: &f" + if (modded) "&aYes" else "&cNo"))
        player.sendMessage(Chat.format("&6Vanished: &f" + if (vanish) "&aYes" else "&cNo"))
        player.sendMessage(Chat.format("&6Visibility: &f" + if (redisvanish) "&aVanished &e[REDIS]" else "&cVisible &e[REDIS]"))
        player.sendMessage(Chat.format("&7&oBukkit respects and abides by these values"))
    }
}