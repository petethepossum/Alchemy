package ltd.matrixstudios.alchemist.staff.mode.action

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import com.cryptomorin.xseries.messages.ActionBar
import ltd.matrixstudios.alchemist.chat.ChatService
import ltd.matrixstudios.alchemist.profiles.getProfile
import ltd.matrixstudios.alchemist.staff.mode.StaffSuiteManager
import ltd.matrixstudios.alchemist.staff.mode.action.StaffModeActionBarHandler.Companion.sendActionBar
import ltd.matrixstudios.alchemist.util.Chat
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable

@CommandAlias("testactionbar")
@CommandPermission("alchemist.staff")
class TestActionBarCommand : BaseCommand() {
    @Default
    fun execute(player: Player) {
        ActionBar.sendActionBar(player, "&eThis is a test &aActionBar &fmessage!")
        player.sendMessage(Chat.format("&aSent test action bar!"))
    }
}

class StaffModeActionBarHandler : BukkitRunnable() {

    override fun run() {
        for (player in Bukkit.getOnlinePlayers()) {
            if (StaffSuiteManager.isModMode(player)) {
                sendActionBar(
                    player,
                    "&eChat&7: &r${getChatChannel(player)} &7｜ &eVanish&7: &r${getVanishString(player)} &7｜ &eStatus&7: &r${getChatString()}"
                )
            }
        }
    }

    private fun getChatString(): String {
        return when {
            ChatService.muted -> "&cMuted"
            ChatService.slowed -> "&6Slowed"
            else -> "&fNormal"
        }
    }

    private fun getVanishString(player: Player): String {
        return if (player.hasMetadata("vanish")) "&aInvisible" else "&cVisible"
    }

    private fun getChatChannel(player: Player): String {
        val profile = player.getProfile() ?: return "&7Unknown"
        val chatChannel = profile.metadata.get("chat-channel")?.asString ?: "global"
        val display = chatChannel.replaceFirstChar { it.uppercase() }
        return when (chatChannel.lowercase()) {
            "global" -> "&f$display"
            "staff" -> "&a$display"
            "admin" -> "&c$display"
            else -> "&7$display"
        }
    }

    companion object {
        fun sendActionBar(player: Player, message: String) {
            // Use XActionBar for cross-version compatibility
            ActionBar.sendActionBar(player, Chat.format(message))
        }
    }
}
