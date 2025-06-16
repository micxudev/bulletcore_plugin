package org.dredd.bulletcore.commands.subcommands;

import org.bukkit.command.CommandSender;
import org.dredd.bulletcore.BulletCore;
import org.dredd.bulletcore.config.ConfigManager;
import org.dredd.bulletcore.config.YMLLModelLoader;
import org.dredd.bulletcore.config.messages.MessageManager;
import org.dredd.bulletcore.custom_item_manager.registries.CustomItemsRegistry;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.dredd.bulletcore.config.messages.ComponentMessage.*;
import static org.dredd.bulletcore.config.messages.MessageManager.of;

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
        long startTime = System.currentTimeMillis();
        MessageManager.reload(BulletCore.getInstance());
        ConfigManager.reload(BulletCore.getInstance());
        CustomItemsRegistry.clearAll();
        YMLLModelLoader.loadAllItems();
        long endTime = System.currentTimeMillis();
        sender.sendMessage(of(sender, CONFIG_RELOADED, Map.of("time", String.valueOf(endTime - startTime))));
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        return Collections.emptyList();
    }
}