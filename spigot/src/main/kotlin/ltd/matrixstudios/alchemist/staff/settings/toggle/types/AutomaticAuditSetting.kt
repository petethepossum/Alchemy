package ltd.matrixstudios.alchemist.staff.settings.toggle.types

import ltd.matrixstudios.alchemist.models.profile.GameProfile
import ltd.matrixstudios.alchemist.service.profiles.ProfileGameService
import ltd.matrixstudios.alchemist.staff.settings.toggle.menu.SettingsMenu
import ltd.matrixstudios.alchemist.util.Chat
import ltd.matrixstudios.alchemist.util.menu.Button
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType

class AutomaticAuditSetting(val profile: GameProfile) : Button() {

    override fun getMaterial(player: Player): Material {
        return if (profile.hasMetadata("toggleAudit")) Material.EMERALD_BLOCK else Material.REDSTONE_BLOCK
    }

    override fun getDescription(player: Player): MutableList<String> {
        val desc = mutableListOf<String>()
        desc.add(" ")
        desc.add(Chat.format("&7Toggle this setting to make it so"))
        desc.add(Chat.format("&7every time you join you are subscribed"))
        desc.add(Chat.format("&7to the server audit logs."))
        desc.add("")
        desc.add(Chat.format("&cThis setting requires a permission"))
        desc.add("")

        if (profile.hasMetadata("toggleAudit")) {
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
        return Chat.format("&eToggle Audit Logs on Join")
    }

    override fun getData(player: Player): Short {
        return 0
    }

    override fun onClick(player: Player, slot: Int, type: ClickType) {
        if (profile.hasMetadata("toggleAudit")) {
            profile.metadata.remove("toggleAudit")
            player.sendMessage(Chat.format("&eYou have toggled your audit log on join &coff"))
        } else {
            profile.metadata.addProperty("toggleAudit", true)
            player.sendMessage(Chat.format("&eYou have toggled your audit log on join &aon"))
        }

        ProfileGameService.save(profile)
        SettingsMenu(player).openMenu()
    }
}
