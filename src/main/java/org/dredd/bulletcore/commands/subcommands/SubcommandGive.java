package org.dredd.bulletcore.commands.subcommands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.dredd.bulletcore.custom_item_manager.registries.CustomItemsRegistry;
import org.dredd.bulletcore.models.CustomBase;
import org.dredd.bulletcore.utils.ServerUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.dredd.bulletcore.config.messages.ComponentMessage.*;
import static org.dredd.bulletcore.config.messages.MessageManager.of;

/**
 * Implements the {@code /bulletcore give} subcommand.
 *
 * @author dredd
 * @since 1.0.0
 */
public final class SubcommandGive implements Subcommand {
    @Override
    public @NotNull String getName() {
        return "give";
    }

    @Override
    public @NotNull String getUsageArgs() {
        return "<item> <player>";
    }

    @Override
    public int getMinArgs() {
        return 2;
    }

    @Override
    public @NotNull String getPermission() {
        return "bulletcore.command.give";
    }

    @Override
    public void execute(@NotNull CommandSender sender, @NotNull String[] args) {
        String itemName = args[1];
        CustomBase item = CustomItemsRegistry.all.getItemOrNull(itemName);
        if (item == null) {
            sender.sendMessage(of(sender, INVALID_ITEM, Map.of("item", itemName)));
            return;
        }

        Player player = Bukkit.getPlayer(args[2]);
        if (player == null) {
            sender.sendMessage(of(sender, PLAYER_NOT_FOUND, Map.of("player", args[2])));
            return;
        }

        player.getInventory().addItem(item.createItemStack());
        sender.sendMessage(of(sender, ITEM_GIVEN, Map.of("item", itemName, "player", player.getName())));
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length == 2)
            return StringUtil.copyPartialMatches(args[1], CustomItemsRegistry.all.getAllNames(), new ArrayList<>());

        if (args.length == 3)
            return StringUtil.copyPartialMatches(args[2], ServerUtils.getOnlinePlayerNames(), new ArrayList<>());

        return Collections.emptyList();
    }
}