package ltd.matrixstudios.alchemist.profiles.commands.player.menu

import ltd.matrixstudios.alchemist.api.AlchemistAPI
import ltd.matrixstudios.alchemist.commands.alts.menu.AltsMenu
import ltd.matrixstudios.alchemist.commands.notes.menu.PlayerNotesMenu
import ltd.matrixstudios.alchemist.commands.tags.grants.menu.grants.TagGrantsMenu
import ltd.matrixstudios.alchemist.friends.filter.FriendFilter
import ltd.matrixstudios.alchemist.friends.menus.FriendsListMenu
import ltd.matrixstudios.alchemist.grants.menu.grants.GrantsMenu
import ltd.matrixstudios.alchemist.grants.menu.grants.filter.GrantFilter
import ltd.matrixstudios.alchemist.grants.view.GrantsCommand
import ltd.matrixstudios.alchemist.models.profile.GameProfile
import ltd.matrixstudios.alchemist.punishment.commands.menu.HistoryMenu
import ltd.matrixstudios.alchemist.punishment.commands.menu.executed.ExecutedPunishmentHistoryMenu
import ltd.matrixstudios.alchemist.serialize.Serializers
import ltd.matrixstudios.alchemist.util.menu.buttons.*
import ltd.matrixstudios.alchemist.service.expirable.RankGrantService
import ltd.matrixstudios.alchemist.util.Chat
import ltd.matrixstudios.alchemist.util.menu.Button
import ltd.matrixstudios.alchemist.util.menu.Menu
import ltd.matrixstudios.alchemist.util.skull.SkullUtil
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta

class PlayerInformationMenu(val player: Player, val target: GameProfile) : Menu(player) {

    override fun size(buttons: Map<Int, Button>): Int = 54

    override fun getButtons(player: Player): MutableMap<Int, Button> {
        val buttons = mutableMapOf<Int, Button>()
        for (int in 0 until 54) {
            buttons[int] = PlaceholderButton(Material.STAINED_GLASS_PANE, mutableListOf(), "", 7)
        }

        buttons[4] = object : Button() {
            override fun getButtonItem(player: Player) =
                SkullUtil.generate(target.username, Chat.format(AlchemistAPI.getRankDisplay(target.uuid) + "'s &7Profile"))
            override fun getMaterial(player: Player) = Material.SKULL_ITEM
            override fun getDescription(player: Player) = mutableListOf(
                " ",
                Chat.format("&7Rank: &f" + target.getCurrentRank().displayName),
                Chat.format("&7Playtime: &f" + ltd.matrixstudios.alchemist.util.TimeUtil.formatDuration(target.playtimeMillis)),
                " "
            )
            override fun getDisplayName(player: Player) = Chat.format(AlchemistAPI.getRankDisplay(target.uuid) + "'s &7Profile")
            override fun getData(player: Player) = 3.toShort()
            override fun onClick(player: Player, slot: Int, type: org.bukkit.event.inventory.ClickType) {}
        }

        buttons[19] = SimpleActionButton(
            Material.BEACON, mutableListOf(
                " ",
                Chat.format("&7Click this button to view"),
                Chat.format("&7the staff history of this player."),
                Chat.format("&7If they are not staff then nothing"),
                Chat.format("&7will appear here!"),
                " "
            ), "&6Staff History", 0
        ).setBody { player, i, clickType ->
            ExecutedPunishmentHistoryMenu(player, target).openMenu()
        }

        buttons[21] = SimpleActionButton(
            Material.REDSTONE, mutableListOf(
                " ",
                Chat.format("&7Click this button to view"),
                Chat.format("&7the punishment history of"),
                Chat.format("&7this user!"),
                " "
            ), "&6Punishment History", 0
        ).setBody { player, i, clickType ->
            HistoryMenu(target, player).openMenu()
        }

        buttons[23] = SimpleActionButton(
            Material.ANVIL, mutableListOf(
                " ",
                Chat.format("&7Click this button to view"),
                Chat.format("&7every alternate account of"),
                Chat.format("&7this user!"),
                " "
            ), "&6Alternate Accounts", 0
        ).setBody { player, i, clickType ->
            target.getAltAccounts()
                .thenAccept { accounts ->
                    AltsMenu(player, target, accounts).updateMenu()
                }
        }

        buttons[25] = SimpleActionButton(
            Material.WOOL, mutableListOf(
                " ",
                Chat.format("&7Click this button to view"),
                Chat.format("&7the rank grants of this"),
                Chat.format("&7user!"),
                " "
            ), "&6Rank Grants", 13
        ).setBody { player, i, clickType ->
            GrantsMenu(
                player,
                target,
                GrantsCommand.getViewableGrants(player, RankGrantService.getFromCache(target.uuid).toMutableList()),
                GrantFilter.ALL
            ).updateMenu()
        }

        buttons[37] = SimpleActionButton(
            Material.PAPER, mutableListOf(
                " ",
                Chat.format("&7Click this button to view"),
                Chat.format("&7the notes of this user"),
                " "
            ), "&6Notes", 0
        ).setBody { player, i, clickType ->
            PlayerNotesMenu(player, target).updateMenu()
        }

        buttons[39] = SimpleActionButton(
            Material.REDSTONE_COMPARATOR, mutableListOf(
                " ",
                Chat.format("&7Click this button to view"),
                Chat.format("&7the friends of this user"),
                " "
            ), "&6Friends", 0
        ).setBody { player, i, clickType ->
            FriendsListMenu(player, target, FriendFilter.ALL).updateMenu()
        }

        buttons[41] = SimpleActionButton(
            Material.NAME_TAG, mutableListOf(
                " ",
                Chat.format("&7Click this button to view"),
                Chat.format("&7the tag grants of this"),
                Chat.format("&7user"),
                " "
            ), "&6Tag Grants", 0
        ).setBody { player, i, clickType ->
            TagGrantsMenu(player, target).updateMenu()
        }

        buttons[43] = SimpleActionButton(
            Material.CAULDRON_ITEM, mutableListOf(
                " ",
                Chat.format("&7Click this button to view"),
                Chat.format("&7the raw JSON dump of this"),
                Chat.format("&7user's profile"),
                " "
            ), "&6JSON Dump", 0
        ).setBody { player, i, clickType ->
            val gson = Serializers.GSON.toJson(target)
            player.closeInventory()
            player.sendMessage(gson)
        }

        return buttons
    }

