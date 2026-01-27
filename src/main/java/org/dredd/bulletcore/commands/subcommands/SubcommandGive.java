package org.dredd.bulletcore.commands.subcommands;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.dredd.bulletcore.custom_item_manager.registries.CustomItemsRegistry;
import org.dredd.bulletcore.models.CustomBase;
import org.dredd.bulletcore.utils.ServerUtils;
import org.jetbrains.annotations.NotNull;

import static org.dredd.bulletcore.config.messages.component.ComponentMessage.ITEM_GIVEN_SUCCESS;
import static org.dredd.bulletcore.config.messages.component.ComponentMessage.ITEM_NOT_FOUND;
import static org.dredd.bulletcore.config.messages.component.ComponentMessage.PLAYER_NOT_FOUND;
import static org.dredd.bulletcore.utils.ServerUtils.EMPTY_LIST;

/**
 * Gives custom items to a player.
 *
 * @author dredd
 * @since 1.0.0
 */
public enum SubcommandGive implements Subcommand {

    INSTANCE;

    @Override
    public @NotNull String getName() {
        return "give";
    }

    @Override
    public @NotNull String getUsageArgs() {
        return "<player> <item>";
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
        final String playerName = args[1];
        final Player player = Bukkit.getPlayerExact(playerName);
        if (player == null) {
            PLAYER_NOT_FOUND.sendMessage(sender, Map.of("player", playerName));
            return;
        }

        final String itemName = args[2];
        final CustomBase item = CustomItemsRegistry.ALL.getItemOrNull(itemName);
        if (item == null) {
            ITEM_NOT_FOUND.sendMessage(sender, Map.of("item", itemName));
            return;
        }

        final var inv = player.getInventory();
        final var leftover = inv.addItem(item.createItemStack());
        leftover.forEach((i, stack) -> player.getWorld().dropItem(player.getLocation(), stack));
        ITEM_GIVEN_SUCCESS.sendMessage(sender, Map.of("item", itemName, "player", player.getName()));
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        final String playerName = args[1];
        if (args.length == 2)
            return StringUtil.copyPartialMatches(playerName, ServerUtils.getOnlinePlayerNames(), new ArrayList<>());

        if (Bukkit.getPlayerExact(playerName) == null) return EMPTY_LIST;

        if (args.length == 3)
            return StringUtil.copyPartialMatches(args[2], CustomItemsRegistry.ALL.getAllNames(), new ArrayList<>());

        return EMPTY_LIST;
    }
}