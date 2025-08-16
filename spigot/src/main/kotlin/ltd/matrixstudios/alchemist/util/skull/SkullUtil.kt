package ltd.matrixstudios.alchemist.util.skull

import com.cryptomorin.xseries.XMaterial
import com.cryptomorin.xseries.profiles.builder.XSkull
import com.cryptomorin.xseries.profiles.objects.ProfileInputType
import com.cryptomorin.xseries.profiles.objects.Profileable
import ltd.matrixstudios.alchemist.util.Chat
import org.bukkit.inventory.ItemStack

object SkullUtil {

    /**
     * Generate a skull for a specific Minecraft username.
     *
     * @param owner The player username
     * @param displayName Display name for the skull
     * @param lore Optional lore
     */
    fun generate(owner: String, displayName: String, lore: List<String>? = null): ItemStack {
        val skull: ItemStack = XMaterial.PLAYER_HEAD.parseItem()!!
        XSkull.of(skull)
            .profile(Profileable.of(ProfileInputType.USERNAME, owner))
            .apply()

        val meta = skull.itemMeta
        if (meta != null) {
            meta.setDisplayName(Chat.format(displayName))
            if (lore != null) meta.lore = lore.map { Chat.format(it) }
            skull.itemMeta = meta
        }

        return skull
    }

    /**
     * Apply a custom head texture (Base64) to an existing ItemStack.
     *
     * @param skull The ItemStack to modify
     * @param base64 The Base64 texture string
     * @param displayName Display name for the skull
     * @param lore Optional lore
     */
    fun applyCustomHead(
        skull: ItemStack,
        base64: String,
        displayName: String,
        lore: List<String> = emptyList()
    ): ItemStack {
        XSkull.of(skull)
            .profile(Profileable.of(ProfileInputType.BASE64, base64))
            .apply()

        val meta = skull.itemMeta
        if (meta != null) {
            meta.setDisplayName(Chat.format(displayName))
            meta.lore = lore.map { Chat.format(it) }
            skull.itemMeta = meta
        }

        return skull
    }
}
