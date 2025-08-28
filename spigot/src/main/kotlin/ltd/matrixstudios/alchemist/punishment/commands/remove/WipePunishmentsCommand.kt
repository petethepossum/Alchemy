package ltd.matrixstudios.alchemist.punishment.commands.remove

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Name
import ltd.matrixstudios.alchemist.models.profile.GameProfile
import ltd.matrixstudios.alchemist.models.profile.notes.ProfileNote
import ltd.matrixstudios.alchemist.punishments.PunishmentType
import ltd.matrixstudios.alchemist.service.expirable.PunishmentService
import ltd.matrixstudios.alchemist.service.profiles.ProfileGameService
import ltd.matrixstudios.alchemist.util.Chat
import org.bukkit.entity.Player

object WipePunishmentsCommand : BaseCommand()
{

    private fun addNoteToProfile(author: Player, targetProfile: GameProfile, content: String) {
        val note = ProfileNote(
            author = author.uniqueId,
            content = content,
            createdAt = System.currentTimeMillis(),
            deletable = false
        )
        targetProfile.notes.add(note)
        ProfileGameService.save(targetProfile)
    }

    @CommandAlias("wipepunishments")
    @CommandPermission("alchemist.punishments.wipe")
    fun wipePunishments(player: Player, @Name("type") typestr: String)
    {
        var foundType: PunishmentType? = null

        for (type in PunishmentType.values())
        {
            if (type.name.equals(typestr.uppercase(), ignoreCase = true))
            {
                foundType = type
            }
        }

        if (foundType == null)
        {
            val matches = typestr.equals("all", ignoreCase = true)

            if (!matches)
            {
                player.sendMessage(Chat.format("&cInvalid punishment type: BAN, BLACKLIST, ALL, MUTE, WARN, GHOST_MUTE"))
                return
            }

            for (punishment in PunishmentService.handler.retrieveAll())
            {
                PunishmentService.handler.deleteAsync(punishment.uuid)
                PunishmentService.grants.clear()

            }

            val playerProfile = ProfileGameService.byId(player.uniqueId)
            if (playerProfile != null) {
                addNoteToProfile(player, playerProfile, "Wiped ALL punishments from the database. [Complete Global Wipe]")
            }

            player.sendMessage(Chat.format("&aCleared every punishment from the database!"))

            return
        }

        val typedPunishment = PunishmentService.handler.retrieveAll().filter { it.getGrantable() == foundType }

        for (punishment in typedPunishment)
        {
            PunishmentService.handler.deleteAsync(punishment.uuid)
        }

        for (entry in PunishmentService.grants.entries)
        {
            entry.value.removeIf { it.getGrantable() == foundType }
        }

        val playerProfile = ProfileGameService.byId(player.uniqueId)
        if (playerProfile != null) {
            addNoteToProfile(player, playerProfile, "Wiped ALL " + foundType.niceName + "'s from the database.")
        }

        player.sendMessage(Chat.format("&aWiped all " + foundType.niceName + "'s"))
    }

    @CommandAlias("wipeplayerpunishments")
    @CommandPermission("alchemist.punishments.wipe.player")
    fun wipePlayerPunishments(player: Player, @Name("target") targetProfile: GameProfile, @Name("type") typeString: String) {
        var foundType: PunishmentType? = null

        for (type in PunishmentType.values()) {
            if (type.name.equals(typeString.uppercase(), ignoreCase = true)) {
                foundType = type
            }
        }

        if (foundType == null) {
            if (typeString.equals("all", ignoreCase = true)) {
                val punishments = targetProfile.getPunishments()
                for (punishment in punishments) {
                    PunishmentService.handler.deleteAsync(punishment.uuid)
                }

                PunishmentService.grants.remove(targetProfile.uuid)

                addNoteToProfile(player, targetProfile, "This players punishments were wiped by: ${player.name}.")

                player.sendMessage(Chat.format("&aWiped all punishments for ${targetProfile.username}."))
                return
            } else {
                player.sendMessage(Chat.format("&cInvalid punishment type: BAN, BLACKLIST, ALL, MUTE, WARN, GHOST_MUTE"))
                return
            }
        }

        val punishmentsToRemove = targetProfile.getPunishments().filter { it.getGrantable() == foundType }

        for (punishment in punishmentsToRemove) {
            PunishmentService.handler.deleteAsync(punishment.uuid)
        }

        PunishmentService.grants[targetProfile.uuid]?.removeIf { it.getGrantable() == foundType }

        addNoteToProfile(player, targetProfile, "This players ${foundType.niceName}'s were wiped by: ${player.name}.")

        player.sendMessage(Chat.format("&aWiped all ${foundType.niceName}'s for ${targetProfile.username}."))
    }
}