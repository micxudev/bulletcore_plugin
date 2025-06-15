package org.dredd.bulletCore.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.util.StringUtil;
import org.dredd.bulletCore.BulletCore;
import org.dredd.bulletCore.commands.subcommands.Subcommand;
import org.dredd.bulletCore.commands.subcommands.SubcommandGive;
import org.dredd.bulletCore.commands.subcommands.SubcommandReload;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Handles the execution and tab completion of the {@value #MAIN_COMMAND_NAME } command.
 * <p>
 * This class serves as the central routing point for all the subcommands,
 * delegating logic to specific handler classes.
 *
 * <p>Implements {@link TabExecutor} to support both command execution and tab suggestions.
 *
 * @author dredd
 * @since 1.0.0
 */
public final class CommandHandler implements TabExecutor {

    /**
     * Reference to the plugin's main instance.
     */
    private static final BulletCore plugin = BulletCore.getInstance();

    /**
     * The name of the main command for this plugin.
     */
    public static final String MAIN_COMMAND_NAME = "bulletcore";

    /**
     * A map of registered subcommands by name.
     */
    private final Map<String, Subcommand> subCommands = new LinkedHashMap<>();

    /**
     * Constructs a new {@code CommandHandler} instance.
     * <p>Registers all subcommands for the main command.
     */
    public CommandHandler() {
        registerSubcommand(new SubcommandGive());
        registerSubcommand(new SubcommandReload());
    }

    /**
     * Registers a subcommand with the main command.
     *
     * @param subcommand the subcommand to register
     */
    private void registerSubcommand(Subcommand subcommand) {
        subCommands.put(subcommand.getName().toLowerCase(Locale.ROOT), subcommand);
    }

    /**
     * Handles the execution of the {@value #MAIN_COMMAND_NAME } subcommand.
     *
     * @param sender  the source of the command
     * @param command the command that was executed
     * @param label   the alias used to execute the command
     * @param args    the command arguments
     * @return {@code true} if the command was handled, {@code false} otherwise
     */
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            sender.sendMessage(plugin.getPluginMeta().getDisplayName() +
                ". Usage: " + "/" + MAIN_COMMAND_NAME + " <subcommand>"
            );
            return true;
        }

        Subcommand sub = subCommands.get(args[0].toLowerCase(Locale.ROOT));
        if (sub == null) {
            sender.sendMessage("Unknown subcommand: " + args[0]);
            return true;
        }

        boolean checkPermission = sub.getPermission() != null && !sub.getPermission().isBlank();
        if (checkPermission && !sender.hasPermission(sub.getPermission())) {
            sender.sendMessage("You do not have permission to use this subcommand.");
            return true;
        }

        if (args.length - 1 < sub.getMinArgs()) {
            sender.sendMessage("Usage: " + "/" + MAIN_COMMAND_NAME + " " + sub.getName() + " " + sub.getUsageArgs());
            return true;
        }

        sub.execute(sender, args);
        return true;
    }

    /**
     * Provides dynamic tab completion for {@value #MAIN_COMMAND_NAME } subcommands and arguments.
     *
     * @param sender  the source of the command
     * @param command the command being tab-completed
     * @param label   the alias used
     * @param args    the arguments currently typed
     * @return a list of possible completions
     */
    @Override
    public @NotNull List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                               @NotNull String label, @NotNull String[] args) {
        if (args.length == 1)
            return StringUtil.copyPartialMatches(args[0], getAllowedSubcommands(sender), new ArrayList<>());

        Subcommand sub = subCommands.get(args[0].toLowerCase(Locale.ROOT));
        return (sub != null && (sub.getPermission() == null || sender.hasPermission(sub.getPermission())))
            ? sub.tabComplete(sender, args)
            : Collections.emptyList();
    }

    private List<String> getAllowedSubcommands(@NotNull CommandSender sender) {
        return subCommands.entrySet().stream()
            .filter(
                entry -> entry.getValue().getPermission() == null || sender.hasPermission(entry.getValue().getPermission())
            )
            .map(Map.Entry::getKey)
            .toList();
    }
}