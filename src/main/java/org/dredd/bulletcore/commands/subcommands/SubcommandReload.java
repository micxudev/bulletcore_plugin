package org.dredd.bulletcore.commands.subcommands;

import org.bukkit.command.CommandSender;
import org.dredd.bulletcore.BulletCore;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

import static org.dredd.bulletcore.config.messages.ComponentMessage.CONFIG_RELOADED;
import static org.dredd.bulletcore.utils.ServerUtils.EMPTY_LIST;

/**
 * Reloads the plugin config.
 *
 * @author dredd
 * @since 1.0.0
 */
public final class SubcommandReload implements Subcommand {

    public static final SubcommandReload INSTANCE = new SubcommandReload();

    private SubcommandReload() {}

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
        long startTime = System.currentTimeMillis();
        BulletCore.initAll();
        long endTime = System.currentTimeMillis();
        sender.sendMessage(CONFIG_RELOADED.toComponent(sender, Map.of("time", Long.toString(endTime - startTime))));
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        return EMPTY_LIST;
    }
}