package ltd.matrixstudios.alchemist.friends.menus

import ltd.matrixstudios.alchemist.api.AlchemistAPI
import ltd.matrixstudios.alchemist.friends.filter.FriendFilter
import ltd.matrixstudios.alchemist.friends.filter.button.FilterButton
import ltd.matrixstudios.alchemist.models.profile.GameProfile
import ltd.matrixstudios.alchemist.util.Chat
import ltd.matrixstudios.alchemist.util.TimeUtil
import ltd.matrixstudios.alchemist.util.items.ItemBuilder
import ltd.matrixstudios.alchemist.util.menu.Button
import ltd.matrixstudios.alchemist.util.menu.pagination.PaginatedMenu
import ltd.matrixstudios.alchemist.util.skull.SkullUtil
import ltd.matrixstudios.alchemist.redis.RedisOnlineStatusService
import ltd.matrixstudios.alchemist.service.profiles.ProfileGameService
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack
import java.util.concurrent.TimeUnit

class FriendsListMenu(val player: Player, val profile: GameProfile, val filter: FriendFilter) :
    PaginatedMenu(18, player)
{

    override fun getHeaderItems(player: Player): MutableMap<Int, Button>
    {
        val buttons = mutableMapOf<Int, Button>()

        buttons[4] = FilterButton(filter, profile)
        return buttons
    }

    override fun getPagesButtons(player: Player): MutableMap<Int, Button>
    {
        val buttons = mutableMapOf<Int, Button>()
        var index = 0

        val filtered = get(profile, filter)

        for (item in filtered)
        {
            buttons[index++] = FriendButton(item)
        }

        return buttons

    }

    override fun getTitle(player: Player): String
    {
        return "Your Friends!"
    }

    class FriendButton(val profile: GameProfile) : Button()
    {
        override fun getMaterial(player: Player): Material
        {
            return Material.DIRT
        }

        override fun getDescription(player: Player): MutableList<String>
        {
            val desc = mutableListOf<String>()
            val rank = AlchemistAPI.findRank(profile.uuid)

            desc.add(Chat.format("&7&m-------------------"))
            desc.add(Chat.format("&eRank: &f" + rank.color + rank.displayName))
            desc.add(Chat.format("&eTotal Friends: &f" + profile.friends.size))

            val isOnline = RedisOnlineStatusService.isOnline(profile.uuid)
            if (isOnline)
            {
                val server = RedisOnlineStatusService.getOnlineServer(profile.uuid) ?: "Unknown"
                desc.add(Chat.format("&ePlaying: &f$server"))
            }
            desc.add(" ")
            if (isOnline)
            {
                desc.add(Chat.format("&aCurrently Online"))
            } else
            {
                desc.add(Chat.format("&cCurrently Offline"))
                desc.add(
                    Chat.format(
                        "&7&oOffline For " + TimeUtil.formatDuration(
                            System.currentTimeMillis().minus(profile.lastSeenAt)
                        )
                    )
                )
            }
            desc.add("")
            desc.add("&c&l[Right Click] &7to &7&lremove this friend")
            desc.add(Chat.format("&7&m-------------------"))

            return desc
        }

        override fun getDisplayName(player: Player): String {
            return Chat.format(profile.getCurrentRank().prefix + profile.getRankDisplay())
        }

        override fun getButtonItem(player: Player): ItemStack
        {
            val name = Chat.format(AlchemistAPI.getRankDisplay(profile.uuid))
            val desc = getDescription(player)
            val skullItem = SkullUtil.generate(profile.username, name)

            return ItemBuilder.copyOf(skullItem).setLore(desc.toList()).name(name).build()
        }

        override fun getData(player: Player): Short
        {
            return 0
        }


        override fun onClick(player: Player, slot: Int, type: ClickType) {
            if (type == ClickType.RIGHT) {
                val playerProfile = AlchemistAPI.quickFindProfile(player.uniqueId).join() ?: run {
                    player.sendMessage(Chat.format("&cYour profile does not exist!"))
                    return
                }

                if (!playerProfile.friends.contains(profile.uuid)) {
                    player.sendMessage(Chat.format("&cThat player is not your friend!"))
                    return
                }

                // Remove friend both ways
                playerProfile.friends.remove(profile.uuid)
                profile.friends.remove(player.uniqueId)

                ProfileGameService.save(playerProfile)
                ProfileGameService.save(profile)

                player.sendMessage(Chat.format("&e&l[Friends] &aYou have &e&lremoved&r ${profile.getCurrentRank().prefix + profile.getRankDisplay()} &afrom your friends list."))

                // Notify removed friend if online
                val onlinePlayer = player.server.getPlayer(profile.uuid)
                onlinePlayer?.sendMessage(Chat.format("&e&l[Friends] &f${player.displayName} &ahas removed you from their friends list."))

                // Refresh the menu so the change shows immediately
                (player.openInventory.topInventory.holder as? FriendsListMenu)?.updateMenu()
            }
        }


    }

    fun get(profile: GameProfile, filter: FriendFilter): List<GameProfile>
    {
        if (filter == FriendFilter.ALL) return profile.supplyFriendsAsProfiles().get()

        val baseList = profile.supplyFriendsAsProfiles().get()

        //statuses
        if (filter == FriendFilter.ONLINE)
        {
            return baseList.filter { RedisOnlineStatusService.isOnline(it.uuid) }
        } else if (filter == FriendFilter.OFFLINE)
        {
            return baseList.filter { !RedisOnlineStatusService.isOnline(it.uuid) }
        }

        //attributes
        if (filter == FriendFilter.YOUR_SERVER)
        {
            return baseList.filter { it.metadata.get("server").asString == profile.metadata.get("server").asString }
        } else if (filter == FriendFilter.RECENTLY_JOINED)
        {
            return baseList.filter { System.currentTimeMillis().minus(it.lastSeenAt) <= TimeUnit.MINUTES.toMillis(30L) }
        }

        return baseList

    }
}