package org.dredd.bulletcore.commands.subcommands;

import org.bukkit.command.CommandSender;
import org.dredd.bulletcore.commands.CommandHandler;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Represents a subcommand of {@link CommandHandler}.<br>
 * Used for modular execution and tab completion.
 *
 * @author dredd
 * @since 1.0.0
 */
public interface Subcommand {

    /**
     * Subcommand name (e.g., {@code give}).
     */
    @NotNull String getName();

    /**
     * Usage string for arguments (e.g., {@code "<player> <item>"}).
     */
    @NotNull String getUsageArgs();

    /**
     * Minimum number of arguments (excluding the subcommand name).
     *
     * @return required argument count
     */
    int getMinArgs();

    /**
     * Required permission to use this subcommand or empty if none.
     */
    @NotNull String getPermission();

    /**
     * Executes the subcommand.
     *
     * @param sender command sender
     * @param args   full argument list (args[0] = subcommand)
     */
    void execute(@NotNull CommandSender sender, @NotNull String[] args);

    /**
     * Provides tab completions.
     *
     * @param sender command sender
     * @param args   full argument list (args[0] = subcommand)
     * @return possible completions
     */
    @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String[] args);
}