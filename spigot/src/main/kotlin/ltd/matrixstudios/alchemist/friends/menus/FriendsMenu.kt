package ltd.matrixstudios.alchemist.friends.menus

import ltd.matrixstudios.alchemist.friends.filter.FriendFilter
import ltd.matrixstudios.alchemist.models.profile.GameProfile
import ltd.matrixstudios.alchemist.service.profiles.ProfileGameService
import ltd.matrixstudios.alchemist.util.Chat
import ltd.matrixstudios.alchemist.util.InputPrompt
import ltd.matrixstudios.alchemist.util.menu.Button
import ltd.matrixstudios.alchemist.util.menu.Menu
import ltd.matrixstudios.alchemist.util.menu.buttons.PlaceholderButton
import ltd.matrixstudios.alchemist.util.menu.buttons.SimpleActionButton
import ltd.matrixstudios.alchemist.packets.NetworkMessagePacket
import ltd.matrixstudios.alchemist.redis.AsynchronousRedisSender
import org.bukkit.Material
import org.bukkit.entity.Player

class FriendsMenu(val player: Player, val profile: GameProfile) : Menu(player) {

    init {
        staticSize = 27
        placeholder = true
    }

    override fun getButtons(player: Player): MutableMap<Int, Button> {
        val buttons = mutableMapOf<Int, Button>()

        buttons[11] = SimpleActionButton(
            Material.CHEST, mutableListOf(
                Chat.format("&7View every outgoing"),
                Chat.format("&7friend request you have!")
            ), Chat.format("&5Outgoing Friend Requests"), 0
        ).setBody { player, i, clickType ->
            player.closeInventory().also {
                player.sendMessage(Chat.format("&eLoading your current &aoutgoing friend requests!"))
            }

            ProfileGameService.getAllOutgoingFriendRequests(profile).thenApply {
                OutgoingFriendRequestMenu(player, profile, it).updateMenu()
            }
        }

        buttons[12] = SimpleActionButton(
            Material.BOOK, mutableListOf(
                Chat.format("&7Send out a friend request"),
                Chat.format("&7to a player on the network")
            ), Chat.format("&bSend Friend Request"), 0
        ).setBody { player, i, clickType ->

            InputPrompt()
                .withText(Chat.format("&eType another user's name into chat to send them a &afriend request!"))
                .acceptInput { s ->

                    ProfileGameService.byUsernameWithList(s).thenAcceptAsync { profiles ->
                        val targetProfile = profiles.firstOrNull()

                        if (targetProfile == null) {
                            player.sendMessage(Chat.format("&cThis player does not exist!"))
                            return@thenAcceptAsync
                        }

                        if (targetProfile.uuid == player.uniqueId) {
                            player.sendMessage(Chat.format("&cYou cannot friend yourself!"))
                            return@thenAcceptAsync
                        }

                        val playerProfile = ProfileGameService.byId(player.uniqueId) ?: return@thenAcceptAsync

                        // Already friends check
                        if (playerProfile.friends.contains(targetProfile.uuid)) {
                            player.sendMessage(Chat.format("&cYou are already friends with this player!"))
                            return@thenAcceptAsync
                        }

                        // If player already sent an invite to target
                        if (targetProfile.friendInvites.contains(player.uniqueId)) {
                            player.sendMessage(Chat.format("&cYou already have an outgoing friend request to this player!"))
                            return@thenAcceptAsync
                        }

                        // If target has sent an invite to player (pending invite from other side)
                        if (playerProfile.friendInvites.contains(targetProfile.uuid)) {
                            player.sendMessage(Chat.format("&cThis player has already sent you a friend request! Use /friend to accept it in menu."))
                            return@thenAcceptAsync
                        }

                        // Add friend invite to target
                        targetProfile.friendInvites.add(player.uniqueId)
                        ProfileGameService.saveSync(targetProfile)

                        player.sendMessage(Chat.format("&e&l[Friends] &aYou have sent a friend request to " + targetProfile.getCurrentRank().prefix + targetProfile.getRankDisplay()))

                        // Notify target player
                        AsynchronousRedisSender.send(
                            NetworkMessagePacket(
                                targetProfile.uuid,
                                Chat.format("&e&l[Friends] &aYou have received a friend request from &f" + playerProfile.getCurrentRank().prefix + playerProfile.getRankDisplay() + "&a! Type &f/friend &ato accept in menu.")
                            )
                        )
                    }
                }.start(player)
        }

        buttons[13] = PlaceholderButton(
            Material.EMERALD, mutableListOf(
                Chat.format("&7Rank: ${profile.getCurrentRank().color + profile.getCurrentRank().displayName}"),
                Chat.format("&7Total Friends: &f" + profile.friends.size),
                Chat.format("&7Friend Requests: &f" + profile.friendInvites.size),
            ), Chat.format("&aYour Profile"), 0
        )

        buttons[14] = SimpleActionButton(
            Material.BEACON, mutableListOf(
                Chat.format("&7View every friend that you"),
                Chat.format("&7have added!")
            ), Chat.format("&6View Friends"), 0
        ).setBody { player, i, clickType ->
            FriendsListMenu(player, profile, FriendFilter.ALL).updateMenu()
        }

        buttons[15] = SimpleActionButton(
            Material.NETHER_STAR, mutableListOf(
                Chat.format("&7View every pending"),
                Chat.format("&7friend request you have!")
            ), Chat.format("&3Pending Friend Requests"), 0
        ).setBody { player, i, clickType ->
            FriendRequestMenu(player, profile).updateMenu()
        }

        return buttons
    }

    override fun getTitle(player: Player): String {
        return "Configure Friends"
    }
}
