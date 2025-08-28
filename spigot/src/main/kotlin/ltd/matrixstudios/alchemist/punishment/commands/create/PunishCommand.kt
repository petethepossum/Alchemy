package ltd.matrixstudios.alchemist.punishment.commands.create

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.*
import ltd.matrixstudios.alchemist.models.profile.GameProfile
import ltd.matrixstudios.alchemist.punishment.commands.menu.PunishMenu
import org.bukkit.entity.Player

class PunishCommand : BaseCommand() {

    @CommandAlias("punish|p")
    @CommandPermission("alchemist.punishments.punish")
    @CommandCompletion("@gameprofile")
    @Syntax("<target> [reason]")
    fun punish(sender: Player, @Name("target") gameProfile: GameProfile, @Optional @Name("reason") reason: String?) {
        PunishMenu(gameProfile, sender, reason).openMenu()
    }
}