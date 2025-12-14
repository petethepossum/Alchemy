package ltd.matrixstudios.alchemist.profiles

import ltd.matrixstudios.alchemist.Alchemist
import ltd.matrixstudios.alchemist.AlchemistSpigotPlugin
import ltd.matrixstudios.alchemist.api.AlchemistAPI
import ltd.matrixstudios.alchemist.chat.ChatService
import ltd.matrixstudios.alchemist.packets.AdminMessagePacket
import ltd.matrixstudios.alchemist.packets.StaffMessagePacket
import ltd.matrixstudios.alchemist.profiles.connection.postlog.BukkitPostLoginConnection
import ltd.matrixstudios.alchemist.profiles.connection.prelog.BukkitPreLoginConnection
import ltd.matrixstudios.alchemist.profiles.permissions.AccessiblePermissionHandler
import ltd.matrixstudios.alchemist.punishments.PunishmentType
import ltd.matrixstudios.alchemist.redis.AsynchronousRedisSender
import ltd.matrixstudios.alchemist.service.ranks.RankService
import ltd.matrixstudios.alchemist.util.Chat
import ltd.matrixstudios.alchemist.util.DailyRewardUtil
import ltd.matrixstudios.alchemist.util.TimeUtil
import me.clip.placeholderapi.PlaceholderAPI
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.event.player.AsyncPlayerPreLoginEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.util.*


class ProfileJoinListener : Listener {

    @EventHandler
    fun autoFormatChat(event: AsyncPlayerChatEvent) {
        val profile = AlchemistAPI.quickFindProfile(event.player.uniqueId).join() ?: return

        // Get chat mode from metadata
        val modeName = profile.metadata.get("chat-channel")?.asString ?: "global"

        when (modeName.lowercase()) {
            "global" -> {
                // continue to normal chat handling below
            }
            "staff" -> {
                if (event.player.hasPermission("alchemist.staff")) {
                    event.isCancelled = true
                    AsynchronousRedisSender.send(
                        StaffMessagePacket(
                            event.message,
                            Alchemist.globalServer.displayName,
                            event.player.uniqueId
                        )
                    )
                    return
                }
            }
            "admin" -> {
                if (event.player.hasPermission("alchemist.admin")) {
                    event.isCancelled = true
                    AsynchronousRedisSender.send(
                        AdminMessagePacket(
                            event.message,
                            Alchemist.globalServer.displayName,
                            event.player.uniqueId
                        )
                    )
                    return
                }
            }
        }

        // --- Chat Formatting Section ---
        var prefixString = ""
        var colorString = ""
        var rank = RankService.FALLBACK_RANK
        var nameString = profile.username

        if (profile.hasActivePrefix()) {
            profile.getActivePrefix()?.let { prefixString = it.prefix }
        }

        profile.activeColor?.let { colorString = it.chatColor }

        profile.rankDisguiseAttribute?.let {
            RankService.byId(it.rank)?.let { rank = it }
        } ?: run { rank = profile.getCurrentRank() }

        profile.skinDisguiseAttribute?.let { nameString = it.customName }

        val configFormat = AlchemistSpigotPlugin.instance.config.getString("chat.format")
        val format = Chat.format(
            configFormat
                ?.replace("<prefix_string>", prefixString)
                ?.replace("<rank_string>", rank.prefix)
                ?.replace("<player_name>", nameString)
                ?.replace("<color_string>", colorString)
                ?.replace("<message>", "%2\$s")
                ?: "%1\$s: %2\$s"
        )

        if (AlchemistSpigotPlugin.instance.server.pluginManager.isPluginEnabled("PlaceholderAPI")) {
            PlaceholderAPI.setPlaceholders(event.player, format)
        }

        // --- Punishment Checks ---
        if (profile.hasActivePunishment(PunishmentType.GHOST_MUTE)) {
            event.isCancelled = true
            event.player.sendMessage(
                Chat.format(format.replace("%1\$s", profile.username).replace("%2\$s", event.message))
            )
            return
        }

        if (profile.hasActivePunishment(PunishmentType.MUTE)) {
            val mute = profile.getActivePunishments(PunishmentType.MUTE).first()
            event.isCancelled = true

            val msgs = AlchemistSpigotPlugin.instance.config.getStringList("muted-chat")
            msgs.replaceAll { it.replace("<reason>", mute.reason) }
            msgs.replaceAll {
                it.replace(
                    "<expires>",
                    if (mute.expirable.duration == Long.MAX_VALUE) "Never" else TimeUtil.formatDuration(mute.expirable.addedAt + mute.expirable.duration - System.currentTimeMillis())
                )
            }
            msgs.forEach { event.player.sendMessage(Chat.format(it)) }
            return
        }

        // --- Chat Muted Check ---
        if (ChatService.muted && !event.player.hasPermission("alchemist.mutechat.bypass")) {
            event.player.sendMessage(Chat.format(ChatService.MUTE_MESSAGE))
            event.isCancelled = true
            return
        }

        // --- Chat Slow Check ---
        if (ChatService.slowed && !event.player.hasPermission("alchemist.slowchat.bypass")) {
            if (ChatService.isOnCooldown(event.player)) {
                val rem = ChatService.getCooldownRemaining(event.player)
                if (rem != 0) {
                    event.player.sendMessage(Chat.format(ChatService.SLOW_MESSAGE.replace("<seconds>", rem.toString())))
                    event.isCancelled = true
                    return
                }
            } else {
                ChatService.addCooldown(event.player)
            }
        }

        // --- Link Limitation ---
        if (ChatService.LINK_LIMIT_ENABLED) {
            val msg = event.message
            if (msg.contains("http://") || msg.contains("https://")) {
                val rankRequired = RankService.byId(ChatService.MINIMUM_LINK_SEND_RANK.lowercase(Locale.getDefault())) ?: return
                if (event.player.getCurrentRank().weight < rankRequired.weight) {
                    event.player.sendMessage(Chat.format("&eYou must be at least ${rankRequired.color}${rankRequired.displayName} &erank to send links"))
                    event.isCancelled = true
                    return
                }
            }
        }

        // --- Set final chat format ---
        event.format = format
    }

