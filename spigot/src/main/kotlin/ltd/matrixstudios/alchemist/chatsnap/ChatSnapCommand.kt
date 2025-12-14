package ltd.matrixstudios.alchemist.chatsnap

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.*
import ltd.matrixstudios.alchemist.models.chatsnap.ChatSnap
import ltd.matrixstudios.alchemist.service.chatsnap.ChatSnapService
import ltd.matrixstudios.alchemist.util.Chat
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.UUID

class ChatSnapCommand : BaseCommand() {

    @CommandAlias("chatsnap")
    @CommandPermission("alchemist.chatsnap")
    @CommandCompletion("@players 1|5|10|20|50")
    fun snap(
        sender: Player,
        @Name("target") targetName: String,
        @Name("lines") @Optional lines: Int?
    ) {
        val target = Bukkit.getPlayerExact(targetName)
        if (target == null) {
            sender.sendMessage(Chat.format("&cPlayer not found or offline."))
            return
        }

        val amount = when {
            lines == null -> 25
            lines < 1 -> 1
            lines > ChatCache.MAX_PER_PLAYER -> ChatCache.MAX_PER_PLAYER
            else -> lines
        }

        val messages = ChatCache.getLastMessages(target.uniqueId, amount)
        if (messages.isEmpty()) {
            sender.sendMessage(Chat.format("&cNo recent messages for &f${target.name}&c."))
            return
        }

        val snap = ChatSnap(
            UUID.randomUUID(),
            target.uniqueId,
            messages,
            System.currentTimeMillis(),
            0 // numericId will be filled by ChatSnapService.save
        )

        ChatSnapService.save(snap)
        sender.sendMessage(Chat.format("&aSaved ChatSnap for &f${target.name} &a(#${snap.numericId})."))
    }
}
