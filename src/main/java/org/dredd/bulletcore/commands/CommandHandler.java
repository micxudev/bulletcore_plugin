package org.dredd.bulletcore.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.util.StringUtil;
import org.dredd.bulletcore.BulletCore;
import org.dredd.bulletcore.commands.subcommands.Subcommand;
import org.dredd.bulletcore.commands.subcommands.SubcommandGive;
import org.dredd.bulletcore.commands.subcommands.SubcommandReload;
import org.dredd.bulletcore.commands.subcommands.SubcommandSkin;
import org.dredd.bulletcore.commands.subcommands.SubcommandSkinManage;
import org.dredd.bulletcore.commands.subcommands.SubcommandSprayInfo;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.dredd.bulletcore.config.messages.ComponentMessage.*;
import static org.dredd.bulletcore.utils.ServerUtils.EMPTY_LIST;

/**
 * Handles execution and tab completion for the main plugin command.
 *
 * @author dredd
 * @since 1.0.0
 */
public final class CommandHandler extends Command {

    // ----------< Static >----------
    private static final String MAIN_COMMAND_NAME = "bulletcore";
    private static final String MAIN_COMMAND_PERMISSION = "bulletcore.command";
    private static final String ALL_SUBCOMMANDS_PERMISSION = "bulletcore.command.*";

    private static boolean canUseAllSubcommands(@NotNull CommandSender sender) {
        return sender.hasPermission(ALL_SUBCOMMANDS_PERMISSION);
    }

    private static boolean canUseSubcommand(@NotNull CommandSender sender, @NotNull Subcommand sub) {
        return sender.hasPermission(sub.getPermission());
    }

    private static boolean canExecuteSubcommand(@NotNull CommandSender sender, @NotNull Subcommand sub) {
        return canUseAllSubcommands(sender) || canUseSubcommand(sender, sub);
    }

    private static CommandHandler INSTANCE;

    private static final List<Subcommand> SUBCOMMANDS = List.of(
        SubcommandGive.INSTANCE,
        SubcommandReload.INSTANCE,
        SubcommandSkin.INSTANCE,
        SubcommandSkinManage.INSTANCE,
        SubcommandSprayInfo.INSTANCE
    );

    public static void init(@NotNull BulletCore plugin) {
        if (INSTANCE == null) {
            INSTANCE = new CommandHandler(plugin, SUBCOMMANDS);
            INSTANCE.register();
        }
    }

    public static void destroy() {
        if (INSTANCE != null) {
            INSTANCE.unregister();
            INSTANCE = null;
        }
    }

    // ----------< Instance >----------

    private final BulletCore plugin;
    private final Map<String, Subcommand> subCommands = new HashMap<>();
    private final List<String> subCommandNames = new ArrayList<>();

    private CommandHandler(@NotNull BulletCore plugin,
                           @NotNull List<Subcommand> subs) {
        super(MAIN_COMMAND_NAME);
        setPermission(MAIN_COMMAND_PERMISSION);
        this.plugin = plugin;
        subs.forEach(this::addSubcommand);
    }

    // -----< Registration >-----

    private void register() {
        plugin.registerPermission(MAIN_COMMAND_PERMISSION);
        plugin.registerPermission(ALL_SUBCOMMANDS_PERMISSION);

        plugin.getServer().getCommandMap()
            .getKnownCommands()
            .put(getName(), this);
    }

    public void unregister() {
        plugin.getServer().getCommandMap()
            .getKnownCommands()
            .remove(getName());
    }

    private void addSubcommand(@NotNull Subcommand sub) {
        String name = sub.getName().toLowerCase(Locale.ROOT);
        subCommands.put(name, sub);
        subCommandNames.add(name);
        plugin.registerPermission(sub.getPermission());
    }

    // -----< Overrides >-----

    @Override
    public boolean execute(@NotNull CommandSender sender,
                           @NotNull String label,
                           @NotNull String[] args) {
        if (args.length == 0) {
            sender.sendMessage(NO_SUBCOMMAND_PROVIDED.toComponent(sender, Map.of("command", MAIN_COMMAND_NAME)));
            return true;
        }

        Subcommand sub = subCommands.get(args[0].toLowerCase(Locale.ROOT));
        if (sub == null) {
            sender.sendMessage(UNKNOWN_SUBCOMMAND.toComponent(sender, Map.of("subcommand", args[0])));
            return true;
        }

        if (!canExecuteSubcommand(sender, sub)) {
            sender.sendMessage(NO_SUBCOMMAND_PERMISSION.toComponent(sender, null));
            return true;
        }

        if (args.length - 1 < sub.getMinArgs()) {
            sender.sendMessage(NOT_ENOUGH_ARGS.toComponent(sender, Map.of(
                "command", MAIN_COMMAND_NAME, "subcommand", args[0], "args", sub.getUsageArgs())
            ));
            return true;
        }

        sub.execute(sender, args);
        return true;
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender,
                                             @NotNull String label,
                                             @NotNull String[] args) {
        if (args.length == 1)
            return StringUtil.copyPartialMatches(args[0], getAllowedSubcommands(sender), new ArrayList<>());

        Subcommand sub = subCommands.get(args[0].toLowerCase(Locale.ROOT));
        return (sub != null && canExecuteSubcommand(sender, sub))
            ? sub.tabComplete(sender, args)
            : EMPTY_LIST;
    }

    private @NotNull List<String> getAllowedSubcommands(@NotNull CommandSender sender) {
        return canUseAllSubcommands(sender)
            ? subCommandNames
            : subCommands.entrySet().stream()
            .filter(entry -> canUseSubcommand(sender, entry.getValue()))
            .map(Map.Entry::getKey)
            .toList();
    }
}