package ltd.matrixstudios.alchemist.chat.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Name
import co.aikar.commands.annotation.Optional
import ltd.matrixstudios.alchemist.api.AlchemistAPI
import ltd.matrixstudios.alchemist.chat.ChatModule
import ltd.matrixstudios.alchemist.chat.ChatService
import ltd.matrixstudios.alchemist.service.profiles.ProfileGameService
import ltd.matrixstudios.alchemist.util.Chat
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

object ChatCommands : BaseCommand()
{

    @CommandAlias("slowchat")
    @CommandPermission("alchemist.chat.admin")
    fun slowchat(player: CommandSender, @Name("duration") duration: Int)
    {
        if (!ChatService.slowed)
        {
            ChatService.slowDuration = duration
            ChatService.slowed = true
            player.sendMessage(Chat.format("&aYou have just slowed the chat down to 1 message every &f$duration &aseconds"))
        } else
        {
            ChatService.slowDuration = 0
            ChatService.slowed = false
            player.sendMessage(Chat.format("&cIn game chat is no longer slowed down"))
        }
    }

    @CommandAlias("mutechat")
    @CommandPermission("alchemist.chat.admin")
    fun mutechat(player: CommandSender)
    {
        if (!ChatService.muted)
        {
            ChatService.muted = true
            player.sendMessage(Chat.format("&aYou have just muted the global chat"))
        } else
        {
            ChatService.muted = false
            player.sendMessage(Chat.format("&aGlobal chat is no longer muted!"))
        }
    }
    @CommandAlias("chat")
    fun chatCommand(player: Player, @Name("channel") @Optional channel: String?) {
        val profile = AlchemistAPI.syncFindProfile(player.uniqueId)
        if (profile == null) {
            player.sendMessage(Chat.format("&cCould not find your profile!"))
            return
        }

        // Load currently selected chat mode
        val currentChannel = profile.metadata.get("chat-channel")?.asString ?: "global"

        // If no channel argument, show the clickable list
        if (channel == null) {
            player.sendMessage(Chat.format("&a[&2*&a] All available Chat Channel(s):"))

            // Always has global
            sendClickableChannel(player, "Global", "global", currentChannel)

            // Staff channel if they have permission
            if (player.hasPermission("alchemist.staff")) {
                sendClickableChannel(player, "Staff", "staff", currentChannel)
            }

            // Admin channel if they have permission
            if (player.hasPermission("alchemist.adminchat")) {
                sendClickableChannel(player, "Admin", "admin", currentChannel)
            }
            return
        }

        // Switching chat mode
        val availableChannels = mutableSetOf("global")
        if (player.hasPermission("alchemist.staff")) availableChannels.add("staff")
        if (player.hasPermission("alchemist.adminchat")) availableChannels.add("admin")

        if (!availableChannels.contains(channel.lowercase())) {
            player.sendMessage(Chat.format("&cThat chat channel does not exist or you don’t have access to it!"))
            return
        }

        // Save to metadata
        profile.metadata.addProperty("chat-channel", channel.lowercase())
        ProfileGameService.save(profile)
        player.sendMessage(Chat.format("&aYour chat channel has been set to &f${channel.capitalize()}"))
    }

    private fun sendClickableChannel(player: Player, displayName: String, channelId: String, currentChannel: String) {
        val chatMode = ChatModule.ChatMode.values().firstOrNull { it.name.equals(channelId, ignoreCase = true) }
        val colour = chatMode?.displayColour ?: "&f"

        val comp = TextComponent(Chat.format("&8- $colour$displayName"))
        if (channelId.equals(currentChannel, ignoreCase = true)) {
            comp.addExtra(Chat.format(" &7(Currently Selected)"))
        } else {
            comp.clickEvent = net.md_5.bungee.api.chat.ClickEvent(
                net.md_5.bungee.api.chat.ClickEvent.Action.RUN_COMMAND,
                "/chat $channelId"
            )
            comp.hoverEvent = net.md_5.bungee.api.chat.HoverEvent(
                net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT,
                arrayOf(TextComponent(Chat.format("&eClick to switch to $displayName Chat")))
            )
        }
        player.spigot().sendMessage(comp)
    }
}