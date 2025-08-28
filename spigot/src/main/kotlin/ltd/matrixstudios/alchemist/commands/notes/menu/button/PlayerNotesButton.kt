package ltd.matrixstudios.alchemist.commands.notes.menu.button

import com.cryptomorin.xseries.NoteBlockMusic
import com.cryptomorin.xseries.XSound
import com.cryptomorin.xseries.XSound.ENTITY_VILLAGER_NO
import ltd.matrixstudios.alchemist.api.AlchemistAPI
import ltd.matrixstudios.alchemist.models.profile.GameProfile
import ltd.matrixstudios.alchemist.models.profile.notes.ProfileNote
import ltd.matrixstudios.alchemist.service.profiles.ProfileGameService
import ltd.matrixstudios.alchemist.util.Chat
import ltd.matrixstudios.alchemist.util.items.ItemBuilder
import ltd.matrixstudios.alchemist.util.menu.Button
import ltd.matrixstudios.alchemist.util.skull.SkullUtil
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack
import java.util.*

class PlayerNotesButton(val note: ProfileNote, val targetProfile: GameProfile) : Button()
{

    override fun getMaterial(player: Player): Material
    {
        return Material.SKULL_ITEM
    }

    override fun getDescription(player: Player): MutableList<String>
    {
        val desc = mutableListOf<String>()

        desc.add(Chat.format("&7&m-------------------"))
        desc.add(Chat.format("&eAdded by: &c" + AlchemistAPI.getRankDisplay(note.author)))
        desc.add(Chat.format("&eNote: &c" + note.content))
        desc.add(Chat.format("&7&m-------------------"))
        desc.add(Chat.format("&eClick to remove this note"))
        desc.add(Chat.format("&7&m-------------------"))

        return desc
    }

    override fun getDisplayName(player: Player): String
    {
        return Chat.format("&e${Date(note.createdAt)}")
    }

    override fun getData(player: Player): Short
    {
        return 3
    }

    override fun getButtonItem(player: Player): ItemStack
    {
        val authorProfile = ProfileGameService.byId(note.author)
        val authorName = authorProfile?.username ?: "Steve"

        val skull = SkullUtil.generate(authorName, getDisplayName(player))

        return ItemBuilder.copyOf(skull)
            .setLore(getDescription(player))
            .build()
    }

    override fun onClick(player: Player, slot: Int, type: ClickType)
    {
        if (!note.deletable)
        {
            player.sendMessage(Chat.format("&cYou are not allowed to remove this note."))
            XSound.play("ENTITY_VILLAGER_NO", { soundPlayer ->
                soundPlayer.forPlayers(player)
            })
            return
        }
        else
            targetProfile.notes.find { it == note }?.let { targetProfile.notes.remove(it) }

            ProfileGameService.save(targetProfile)

            player.sendMessage(Chat.format("&cRemoved note from ${targetProfile.username}."))
            player.closeInventory()
    }
}