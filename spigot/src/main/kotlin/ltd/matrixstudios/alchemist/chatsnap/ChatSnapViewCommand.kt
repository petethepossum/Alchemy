package ltd.matrixstudios.alchemist.chatsnap

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.*
import ltd.matrixstudios.alchemist.api.AlchemistAPI
import ltd.matrixstudios.alchemist.chatsnap.menu.ChatSnapViewMenu
import ltd.matrixstudios.alchemist.chatsnap.menu.ChatSnapViewerMenu
import ltd.matrixstudios.alchemist.service.chatsnap.ChatSnapService
import ltd.matrixstudios.alchemist.util.Chat
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.UUID


class ChatSnapViewCommand : BaseCommand() {
    @CommandAlias("chatsnapview|csv|viewsnap|mhist|snaps")
    @CommandPermission("alchemist.chatsnap.view")
    @Default
    @CommandCompletion("@players|1..9999")
    fun view(
        sender: Player,
        @Name("identifier") @Optional identifier: String?
    ) {
        // No identifier: show sender's own snaps if any
        if (identifier == null) {
            val selfSnaps = ChatSnapService.getByOwner(sender.uniqueId)
            if (selfSnaps.isEmpty()) {
                sender.sendMessage(Chat.format("&cYou have no saved ChatSnaps."))
                return
            }

            val selfProfile = AlchemistAPI.syncFindProfile(sender.uniqueId)
            ChatSnapViewerMenu(sender, selfSnaps, selfProfile).updateMenu()
            return
        }

        // Try numeric ID first
        val numeric = identifier.toIntOrNull()
        if (numeric != null) {
            val snap = ChatSnapService.byNumericId(numeric)
            if (snap == null) {
                sender.sendMessage(Chat.format("&cNo ChatSnap found with id &f#$numeric&c."))
                return
            }

            val profile = AlchemistAPI.syncFindProfile(snap.owner)
            ChatSnapViewMenu(sender, snap, profile).updateMenu()
            return
        }

        // Treat as player name
        val target = Bukkit.getPlayerExact(identifier)
        if (target == null) {
            sender.sendMessage(Chat.format("&cPlayer &f$identifier &cis not online."))
            return
        }

        val snaps = ChatSnapService.getByOwner(target.uniqueId)
        if (snaps.isNotEmpty()) {
            val profile = AlchemistAPI.syncFindProfile(target.uniqueId)
            ChatSnapViewerMenu(sender, snaps, profile).updateMenu()
            return
        }

        // Fallback: ephemeral snap from recent chat cache
        val messages = ChatCache.getLastMessages(target.uniqueId, 10)
        if (messages.isEmpty()) {
            sender.sendMessage(Chat.format("&cNo recent messages for &f${target.name}&c."))
            return
        }

        val snap = ltd.matrixstudios.alchemist.models.chatsnap.ChatSnap(
            UUID.randomUUID(),
            target.uniqueId,
            messages,
            System.currentTimeMillis(),
            0
        )

        ChatSnapViewMenu(sender, snap, AlchemistAPI.syncFindProfile(target.uniqueId)).updateMenu()
    }
}
