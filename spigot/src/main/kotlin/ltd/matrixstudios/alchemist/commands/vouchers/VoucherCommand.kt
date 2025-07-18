package ltd.matrixstudios.alchemist.commands.vouchers

import co.aikar.commands.BaseCommand
import co.aikar.commands.CommandHelp
import co.aikar.commands.annotation.*
import ltd.matrixstudios.alchemist.api.AlchemistAPI
import ltd.matrixstudios.alchemist.models.profile.GameProfile
import ltd.matrixstudios.alchemist.models.vouchers.VoucherGrant
import ltd.matrixstudios.alchemist.models.vouchers.VoucherTemplate
import ltd.matrixstudios.alchemist.punishment.BukkitPunishmentFunctions
import ltd.matrixstudios.alchemist.service.vouchers.VoucherService
import ltd.matrixstudios.alchemist.util.Chat
import ltd.matrixstudios.alchemist.util.TimeUtil
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.*

@CommandAlias("voucher|vouchers")
class VoucherCommand : BaseCommand()
{

    @Default
    fun openMenu(player: Player)
    {
        val grants = VoucherService.allGrantsFromPlayer(player.uniqueId)
        if (grants.isEmpty()) {
            player.sendMessage(Chat.format("&cYou do not have any vouchers. Use &f/voucher info &c to see how to get some!"))
            return
        }
        VoucherGrantsMenu(player, grants).updateMenu()
    }

    @Subcommand("help")
    @CommandPermission("alchemist.vouchers.admin")
    @HelpCommand
    fun help(help: CommandHelp)
    {
        help.showHelp()
    }

    @Subcommand("info")
    fun info(player: Player)
    {
        player.sendMessage(Chat.format("&eVouchers are a way to give players items, commands, or other rewards."))
        player.sendMessage(Chat.format("&eYou can get vouchers by completing tasks, winning events, or purchasing them."))
        player.sendMessage(Chat.format("&eUse &f/voucher &eto view your vouchers and redeem them."))
        player.sendMessage(Chat.format("&eFor more information, contact a staff member."))
    }

    @Subcommand("template setprize")
    @CommandPermission("alchemist.vouchers.admin")
    fun create(sender: CommandSender, @Name("id") id: String, @Name("prize") prize: String)
    {
        val template = VoucherService.findVoucherTemplate(id.lowercase())

        if (template == null)
        {
            sender.sendMessage(Chat.format("&cA voucher with this id does not exist"))
            return
        }

        template.whatFor = Chat.format(prize)
        VoucherService.insertTemplate(template)
        sender.sendMessage(Chat.format("&aSet the prize of &f$id &ato &f$prize"))
    }

    @Subcommand("template setcommand")
    @CommandPermission("alchemist.vouchers.admin")
    fun setcommand(sender: CommandSender, @Name("id") id: String, @Name("command") command: String)
    {
        val template = VoucherService.findVoucherTemplate(id.lowercase())

        if (template == null)
        {
            sender.sendMessage(Chat.format("&cA voucher with this id does not exist"))
            return
        }

        template.commandToExecute = command
        VoucherService.insertTemplate(template)
        sender.sendMessage(Chat.format("&aSet the command of &f$id &ato &f$command"))
    }

    @Subcommand("issue")
    @CommandPermission("alchemist.vouchers.admin")
    fun issue(
        sender: CommandSender,
        @Name("id") id: String,
        @Name("target") target: GameProfile,
        @Name("duration") duration: String
    )
    {
        val template = VoucherService.findVoucherTemplate(id.lowercase())
        if (template == null)
        {
            sender.sendMessage(Chat.format("&cA voucher with this id does not exist"))
            return
        }
        val grant = VoucherGrant(
            UUID.randomUUID(),
            template,
            !duration.equals("perm", ignoreCase = true),
            if (!duration.equals("perm", ignoreCase = true)) System.currentTimeMillis().plus(TimeUtil.parseTime(duration) * 1000L) else Long.MAX_VALUE,
            false,
            BukkitPunishmentFunctions.getSenderUUID(sender),
            target.uuid
        )
        VoucherService.insertGrant(target.uuid, grant)
        sender.sendMessage(Chat.format("&aIssued a new voucher grant to " + AlchemistAPI.getRankDisplay(target.uuid)))
    }

    @Subcommand("template create")
    @CommandPermission("alchemist.vouchers.admin")
    fun create(sender: CommandSender, @Name("id") id: String)
    {
        val template = VoucherService.findVoucherTemplate(id.lowercase())

        if (template != null)
        {
            sender.sendMessage(Chat.format("&cA voucher with this id already exists"))
            return
        }

        val toCreate = VoucherTemplate(id.lowercase(), id, "", mutableListOf())
        VoucherService.insertTemplate(toCreate)
        sender.sendMessage(Chat.format("&aCreated a new voucher template with the name &f$id"))
    }

    @Subcommand("template delete")
    @CommandPermission("alchemist.vouchers.admin")
    fun delete(sender: CommandSender, @Name("id") id: String)
    {
        val template = VoucherService.findVoucherTemplate(id.lowercase())

        if (template == null)
        {
            sender.sendMessage(Chat.format("&cA voucher with this id does not exist"))
            return
        }

        VoucherService.handlerTemplates.deleteAsync(template.id)
        VoucherService.voucherTemplates.remove(template.id)

        sender.sendMessage(Chat.format("&aDeleted a voucher template with the name &f$id"))
    }
}