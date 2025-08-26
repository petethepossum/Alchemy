package ltd.matrixstudios.alchemist.chat

import co.aikar.commands.BaseCommand
import ltd.matrixstudios.alchemist.AlchemistSpigotPlugin
import ltd.matrixstudios.alchemist.chat.commands.ChatCommands
import ltd.matrixstudios.alchemist.module.PluginModule
import ltd.matrixstudios.alchemist.profiles.getProfile
import org.bukkit.entity.Player

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
    fun getChatChannelForPlayer(player: Player): ChatMode {
        val profile = player.getProfile() ?: return ChatMode.GLOBAL
        val channel = profile.metadata.get("chat-channel")?.asString ?: "global"

        return when (channel.lowercase()) {
            "global" -> ChatMode.GLOBAL
            "staff" -> ChatMode.STAFF
            "admin" -> ChatMode.ADMIN
            else -> ChatMode.GLOBAL
        }
    }

    fun getColoredChatChannel(player: Player): String {
        val mode = getChatChannelForPlayer(player)
        return "${mode.displayColour}${mode.displayName}"
    }



    override fun getModularConfigOption(): Boolean
    {
        return AlchemistSpigotPlugin.instance.config.getBoolean("modules.chat")
    }


    enum class ChatMode(val displayName: String, val permission: String?, val displayColour: String) {
        GLOBAL("Global", null, "&7"),
        STAFF("Staff", "alchemist.staff", "&a"),
        ADMIN("Admin", "alchemist.admin", "&c")
    }


}