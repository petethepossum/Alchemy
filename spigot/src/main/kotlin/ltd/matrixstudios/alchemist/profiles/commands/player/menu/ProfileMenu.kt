package ltd.matrixstudios.alchemist.profiles.commands.player.menu

import com.cryptomorin.xseries.XMaterial
import ltd.matrixstudios.alchemist.api.AlchemistAPI
import ltd.matrixstudios.alchemist.commands.alts.menu.AltsMenu
import ltd.matrixstudios.alchemist.commands.tags.grants.menu.grants.TagGrantsMenu
import ltd.matrixstudios.alchemist.friends.filter.FriendFilter
import ltd.matrixstudios.alchemist.friends.menus.FriendsListMenu
import ltd.matrixstudios.alchemist.grants.menu.grants.GrantsMenu
import ltd.matrixstudios.alchemist.grants.menu.grants.filter.GrantFilter
import ltd.matrixstudios.alchemist.grants.view.GrantsCommand
import ltd.matrixstudios.alchemist.models.profile.GameProfile
import ltd.matrixstudios.alchemist.serialize.Serializers
import ltd.matrixstudios.alchemist.service.expirable.RankGrantService
import ltd.matrixstudios.alchemist.util.Chat
import ltd.matrixstudios.alchemist.util.TimeUtil
import ltd.matrixstudios.alchemist.util.menu.Button
import ltd.matrixstudios.alchemist.util.menu.Menu
import ltd.matrixstudios.alchemist.util.menu.buttons.PlaceholderButton
import ltd.matrixstudios.alchemist.util.menu.buttons.SimpleActionButton
import ltd.matrixstudios.alchemist.util.skull.SkullUtil
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class ProfileMenu(val player: Player, val profile: GameProfile) : Menu(player) {

    override fun size(buttons: Map<Int, Button>): Int = 54

    override fun getButtons(player: Player): MutableMap<Int, Button> {
        val buttons = mutableMapOf<Int, Button>()

        for (i in 0 until 54) {
            buttons[i] = PlaceholderButton(
                XMaterial.LIGHT_GRAY_STAINED_GLASS_PANE.parseMaterial()!!,
                mutableListOf(),
                "",
                7
            )
        }


        buttons[4] = object : Button() {
            override fun getButtonItem(player: Player): ItemStack =
                SkullUtil.generate(
                    profile.username,
                    "&6Your Profile",
                    getDescription(player)
                )

            override fun getMaterial(player: Player) = XMaterial.PLAYER_HEAD.parseMaterial()!!
            override fun getDescription(player: Player) = mutableListOf(
                " ",
                "&7Rank: &f${profile.getCurrentRank().displayName}",
                "&7Playtime: &f${TimeUtil.formatDuration(profile.playtimeMillis)}",
                "&7Coins: &f${profile.coins}",
                " "
            ).map(Chat::format).toMutableList()

            override fun getDisplayName(player: Player) = Chat.format("&6Your Profile")
            override fun getData(player: Player) = 3.toShort()
            override fun onClick(player: Player, slot: Int, type: org.bukkit.event.inventory.ClickType) {}
        }


        buttons[19] = SimpleActionButton(
            XMaterial.GOLD_INGOT.parseMaterial()!!, mutableListOf(
                " ",
                Chat.format("&7You currently have &e${profile.coins} &7coins."),
                " "
            ), "&6Coins", 0
        )


        buttons[21] = SimpleActionButton(
            XMaterial.WHITE_WOOL.parseMaterial()!!, mutableListOf(
                " ",
                Chat.format("&7View all of your active and past rank grants."),
                " "
            ), "&6Rank Grants", 13
        ).setBody { player, _, _ ->
            GrantsMenu(
                player,
                profile,
                GrantsCommand.getViewableGrants(player, RankGrantService.getFromCache(profile.uuid).toMutableList()),
                GrantFilter.ALL
            ).updateMenu()
        }


        buttons[23] = SimpleActionButton(
            XMaterial.ANVIL.parseMaterial()!!, mutableListOf(
                " ",
                Chat.format("&7View accounts linked to you."),
                " "
            ), "&6Alternate Accounts", 0
        ).setBody { player, _, _ ->
            profile.getAltAccounts().thenAccept { accounts ->
                AltsMenu(player, profile, accounts).updateMenu()
            }
        }

        buttons[25] = SimpleActionButton(
            XMaterial.NAME_TAG.parseMaterial()!!, mutableListOf(
                " ",
                Chat.format("&7View and manage your unlocked tags."),
                " "
            ), "&6Tags", 0
        ).setBody { player, _, _ -> TagGrantsMenu(player, profile).updateMenu() }

        val friendsHeadBase64 =
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmM0ZDdlZTRiMWM5NDE3YWI0YjQ3MmQ3YjY3ZTc3N2ZhNGE2YWRmN2U4ODc5OTg1ODhiYTRkYWU2NTg0OTliMSJ9fX0="

        buttons[37] = object : Button() {
            override fun getButtonItem(player: Player): ItemStack {
                return SkullUtil.applyCustomHead(
                    XMaterial.PLAYER_HEAD.parseItem()!!,
                    friendsHeadBase64,
                    "&6Friends",
                    listOf(
                        " ",
                        "&7View and manage your friends list.",
                        " "
                    ).map(Chat::format).toMutableList()
                )
            }

            override fun getMaterial(player: Player) = XMaterial.PLAYER_HEAD.parseMaterial()!!
            override fun getDescription(player: Player): MutableList<String>? = null
            override fun getDisplayName(player: Player): String? = Chat.format("&6Friends")
            override fun getData(player: Player): Short = 3
            override fun onClick(player: Player, slot: Int, type: org.bukkit.event.inventory.ClickType) {
                FriendsListMenu(player, profile, FriendFilter.ALL).updateMenu()
            }
        }

        // JSON Dump
        buttons[43] = SimpleActionButton(
            XMaterial.CAULDRON.parseMaterial()!!, mutableListOf(
                " ",
                Chat.format("&7View the raw JSON dump of your profile."),
                " "
            ), "&6JSON Dump", 0
        ).setBody { player, _, _ ->
            val gson = Serializers.GSON.toJson(profile)
            player.closeInventory()
            player.sendMessage(gson)
        }

        return buttons
    }

    override fun getTitle(player: Player): String =
        Chat.format("&7Your Profile: &r${AlchemistAPI.getRankDisplay(profile.uuid)}")
}
