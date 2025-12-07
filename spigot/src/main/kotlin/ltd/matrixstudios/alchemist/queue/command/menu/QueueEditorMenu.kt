package ltd.matrixstudios.alchemist.queue.command.menu

/**
 * Class created on 7/12/2023
 *
 * @author 98ping
 * @project Alchemist
 * @website https://solo.to/redis
 */

import ltd.matrixstudios.alchemist.models.queue.QueueModel
import ltd.matrixstudios.alchemist.models.queue.QueueStatus
import ltd.matrixstudios.alchemist.queue.packet.QueueUpdatePacket
import ltd.matrixstudios.alchemist.redis.AsynchronousRedisSender
import ltd.matrixstudios.alchemist.service.queue.QueueService
import ltd.matrixstudios.alchemist.service.server.UniqueServerService
import ltd.matrixstudios.alchemist.util.Chat
import ltd.matrixstudios.alchemist.util.InputPrompt
import ltd.matrixstudios.alchemist.util.TimeUtil
import ltd.matrixstudios.alchemist.util.menu.Button
import ltd.matrixstudios.alchemist.util.menu.buttons.SimpleActionButton
import ltd.matrixstudios.alchemist.util.menu.pagination.PaginatedMenu
import org.bukkit.Material
import org.bukkit.entity.Player
import java.util.*

class QueueEditorMenu(var player: Player) : PaginatedMenu(36, player) {

    override fun getPagesButtons(player: Player): MutableMap<Int, Button> {
        val buttons = hashMapOf<Int, Button>()

        var index = 0
        // Sort by id just to keep things predictable
        val queues = QueueService.cache.values.sortedBy { it.id }

        for (queue in queues) {
            buttons[index++] = createQueueDebugButton(queue)
        }

        return buttons
    }

    private fun createQueueDebugButton(queue: QueueModel): Button {
        val uniqueServer = UniqueServerService.byId(queue.uniqueServerId)

        val statusColor = when (queue.status) {
            QueueStatus.OPEN -> "&a"
            QueueStatus.PAUSED -> "&e"
            QueueStatus.CLOSED -> "&c"
        }

        val playersInQueue = queue.playersInQueue.size

        val serverDisplay = uniqueServer?.displayName ?: "&cUNKNOWN"
        val serverBungee = uniqueServer?.bungeeName ?: "&cUNKNOWN"

        val lore = mutableListOf<String>()
        lore.add("")
        lore.add(Chat.format("&7ID: &f${queue.id}"))
        lore.add(Chat.format("&7Display: &f${queue.displayName}"))
        lore.add(Chat.format("&7Status: $statusColor${queue.status}"))
        lore.add(Chat.format("&7Players in queue: &f$playersInQueue"))
        lore.add("")
        lore.add(Chat.format("&7UniqueServer ID: &f${queue.uniqueServerId}"))
        lore.add(Chat.format("&7Test Display: &f$serverDisplay"))
        lore.add(Chat.format("&7Test Bungee: &f$serverBungee"))

        if (queue.lastPull > 0L) {
            val since = System.currentTimeMillis() - queue.lastPull
            lore.add(Chat.format("&7Last Pull: &f${TimeUtil.formatDuration(since)} &7ago"))
        } else {
            lore.add(Chat.format("&7Last Pull: &fNever"))
        }

        // Show first few queued player UUIDs (shortened) for extra debugging
        if (playersInQueue > 0) {
            val sample = queue.playersInQueue
                .take(3)
                .joinToString("&7, ") { "&f" + it.id.toString().substring(0, 8) }

            lore.add("")
            lore.add(Chat.format("&7Sample players: $sample"))
        }

        lore.add("")
        lore.add(Chat.format("&eClick to debug this queue"))

        return SimpleActionButton(
            Material.PAPER,
            lore,
            Chat.format("&b${queue.displayName}"),
            0
        ).setBody { player, _, _ ->
            // Simple debug output when clicking the button
            player.sendMessage(Chat.format("&7&m--------------------------"))
            player.sendMessage(Chat.format("&e[Queue Debug] &f${queue.id}"))
            player.sendMessage(Chat.format("&7Status: $statusColor${queue.status}"))
            player.sendMessage(Chat.format("&7Players in queue: &f$playersInQueue"))
            player.sendMessage(Chat.format("&7UniqueServer ID: &f${queue.uniqueServerId}"))
            player.sendMessage(Chat.format("&7Dest Display: &f$serverDisplay"))
            player.sendMessage(Chat.format("&7Dest Bungee: &f$serverBungee"))
            if (queue.lastPull > 0L) {
                val since = System.currentTimeMillis() - queue.lastPull
                player.sendMessage(Chat.format("&7Last Pull: &f${TimeUtil.formatDuration(since)} &7ago"))
            } else {
                player.sendMessage(Chat.format("&7Last Pull: &fNever"))
            }
            player.sendMessage(Chat.format("&7&m--------------------------"))
        }
    }

    override fun getButtonPositions(): List<Int> {
        return listOf(
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34
        )
    }

    override fun getHeaderItems(player: Player): MutableMap<Int, Button> {
        return mutableMapOf(
            1 to Button.placeholder(),
            2 to Button.placeholder(),
            5 to Button.placeholder(),
            3 to Button.placeholder(),
            4 to SimpleActionButton(
                Material.BOOKSHELF, mutableListOf(
                    " ",
                    Chat.format("&7Click here to create a new"),
                    Chat.format("&7queue and add it to the local"),
                    Chat.format("&7cache"),
                    " "
                ), "&eCreate New Queue", 0
            ).setBody { player, _, _ ->
                InputPrompt()
                    .withText(Chat.format("&eType in the name of the queue you want to create"))
                    .acceptInput { string ->
                        val id = string.lowercase(Locale.getDefault())
                        QueueService.byId(id).thenAccept {
                            if (it != null) {
                                player.sendMessage(Chat.format("&cThis queue already exists!"))
                                return@thenAccept
                            }

                            val queue = QueueModel(
                                id,
                                string,
                                1,
                                QueueStatus.CLOSED,
                                1000,
                                id,
                                -1L,
                                "DIAMOND"
                            )
                            QueueService.saveQueue(queue)
                            AsynchronousRedisSender.send(QueueUpdatePacket())
                            player.sendMessage(Chat.format("&aYou have created a new queue with the name &f$string"))
                        }
                    }.start(player)
            },
            6 to Button.placeholder(),
            7 to Button.placeholder(),
            17 to Button.placeholder(),
            18 to Button.placeholder(),
            26 to Button.placeholder(),
            35 to Button.placeholder(),
            36 to Button.placeholder(),
            37 to Button.placeholder(),
            38 to Button.placeholder(),
            39 to Button.placeholder(),
            40 to Button.placeholder(),
            41 to Button.placeholder(),
            42 to Button.placeholder(),
            43 to Button.placeholder(),
            44 to Button.placeholder(),
            9 to Button.placeholder(),
            27 to Button.placeholder(),
        )
    }

    override fun getButtonsPerPage(): Int {
        return 21
    }

    override fun getTitle(player: Player): String {
        return Chat.format("&7[Editor] &eQueues")
    }
}
