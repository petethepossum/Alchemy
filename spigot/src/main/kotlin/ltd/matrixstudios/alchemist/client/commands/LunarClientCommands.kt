package ltd.matrixstudios.alchemist.client.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.CommandHelp
import co.aikar.commands.annotation.*
import co.aikar.commands.bukkit.contexts.OnlinePlayer
import com.lunarclient.apollo.Apollo
import com.lunarclient.apollo.module.notification.Notification
import com.lunarclient.apollo.module.notification.NotificationModule
import com.lunarclient.apollo.module.staffmod.StaffMod
import com.lunarclient.apollo.module.staffmod.StaffModModule
import com.lunarclient.apollo.player.ApolloPlayer
import ltd.matrixstudios.alchemist.api.AlchemistAPI
import ltd.matrixstudios.alchemist.util.Chat
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import java.time.Duration

object LunarClientNotifier {

    /**
     *
     *
     * @param apolloPlayer The Apollo player to send the notification to.
     * @param title The title component
     * @param description The description component
     * @param icon Optional resource icon path (default: "icons/golden_apple.png").
     * @param duration Duration to display the notification (default: 5 seconds).
     */
    fun sendNotification(
        apolloPlayer: ApolloPlayer,
        title: Component,
        description: Component,
        icon: String = "icons/golden_apple.png",
        duration: Duration = Duration.ofSeconds(5)
    ) {
        val notificationModule = Apollo.getModuleManager()
            .getModule(NotificationModule::class.java) ?: return

        try {
            // Build the notification via reflection kinda scuff need to unrelocate adventure api
            val builder = Notification::class.java.getDeclaredMethod("builder").invoke(null)

            val titleMethod = builder.javaClass.getMethod("titleComponent", Any::class.java)
            val descriptionMethod = builder.javaClass.getMethod("descriptionComponent", Any::class.java)
            val resourceMethod = builder.javaClass.getMethod("resourceLocation", String::class.java)
            val displayTimeMethod = builder.javaClass.getMethod("displayTime", Duration::class.java)
            val buildMethod = builder.javaClass.getMethod("build")

            titleMethod.invoke(builder, title)
            descriptionMethod.invoke(builder, description)
            resourceMethod.invoke(builder, icon)
            displayTimeMethod.invoke(builder, duration)

            val notification = buildMethod.invoke(builder)
            val displayMethod = notificationModule.javaClass.getMethod("displayNotification", ApolloPlayer::class.java, Notification::class.java)
            displayMethod.invoke(notificationModule, apolloPlayer, notification)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}


@CommandAlias("lunarclient|lc")
@CommandPermission("alchemist.clients.lunar")
object LunarClientCommands : BaseCommand() {

    @HelpCommand
    fun help(help: CommandHelp) {
        help.showHelp()
    }


    fun enableStaffModules(apolloPlayer: ApolloPlayer) {
        val staffModModule = Apollo.getModuleManager().getModule(StaffModModule::class.java)
        staffModModule?.enableStaffMods(apolloPlayer, listOf(StaffMod.XRAY))
    }
    fun disableStaffModules(apolloPlayer: ApolloPlayer) {
        val staffModModule = Apollo.getModuleManager().getModule(StaffModModule::class.java)
        staffModModule?.disableStaffMods(apolloPlayer, listOf(StaffMod.XRAY))
    }



    @Subcommand("players")
    fun players(sender: CommandSender) {
        val start = System.currentTimeMillis()
        val count = Bukkit.getOnlinePlayers().count {
            Apollo.getPlayerManager().hasSupport(it.uniqueId)
        }

        sender.sendMessage(Chat.format("&eThere are &a$count &eplayers running &bLunar Client&e."))
        sender.sendMessage(Chat.format("&7To check a specific user's status: /lc check <player>"))
        sender.sendMessage(Chat.format("&8(This lookup took ${System.currentTimeMillis() - start} ms)"))
    }

    @Subcommand("check")
    @CommandCompletion("@players")
    fun check(sender: CommandSender, @Name("target") target: OnlinePlayer) {
        val isUsing = Apollo.getPlayerManager().hasSupport(target.player.uniqueId)
        sender.sendMessage(
            Chat.format(
                "&r${AlchemistAPI.getRankDisplay(target.player.uniqueId)} &eis " +
                        "${if (isUsing) "&acurrently" else "&cnot currently"} &eusing &bLunar Client&e."
            )
        )
    }

    @Subcommand("staffmodules")
    @CommandCompletion("@players")
    fun staffModules(sender: CommandSender, @Name("target") target: OnlinePlayer) {
        val uuid = target.player.uniqueId
        val apolloOpt = Apollo.getPlayerManager().getPlayer(uuid)

        if (!apolloOpt.isPresent) {
            sender.sendMessage(Chat.format("&cThat player is not running Lunar Client."))
            return
        }

        val apolloPlayer = apolloOpt.get()

        enableStaffModules(apolloPlayer)

       // LunarClientNotifier.sendNotification(
       //     apolloPlayer,
        //    Component.text("Staff Modules Enabled", NamedTextColor.GREEN),
        //    Component.text("XRAY staff mod has been enabled.", NamedTextColor.WHITE)
      //Need to un relocate adventure api  )

        sender.sendMessage(
            Chat.format("&r${AlchemistAPI.getRankDisplay(uuid)} &ehas been given &bLunar Client &eStaff Modules.")
        )
    }
}
