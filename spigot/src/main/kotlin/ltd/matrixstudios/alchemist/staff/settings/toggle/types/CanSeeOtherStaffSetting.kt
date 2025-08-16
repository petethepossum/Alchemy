package ltd.matrixstudios.alchemist.staff.settings.toggle.types

import ltd.matrixstudios.alchemist.models.profile.GameProfile
import ltd.matrixstudios.alchemist.service.profiles.ProfileGameService
import ltd.matrixstudios.alchemist.staff.settings.toggle.menu.SettingsMenu
import ltd.matrixstudios.alchemist.util.Chat
import ltd.matrixstudios.alchemist.util.menu.Button
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType

class CanSeeOtherStaffSetting(val profile: GameProfile) : Button() {

    private fun isEnabled(): Boolean {
        // Check the boolean value of metadata
        return profile.metadata.has("seeOtherStaff") && profile.metadata.get("seeOtherStaff").asBoolean
    }

    override fun getMaterial(player: Player): Material {
        return if (isEnabled()) Material.EMERALD_BLOCK else Material.REDSTONE_BLOCK
    }

    override fun getDescription(player: Player): MutableList<String> {
        val desc = mutableListOf<String>()
        desc.add(" ")
        desc.add(Chat.format("&7Toggle this setting to make it so"))
        desc.add(Chat.format("&7you can see or not see any other"))
        desc.add(Chat.format("&6Staff Members"))
        desc.add("")

        if (isEnabled()) {
            desc.add(Chat.format("&7► &eCurrently &aon"))
        } else {
            desc.add(Chat.format("&7► &eCurrently &coff"))
        }

        desc.add(" ")
        desc.add(Chat.format("&7Click to edit this value!"))
        desc.add(" ")
        return desc
    }

    override fun getDisplayName(player: Player): String {
        return Chat.format("&eToggle Staff Visibility")
    }

    override fun getData(player: Player): Short {
        return 0
    }

    override fun onClick(player: Player, slot: Int, type: ClickType) {
        if (isEnabled()) {
            profile.metadata.remove("seeOtherStaff")
            player.sendMessage(Chat.format("&eYou have toggled your staff visibility to &coff"))
        } else {
            profile.metadata.addProperty("seeOtherStaff", true)
            player.sendMessage(Chat.format("&eYou have toggled your staff visibility to &aon"))
        }
        ProfileGameService.save(profile)
        SettingsMenu(player).openMenu()
    }
}