    @EventHandler
    fun applyPerms(event: PlayerJoinEvent) {
        val player = event.player

        if (!AlchemistSpigotPlugin.instance.config.getBoolean("debug.noJoinEvents")) {
            val allCallbacks = mutableListOf<(Player) -> Unit>().also {
                it.addAll(BukkitPostLoginConnection.allCallbacks + BukkitPostLoginConnection.allLazyCallbacks)
            }

            for (cback in allCallbacks) {
                cback.invoke(player)
            }
        }
    }

    @EventHandler
    fun join(event: AsyncPlayerPreLoginEvent) {
        if (!AlchemistSpigotPlugin.instance.config.getBoolean("debug.noJoinEvents")) {
            val allCallbacks = mutableListOf<(AsyncPlayerPreLoginEvent) -> Unit>().also {
                it.addAll(BukkitPreLoginConnection.allCallbacks + BukkitPreLoginConnection.allLazyCallbacks)
            }

            for (cback in allCallbacks) {
                cback.invoke(event)
            }
        }
    }
    @EventHandler
    fun autoClaimDailyOnJoin(event: PlayerJoinEvent) {
        val player = event.player
        if (!player.hasPermission("daily.autoclaim")) {
            return
        }
        val profile = AlchemistAPI.syncFindProfile(player.uniqueId) ?: return
        DailyRewardUtil.handleDailyRewardClaimSilently(player, profile)
    }


    @EventHandler
    fun leave(event: PlayerQuitEvent) {
        val player = event.player
        AccessiblePermissionHandler.remove(player)
    }
}
