package ltd.matrixstudios.alchemist.profiles.commands.player.menu

import com.cryptomorin.xseries.XMaterial
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

class PlayerInformationMenu(val player: Player, val target: GameProfile) : Menu(player) {

    override fun size(buttons: Map<Int, Button>): Int = 54

    override fun getButtons(player: Player): MutableMap<Int, Button> {
        val buttons = mutableMapOf<Int, Button>()

        // Fill placeholders
        for (i in 0 until 54) {
            buttons[i] =
                PlaceholderButton(XMaterial.LIGHT_GRAY_STAINED_GLASS_PANE.parseMaterial()!!, mutableListOf(), "", 7)
        }

        // Player Profile Skull
        buttons[4] = object : Button() {
            override fun getButtonItem(player: Player): ItemStack =
                SkullUtil.generate(
                    target.username,
                    Chat.format("${AlchemistAPI.getRankDisplay(target.uuid)}'s &7Profile")
                )

            override fun getMaterial(player: Player) = XMaterial.PLAYER_HEAD.parseMaterial()!!
            override fun getDescription(player: Player) = mutableListOf(
                " ",
                Chat.format("&7Rank: &f${target.getCurrentRank().displayName}"),
                Chat.format("&7Playtime: &f${TimeUtil.formatDuration(target.playtimeMillis)}"),
                " "
            )

            override fun getDisplayName(player: Player) =
                Chat.format("${AlchemistAPI.getRankDisplay(target.uuid)}'s &7Profile")

            override fun getData(player: Player) = 3.toShort()
            override fun onClick(player: Player, slot: Int, type: org.bukkit.event.inventory.ClickType) {}
        }

        // Staff History Button
        buttons[19] = SimpleActionButton(
            XMaterial.BEACON.parseMaterial()!!, mutableListOf(
                " ",
                Chat.format("&7Click this button to view"),
                Chat.format("&7the staff history of this player."),
                Chat.format("&7If they are not staff then nothing"),
                Chat.format("&7will appear here!"),
                " "
            ), "&6Staff History", 0
        ).setBody { player, _, _ -> ExecutedPunishmentHistoryMenu(player, target).openMenu() }

        // Punishment History
        buttons[21] = SimpleActionButton(
            XMaterial.REDSTONE.parseMaterial()!!, mutableListOf(
                " ",
                Chat.format("&7Click this button to view"),
                Chat.format("&7the punishment history of this user!"),
                " "
            ), "&6Punishment History", 0
        ).setBody { player, _, _ -> HistoryMenu(target, player).openMenu() }

        // Alternate Accounts
        buttons[23] = SimpleActionButton(
            XMaterial.ANVIL.parseMaterial()!!, mutableListOf(
                " ",
                Chat.format("&7Click this button to view every alternate account of this user!"),
                " "
            ), "&6Alternate Accounts", 0
        ).setBody { player, _, _ ->
            target.getAltAccounts().thenAccept { accounts ->
                AltsMenu(player, target, accounts).updateMenu()
            }
        }

        // Rank Grants
        buttons[25] = SimpleActionButton(
            XMaterial.WHITE_WOOL.parseMaterial()!!, mutableListOf(
                " ",
                Chat.format("&7Click this button to view the rank grants of this user!"),
                " "
            ), "&6Rank Grants", 13
        ).setBody { player, _, _ ->
            GrantsMenu(
                player,
                target,
                GrantsCommand.getViewableGrants(player, RankGrantService.getFromCache(target.uuid).toMutableList()),
                GrantFilter.ALL
            ).updateMenu()
        }

        // Notes
        buttons[37] = SimpleActionButton(
            XMaterial.PAPER.parseMaterial()!!, mutableListOf(
                " ",
                Chat.format("&7Click this button to view the notes of this user"),
                " "
            ), "&6Notes", 0
        ).setBody { player, _, _ -> PlayerNotesMenu(player, target).updateMenu() }

        // Friends
        buttons[39] = SimpleActionButton(
            XMaterial.COMPARATOR.parseMaterial()!!, mutableListOf(
                " ",
                Chat.format("&7Click this button to view the friends of this user"),
                " "
            ), "&6Friends", 0
        ).setBody { player, _, _ -> FriendsListMenu(player, target, FriendFilter.ALL).updateMenu() }

        // Tag Grants
        buttons[41] = SimpleActionButton(
            XMaterial.NAME_TAG.parseMaterial()!!, mutableListOf(
                " ",
                Chat.format("&7Click this button to view the tag grants of this user"),
                " "
            ), "&6Tag Grants", 0
        ).setBody { player, _, _ -> TagGrantsMenu(player, target).updateMenu() }

        // JSON Dump
        buttons[43] = SimpleActionButton(
            XMaterial.CAULDRON.parseMaterial()!!, mutableListOf(
                " ",
                Chat.format("&7Click this button to view the raw JSON dump of this user's profile"),
                " "
            ), "&6JSON Dump", 0
        ).setBody { player, _, _ ->
            val gson = Serializers.GSON.toJson(target)
            player.closeInventory()
            player.sendMessage(gson)
        }

        return buttons
    }

    override fun getTitle(player: Player): String =
        Chat.format("&7Viewing: &r${AlchemistAPI.getRankDisplay(target.uuid)}")

}