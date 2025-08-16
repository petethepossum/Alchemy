package ltd.matrixstudios.alchemist.profiles.connection.postlog.tasks

import ltd.matrixstudios.alchemist.AlchemistSpigotPlugin
import ltd.matrixstudios.alchemist.staff.mode.StaffSuiteVisibilityHandler
import ltd.matrixstudios.alchemist.profiles.connection.postlog.BukkitPostLoginTask
import org.bukkit.Bukkit
import org.bukkit.entity.Player

object VanishVisibility : BukkitPostLoginTask
{
    override fun run(player: Player)
    {
        Bukkit.getScheduler().runTaskLater(AlchemistSpigotPlugin.instance, {
            StaffSuiteVisibilityHandler.applyVisibilityForJoiningPlayer(player)
        }, 10L)  // ensure profile and metadata are loaded
    }
}