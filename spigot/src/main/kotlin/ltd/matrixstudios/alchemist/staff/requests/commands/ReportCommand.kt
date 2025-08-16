package ltd.matrixstudios.alchemist.staff.requests.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.Name
import org.bukkit.entity.Player

@CommandAlias("report")
class ReportCommand : BaseCommand() {

    @CommandCompletion("@gameprofile")
    fun onReport(
        sender: Player,
        @Name("target") targetName: String,
        @Name("reason") reason: String
    ) {
        // This is the most important test.
        // If this message appears correctly in the console, the command is working.
        sender.sendMessage("§aDebug: Successfully executed the command!")
        sender.sendMessage("§e -> Target: §f$targetName")
        sender.sendMessage("§e -> Reason: §f$reason")

        println("[REPORT DEBUG] Sender: ${sender.name}, Target: $targetName, Reason: $reason")
    }
}