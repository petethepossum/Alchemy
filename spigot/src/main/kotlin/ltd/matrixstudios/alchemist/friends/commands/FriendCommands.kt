package ltd.matrixstudios.alchemist.friends.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.*
import ltd.matrixstudios.alchemist.api.AlchemistAPI
import ltd.matrixstudios.alchemist.friends.filter.FriendFilter
import ltd.matrixstudios.alchemist.friends.menus.FriendsListMenu
import ltd.matrixstudios.alchemist.friends.menus.FriendsMenu
import ltd.matrixstudios.alchemist.models.profile.GameProfile
import ltd.matrixstudios.alchemist.packets.NetworkMessagePacket
import ltd.matrixstudios.alchemist.profiles.getProfile
import ltd.matrixstudios.alchemist.redis.AsynchronousRedisSender
import ltd.matrixstudios.alchemist.service.profiles.ProfileGameService
import ltd.matrixstudios.alchemist.util.Chat
import org.bukkit.Bukkit
import org.bukkit.entity.Player

class FriendCommands : BaseCommand()
{

    @CommandAlias("friend|friends")
    @Default
    fun friend(player: Player)
    {
        val profile = player.getProfile()

        if (profile == null)
        {
            player.sendMessage(Chat.format("&cYour profile does not exist!"))
            return
        }

        FriendsMenu(player, profile).openMenu()
    }

    @Subcommand("add")
    @CommandCompletion("@gameprofile")
    fun add(player: Player, @Name("target") gameProfile: GameProfile)
    {
        val playerProfile = AlchemistAPI.quickFindProfile(player.uniqueId).join() ?: return

        if (gameProfile.friends.contains(player.uniqueId))
        {
            player.sendMessage(Chat.format("&cThis player is already friends with you"))
            return
        }

        if (playerProfile.friendInvites.contains(gameProfile.uuid))
        {
            player.sendMessage(Chat.format("&cAlready sent an invite to this player"))
            return
        }

        if (gameProfile.friendInvites.contains(player.uniqueId)) {
            player.sendMessage(Chat.format("&cThis player has already sent you a friend request! Use /friend accept to accept it."))
            return
        }

        if (gameProfile.uuid == player.uniqueId)
        {
            player.sendMessage(Chat.format("&cCannot friend yourself!"))
            return
        }

        gameProfile.friendInvites.add(player.uniqueId)
        player.sendMessage(Chat.format("&e&l[Friends] &aYou have sent a friend request to &f" + gameProfile.username))

        AsynchronousRedisSender.send(
            NetworkMessagePacket(
                gameProfile.uuid,
                Chat.format("&e&l[Friends] &aYou have received a friend request from &f" + playerProfile.username)
            )
        )
        AsynchronousRedisSender.send(
            NetworkMessagePacket(
                gameProfile.uuid,
                Chat.format("&e&l[Friends] &aType &f/friend accept &ato accept the request")
            )
        )

        ProfileGameService.save(gameProfile)
    }

    @Subcommand("list")
    fun list(player: Player)
    {
        val gameProfile = AlchemistAPI.quickFindProfile(player.uniqueId).get() ?: run {
            player.sendMessage(Chat.format("&cYour profile does not exist!"))
            return
        }

        FriendsListMenu(player, gameProfile, FriendFilter.ALL).updateMenu()
    }

    @Subcommand("accept")
    @CommandCompletion("@gameprofile")
    fun accept(player: Player, @Name("target") gameProfile: GameProfile)
    {
        val it = ProfileGameService.byId(player.uniqueId)
        if (it == null) {
            player.sendMessage(Chat.format("&cYour profile does not exist!"))
            return
        }

        if (!it.friendInvites.contains(gameProfile.uuid))
        {
            player.sendMessage(Chat.format("&cThis player has never tried friending you!"))
            return
        }

        it.friendInvites.remove(gameProfile.uuid)
        it.friends.add(gameProfile.uuid)

        gameProfile.friends.add(it.uuid)

        ProfileGameService.save(it)
        ProfileGameService.save(gameProfile)

        player.sendMessage(Chat.format("&e&l[Friends] &aYou have accepted ${gameProfile.username}'s &fFriend Request"))

        AsynchronousRedisSender.send(
            NetworkMessagePacket(
                gameProfile.uuid,
                Chat.format("&e&l[Friends] &f" + player.name + " &ahas accepted your friend request!")
            )
        )
        AsynchronousRedisSender.send(
            NetworkMessagePacket(
                gameProfile.uuid,
                Chat.format("&e&l[Friends] &aYou can now see them in your friends list!")
            )
        )
    }

    @Subcommand("remove")
    @CommandCompletion("@gameprofile")
    fun remove(player: Player, @Name("target") gameProfile: GameProfile)
    {
        val playerProfile = ProfileGameService.byId(player.uniqueId)
        if (playerProfile == null)
        {
            player.sendMessage(Chat.format("&cYour profile does not exist!"))
            return
        }

        if (!playerProfile.friends.contains(gameProfile.uuid))
        {
            player.sendMessage(Chat.format("&cThat player is not your friend!"))
            return
        }

        playerProfile.friends.remove(gameProfile.uuid)
        gameProfile.friends.remove(player.uniqueId)

        ProfileGameService.save(playerProfile)
        ProfileGameService.save(gameProfile)

        player.sendMessage(Chat.format("&e&l[Friends] &aYou have removed ${gameProfile.username} from your friends list."))

        // Notify the other player if online
        val onlinePlayer = Bukkit.getPlayer(gameProfile.uuid)
        onlinePlayer?.sendMessage(Chat.format("&e&l[Friends] &f${player.name} &ahas removed you from their friends list."))
    }

}
