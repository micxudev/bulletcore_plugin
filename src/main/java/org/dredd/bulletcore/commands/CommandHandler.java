package org.dredd.bulletcore.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.util.StringUtil;
import org.dredd.bulletcore.commands.subcommands.Subcommand;
import org.dredd.bulletcore.commands.subcommands.SubcommandGive;
import org.dredd.bulletcore.commands.subcommands.SubcommandReload;
import org.dredd.bulletcore.commands.subcommands.SubcommandSkin;
import org.dredd.bulletcore.commands.subcommands.SubcommandSkinManage;
import org.dredd.bulletcore.commands.subcommands.SubcommandSprayInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.dredd.bulletcore.config.messages.ComponentMessage.*;
import static org.dredd.bulletcore.config.messages.MessageManager.of;
import static org.dredd.bulletcore.utils.ServerUtils.EMPTY_LIST;

/**
 * Handles execution and tab completion for the {@value #MAIN_COMMAND_NAME} command.<br>
 * Delegates logic to registered subcommands.
 * <p>
 * Implements {@link TabExecutor} to support both execution and tab suggestions.
 *
 * @author dredd
 * @since 1.0.0
 */
public final class CommandHandler implements TabExecutor {

    /**
     * Main plugin command name.
     */
    public static final String MAIN_COMMAND_NAME = "bulletcore";

    /**
     * Registered subcommands by name.
     */
    private final Map<String, Subcommand> subCommands = new LinkedHashMap<>();

    /**
     * Creates a new handler and registers all subcommands.
     */
    public CommandHandler() {
        registerSubcommand(new SubcommandGive());
        registerSubcommand(new SubcommandReload());
        registerSubcommand(new SubcommandSkin());
        registerSubcommand(new SubcommandSkinManage());
        registerSubcommand(new SubcommandSprayInfo());
    }

    /**
     * Registers a subcommand.
     *
     * @param subcommand subcommand to register
     */
    private void registerSubcommand(@NotNull Subcommand subcommand) {
        subCommands.put(subcommand.getName().toLowerCase(Locale.ROOT), subcommand);
    }

    /**
     * Executes the {@value #MAIN_COMMAND_NAME} subcommand.
     */
    @Override
    public boolean onCommand(@NotNull CommandSender sender,
                             @NotNull Command command,
                             @NotNull String label,
                             @NotNull String[] args) {
        if (args.length == 0) {
            sender.sendMessage(of(sender, NO_SUBCOMMAND_PROVIDED, Map.of("command", MAIN_COMMAND_NAME)));
            return true;
        }

        Subcommand sub = subCommands.get(args[0].toLowerCase(Locale.ROOT));
        if (sub == null) {
            sender.sendMessage(of(sender, UNKNOWN_SUBCOMMAND, Map.of("subcommand", args[0])));
            return true;
        }

        if (!sub.getPermission().isBlank() && !sender.hasPermission(sub.getPermission())) {
            sender.sendMessage(of(sender, NO_SUBCOMMAND_PERMISSION, null));
            return true;
        }

        if (args.length - 1 < sub.getMinArgs()) {
            sender.sendMessage(of(sender, NOT_ENOUGH_ARGS, Map.of(
                "command", MAIN_COMMAND_NAME, "subcommand", args[0], "args", sub.getUsageArgs())
            ));
            return true;
        }

        sub.execute(sender, args);
        return true;
    }

    /**
     * Provides tab completion for subcommands and arguments.
     *
     * @return list of completions
     */
    @Override
    public @NotNull List<String> onTabComplete(@NotNull CommandSender sender,
                                               @NotNull Command command,
                                               @NotNull String label,
                                               @NotNull String[] args) {
        if (args.length == 1)
            return StringUtil.copyPartialMatches(args[0], getAllowedSubcommands(sender), new ArrayList<>());

        Subcommand sub = subCommands.get(args[0].toLowerCase(Locale.ROOT));
        return (sub != null && (sub.getPermission().isBlank() || sender.hasPermission(sub.getPermission())))
            ? sub.tabComplete(sender, args)
            : EMPTY_LIST;
    }

    /**
     * Returns subcommands the sender can use.
     */
    private @NotNull @Unmodifiable List<String> getAllowedSubcommands(@NotNull CommandSender sender) {
        return subCommands.entrySet().stream()
            .filter(entry -> {
                String perm = entry.getValue().getPermission();
                return perm.isBlank() || sender.hasPermission(perm);
            })
            .map(Map.Entry::getKey)
            .toList();
    }
}