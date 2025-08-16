package ltd.matrixstudios.alchemist.staff.settings.toggle.types

import ltd.matrixstudios.alchemist.models.profile.GameProfile
import ltd.matrixstudios.alchemist.service.profiles.ProfileGameService
import ltd.matrixstudios.alchemist.staff.settings.toggle.menu.SettingsMenu
import ltd.matrixstudios.alchemist.util.Chat
import ltd.matrixstudios.alchemist.util.menu.Button
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType

class AllMessagesAreStaffSetting(val profile: GameProfile) : Button() {

    override fun getMaterial(player: Player): Material {
        val currentChannel = profile.metadata.get("chat-channel")?.asString ?: "global"
        return if (currentChannel.lowercase() == "staff") Material.EMERALD_BLOCK else Material.REDSTONE_BLOCK
    }

    override fun getDescription(player: Player): MutableList<String> {
        val desc = mutableListOf<String>()
        desc.add(" ")
        desc.add(Chat.format("&7Toggle this setting to make it so"))
        desc.add(Chat.format("&7every new message you send automatically"))
        desc.add(Chat.format("&7goes into staff chat"))
        desc.add("")

        val currentChannel = profile.metadata.get("chat-channel")?.asString ?: "global"
        desc.add(Chat.format("&7► &eCurrently: &f${currentChannel.replaceFirstChar { it.uppercase() }}"))
        desc.add(" ")
        desc.add(Chat.format("&7Click to switch channel to Staff Chat"))
        desc.add(" ")
        return desc
    }

    override fun getDisplayName(player: Player): String =
        Chat.format("&eChat Channel: ${profile.metadata.get("chat-channel")?.asString?.replaceFirstChar { it.uppercase() } ?: "Global"}")

    override fun getData(player: Player): Short = 0 // Not needed since we're using blocks directly

    override fun onClick(player: Player, slot: Int, type: ClickType) {
        profile.metadata.addProperty("chat-channel", "staff")
        ProfileGameService.save(profile)

        player.sendMessage(Chat.format("&eYour chat channel has been set to &fStaff"))

        SettingsMenu(player).openMenu()
    }
}
