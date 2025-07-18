package ltd.matrixstudios.alchemist.profiles

import co.aikar.commands.BaseCommand
import ltd.matrixstudios.alchemist.AlchemistSpigotPlugin
import ltd.matrixstudios.alchemist.module.PluginModule
import ltd.matrixstudios.alchemist.profiles.commands.auth.AuthCommands
import ltd.matrixstudios.alchemist.profiles.commands.player.*
import ltd.matrixstudios.alchemist.profiles.commands.sibling.SiblingCommands
import ltd.matrixstudios.alchemist.profiles.commands.website.RegisterCommand
import ltd.matrixstudios.alchemist.staff.commands.*
import ltd.matrixstudios.alchemist.staff.requests.commands.ReportCommand
import ltd.matrixstudios.alchemist.staff.requests.commands.RequestCommand
import ltd.matrixstudios.alchemist.staff.settings.toggle.SettingsCommand
import ltd.matrixstudios.alchemist.util.Chat

/**
 * Class created on 7/21/2023

 * @author 98ping
 * @project Alchemist
 * @website https://solo.to/redis
 */
object ProfileModule : PluginModule
{

    override fun onLoad()
    {
        val start = System.currentTimeMillis()
        BukkitProfileAdaptation.loadAllEvents()
        val plugin = AlchemistSpigotPlugin.instance //this is a hacky way of doing this, but it works
        plugin.server.pluginManager.registerEvents(
            ltd.matrixstudios.alchemist.party.PartyDisconnectListener(), plugin
        )

        Chat.sendConsoleMessage(
            "&b[Profiles] &fAll profile events loaded in &b" + System.currentTimeMillis().minus(start) + "ms"
        )
    }

    override fun getCommands(): MutableList<BaseCommand>
    {
        val list = mutableListOf<BaseCommand>()

        if (AlchemistSpigotPlugin.instance.config.getBoolean("freeRank.enabled"))
        {
            list.add(FreerankCommand())
        }

        list.add(ListCommand())
        list.add(LookupCommand())
        list.add(PlayerAdminCommand())
        list.add(SudoCommand())
        list.add(WipeGrantsCommand)
        list.add(WipeProfileCommand())
        list.add(SiblingCommands())

        list.add(JumpToPlayerCommand())
        list.add(OnlineStaffCommand())
        list.add(StaffchatCommand())
        list.add(TimelineCommand())
        list.add(StaffLeaderboardCommand)

        if (AlchemistSpigotPlugin.instance.config.getBoolean("modules.websiteCommands"))
        {
            list.add(RegisterCommand)
        }

        list.add(ReportCommand())
        list.add(RequestCommand())

        if (AlchemistSpigotPlugin.instance.config.getBoolean("modules.2fa"))
        {
            list.add(AuthCommands())
        }

        list.add(SettingsCommand())

        return list
    }

    override fun getModularConfigOption(): Boolean
    {
        return true
    }
}