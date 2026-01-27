package org.dredd.bulletcore.commands.subcommands;

import java.util.List;
import java.util.Map;

import org.bukkit.command.CommandSender;
import org.dredd.bulletcore.BulletCore;
import org.jetbrains.annotations.NotNull;

import static org.dredd.bulletcore.config.messages.component.ComponentMessage.CONFIG_RELOADED;
import static org.dredd.bulletcore.utils.ServerUtils.EMPTY_LIST;

/**
 * Reloads the plugin config.
 *
 * @author dredd
 * @since 1.0.0
 */
public enum SubcommandReload implements Subcommand {

    INSTANCE;

    @Override
    public @NotNull String getName() {
        return "reload";
    }

    @Override
    public @NotNull String getUsageArgs() {
        return "";
    }

    @Override
    public int getMinArgs() {
        return 0;
    }

    @Override
    public @NotNull String getPermission() {
        return "bulletcore.command.reload";
    }

    @Override
    public void execute(@NotNull CommandSender sender, @NotNull String[] args) {
        final long startTime = System.currentTimeMillis();
        BulletCore.init(BulletCore.instance());
        final long endTime = System.currentTimeMillis();
        CONFIG_RELOADED.sendMessage(sender, Map.of("time", Long.toString(endTime - startTime)));
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        return EMPTY_LIST;
    }
}