package ltd.matrixstudios.alchemist.staff.mode

import ltd.matrixstudios.alchemist.Alchemist
import ltd.matrixstudios.alchemist.api.AlchemistAPI
import ltd.matrixstudios.alchemist.redis.AsynchronousRedisSender
import ltd.matrixstudios.alchemist.staff.alerts.StaffActionAlertPacket
import org.bukkit.Bukkit
import org.bukkit.entity.Player

object StaffSuiteVisibilityHandler
{

    fun onDisableVisbility(player: Player)
    {
        Bukkit.getOnlinePlayers().forEach {
            it.showPlayer(player)
        }
    }

    fun onEnableVisibility(player: Player)
    {
        Bukkit.getOnlinePlayers().filter { !it.hasPermission("alchemist.staff") }.forEach { it.hidePlayer(player) }

        val profile = AlchemistAPI.syncFindProfile(player.uniqueId)?.hasMetadata("seeOtherStaff") ?: return

        if (profile)
        {
            Bukkit.getOnlinePlayers().filter {
                it.hasPermission("alchemist.staff")
            }.forEach {
                player.showPlayer(it)
            }
        }
    }
    fun applyVisibilityForJoiningPlayer(joiningPlayer: Player) {
        val vanishedPlayers = Bukkit.getOnlinePlayers().filter { it.hasMetadata("vanish") }

        if (joiningPlayer.hasPermission("alchemist.staff")) {
            val profile = AlchemistAPI.syncFindProfile(joiningPlayer.uniqueId)
            val canSeeOtherStaff = profile?.hasMetadata("seeOtherStaff") ?: false

            if (canSeeOtherStaff) {
                // Show all vanished staff to this player
                vanishedPlayers.forEach { joiningPlayer.showPlayer(it) }
            } else {
                // Hide all vanished staff from this player
                vanishedPlayers.forEach { joiningPlayer.hidePlayer(it) }
            }
        } else {
            // Non-staff never see vanished players
            vanishedPlayers.forEach { joiningPlayer.hidePlayer(it) }
        }
    }
}