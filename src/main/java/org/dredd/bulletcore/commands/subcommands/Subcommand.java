package org.dredd.bulletcore.commands.subcommands;

import org.bukkit.command.CommandSender;
import org.dredd.bulletcore.commands.CommandHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Represents a subcommand of the main command defined by {@link CommandHandler}.
 *
 * <p>This interface is used by {@link CommandHandler} to register and delegate
 * subcommand execution and tab completion in a modular and consistent way.
 *
 * @author dredd
 * @since 1.0.0
 */
public interface Subcommand {

    /**
     * The name used to invoke this subcommand (e.g., {@code give}).
     */
    @NotNull String getName();

    /**
     * The usage string used to display the proper usage of arguments for this subcommand.
     */
    @NotNull String getUsageArgs();

    /**
     * The minimum number of arguments required to execute this subcommand.
     * <p> Subcommand name (args[0]) is <b>not</b> included in the count.
     * <p> If fewer arguments are provided, the command will fail and send the proper usage message.
     *
     * @return the minimum number of arguments required to execute this subcommand
     */
    int getMinArgs();

    /**
     * The required permission to execute this subcommand.
     * <p>Return null or empty if no permission is required.
     */
    @Nullable String getPermission();

    /**
     * Executes the subcommand only if the sender has permission.
     *
     * @param sender the command sender (player or console)
     * @param args   the full argument list, including the subcommand as args[0]
     */
    void execute(@NotNull CommandSender sender, @NotNull String[] args);

    /**
     * Provides tab completions for this subcommand.
     *
     * @param sender the command sender (player or console)
     * @param args   the full argument list
     * @return a list of possible completions
     */
    @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String[] args);
}