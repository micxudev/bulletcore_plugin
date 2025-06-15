package org.dredd.bulletCore.commands.subcommands;

import org.bukkit.command.CommandSender;
import org.dredd.bulletCore.custom_item_manager.registries.CustomItemsRegistry;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public final class SubcommandReload implements Subcommand {
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
        CustomItemsRegistry.clearAll();
        sender.sendMessage("Reloaded successfully!");
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        return Collections.emptyList();
    }
}