    override fun getTitle(player: Player): String {
        return Chat.format("&7Viewing: &r" + AlchemistAPI.getRankDisplay(target.uuid))
    }

    class PlayerSelfInformationMenu(val player: Player, val profile: GameProfile) : Menu(player) {

        override fun size(buttons: Map<Int, Button>): Int = 54

        override fun getButtons(player: Player): MutableMap<Int, Button> {
            val buttons = mutableMapOf<Int, Button>()
            for (int in 0 until 54) {
                buttons[int] = PlaceholderButton(Material.STAINED_GLASS_PANE, mutableListOf(), "", 7)
            }

            buttons[4] = object : Button() {
                override fun getButtonItem(player: Player): ItemStack {
                    val item = ItemStack(Material.SKULL_ITEM, 1, 3.toShort())
                    val meta = item.itemMeta as SkullMeta
                    meta.displayName = Chat.format("&6Profile Overview")
                    meta.lore = listOf(
                        " ",
                        Chat.format("&7Rank: &f" + profile.getCurrentRank().displayName),
                        Chat.format("&7Playtime: &f" + ltd.matrixstudios.alchemist.util.TimeUtil.formatDuration(profile.playtimeMillis)),
                        " "
                    )
                    item.itemMeta = meta
                    return item
                }

                override fun getMaterial(player: Player): Material = Material.SKULL_ITEM
                override fun getDescription(player: Player): MutableList<String> = mutableListOf()
                override fun getDisplayName(player: Player): String = Chat.format("&6Profile Overview")
                override fun getData(player: Player): Short = 3
                override fun onClick(player: Player, slot: Int, type: org.bukkit.event.inventory.ClickType) {}
            }

            buttons[19] = SimpleActionButton(
                Material.GOLD_INGOT, mutableListOf(
                    " ",
                    Chat.format("&7Your current coin balance:"),
                    Chat.format("&e" + profile.coins),
                    " "
                ), "&6Coins", 0
            )

            buttons[21] = SimpleActionButton(
                Material.WOOL, mutableListOf(
                    " ",
                    Chat.format("&7View your rank grants."),
                    " "
                ), "&6Rank Grants", 13
            ).setBody { player, i, clickType ->
                GrantsMenu(
                    player,
                    profile,
                    GrantsCommand.getViewableGrants(player, RankGrantService.getFromCache(profile.uuid).toMutableList()),
                    GrantFilter.ALL
                ).updateMenu()
            }

            buttons[23] = SimpleActionButton(
                Material.ANVIL, mutableListOf(
                    " ",
                    Chat.format("&7View your alternate accounts."),
                    " "
                ), "&6Alternate Accounts", 0
            ).setBody { player, i, clickType ->
                profile.getAltAccounts()
                    .thenAccept { accounts ->
                        AltsMenu(player, profile, accounts).updateMenu()
                    }
            }

            buttons[25] = SimpleActionButton(
                Material.NAME_TAG, mutableListOf(
                    " ",
                    Chat.format("&7View your tag grants."),
                    " "
                ), "&6Tag Grants", 0
            ).setBody { player, i, clickType ->
                TagGrantsMenu(player, profile).updateMenu()
            }

            buttons[37] = SimpleActionButton(
                Material.REDSTONE_COMPARATOR, mutableListOf(
                    " ",
                    Chat.format("&7View your friends list."),
                    " "
                ), "&6Friends", 0
            ).setBody { player, i, clickType ->
                FriendsListMenu(player, profile, FriendFilter.ALL).updateMenu()
            }

            buttons[43] = SimpleActionButton(
                Material.CAULDRON_ITEM, mutableListOf(
                    " ",
                    Chat.format("&7View the raw JSON dump of your profile."),
                    " "
                ), "&6JSON Dump", 0
            ).setBody { player, i, clickType ->
                val gson = Serializers.GSON.toJson(profile)
                player.closeInventory()
                player.sendMessage(gson)
            }

            return buttons
        }

        override fun getTitle(player: Player): String {
            return Chat.format("&7Your Profile: &r" + AlchemistAPI.getRankDisplay(profile.uuid))
        }
    }
}