package ltd.matrixstudios.alchemist.commands.filter

import co.aikar.commands.BaseCommand
import co.aikar.commands.CommandHelp
import co.aikar.commands.annotation.*
import ltd.matrixstudios.alchemist.commands.filter.menu.FilterEditorMenu
import ltd.matrixstudios.alchemist.models.filter.Filter
import ltd.matrixstudios.alchemist.redis.AsynchronousRedisSender
import ltd.matrixstudios.alchemist.redis.cache.refresh.RefreshFiltersPacket
import ltd.matrixstudios.alchemist.service.filter.FilterService
import ltd.matrixstudios.alchemist.util.Chat
import org.bukkit.entity.Player
import java.util.*

@CommandAlias("filters|filter")
@CommandPermission("alchemist.filters.admin")
class FilterCommands : BaseCommand()
{

    @HelpCommand
    fun help(help: CommandHelp)
    {
        help.showHelp()
    }

    @Subcommand("editor")
    fun create(player: Player)
    {
        FilterEditorMenu(player).updateMenu()
    }

    @Subcommand("delete")
    fun delete(player: Player, @Name("wordOrId") wordOrId: String) {
        val possibleId = wordOrId.toIntOrNull()

        val filter = if (possibleId != null) {
            FilterService.byNumericId(possibleId).also {
                if (it == null) {
                    player.sendMessage(Chat.format("&cFilter with ID #$possibleId does not exist!"))
                }
            }
        } else {
            FilterService.byWord(wordOrId.lowercase()).also {
                if (it == null) {
                    player.sendMessage(Chat.format("&cThis is not a filter: &f$wordOrId"))
                }
            }
        } ?: return


        FilterService.handler.delete(filter.id)
        AsynchronousRedisSender.send(RefreshFiltersPacket())

        if (possibleId != null) {
            player.sendMessage(
                Chat.format("&aDeleted filter &f'${filter.word}' &a(ID &e#${filter.numericId}&a)")
            )
        } else {
            player.sendMessage(
                Chat.format("&aDeleted filter &f'${filter.word}' &a(ID &e#${filter.numericId}&a)")
            )
        }
    }

    @Subcommand("lookup")
    fun lookup(player: Player, @Name("id") id: Int) {
        val filter = FilterService.byNumericId(id)

        if (filter == null) {
            player.sendMessage(Chat.format("&cFilter #$id does not exist."))
            return
        }

        player.sendMessage(Chat.format("&eFilter #${filter.numericId}: &f${filter.word}"))
        player.sendMessage(Chat.format("&7Silent: &f${if (filter.silent) "&aYes" else "&cNo"}"))
        player.sendMessage(Chat.format("&7Punish: &f${if (filter.shouldPunish) "&aYes" else "&cNo"}"))
        player.sendMessage(Chat.format("&7Type: &f${filter.punishmentType}"))
        player.sendMessage(Chat.format("&7Duration: &f${filter.duration}"))
        player.sendMessage(Chat.format("&7Staff Exempt: &f${if (filter.staffExempt) "&aYes" else "&cNo"}"))
        player.sendMessage(Chat.format("&7Permission: &f${filter.exemptPermission}"))
    }
}
