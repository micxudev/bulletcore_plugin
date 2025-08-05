package org.dredd.bulletcore.commands.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.dredd.bulletcore.models.weapons.shooting.spray.SprayHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.dredd.bulletcore.config.messages.ComponentMessage.INVALID_OPERATION;
import static org.dredd.bulletcore.config.messages.ComponentMessage.ONLY_PLAYERS;
import static org.dredd.bulletcore.config.messages.MessageManager.of;
import static org.dredd.bulletcore.utils.ServerUtils.EMPTY_LIST;

/**
 * Implements the {@code /bulletcore spray_info} subcommand.
 *
 * @author dredd
 * @since 1.0.0
 */
public class SubcommandSprayInfo implements Subcommand {

    private static final List<String> OPERATIONS = List.of("on", "off");

    @Override
    public @NotNull String getName() {
        return "spray_info";
    }

    @Override
    public @NotNull String getUsageArgs() {
        return "<on|off>";
    }

    @Override
    public int getMinArgs() {
        return 1;
    }

    @Override
    public @Nullable String getPermission() {
        return "bulletcore.command.spray_info";
    }

    @Override
    public void execute(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(of(sender, ONLY_PLAYERS, null));
            return;
        }

        String operation = args[1];
        if (!OPERATIONS.contains(operation)) {
            sender.sendMessage(of(sender, INVALID_OPERATION, Map.of("operation", operation)));
            return;
        }

        boolean send = switch (operation) {
            case "on" -> true;
            case "off" -> false;
            default -> throw new IllegalStateException("Unexpected value: " + operation);
        };

        SprayHandler.getSprayContext(player).setSendMessage(send);
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof Player)) return EMPTY_LIST;
        if (args.length == 2)
            return StringUtil.copyPartialMatches(args[1], OPERATIONS, new ArrayList<>());
        return EMPTY_LIST;
    }
}