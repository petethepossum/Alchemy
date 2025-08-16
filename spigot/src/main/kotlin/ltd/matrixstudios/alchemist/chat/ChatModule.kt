package ltd.matrixstudios.alchemist.chat

import co.aikar.commands.BaseCommand
import ltd.matrixstudios.alchemist.AlchemistSpigotPlugin
import ltd.matrixstudios.alchemist.chat.commands.ChatCommands
import ltd.matrixstudios.alchemist.module.PluginModule

object ChatModule : PluginModule
{

    override fun onLoad()
    {
    }

    override fun getCommands(): MutableList<BaseCommand>
    {
        val list = mutableListOf<BaseCommand>()

        list.add(ChatCommands)

        return list
    }

    override fun getModularConfigOption(): Boolean
    {
        return AlchemistSpigotPlugin.instance.config.getBoolean("modules.chat")
    }


    enum class ChatMode(val displayName: String, val permission: String?) {
        GLOBAL("Global", null),
        STAFF("Staff", "alchemist.staff"),
        ADMIN("Admin", "alchemist.admin")
    }

}