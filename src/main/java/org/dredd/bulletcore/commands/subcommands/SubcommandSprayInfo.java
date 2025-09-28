package org.dredd.bulletcore.commands.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.dredd.bulletcore.models.weapons.shooting.spray.SprayHandler;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.dredd.bulletcore.config.messages.component.ComponentMessage.*;
import static org.dredd.bulletcore.utils.ServerUtils.EMPTY_LIST;

/**
 * Toggles whether to display weapon spray information for the player.
 *
 * @author dredd
 * @since 1.0.0
 */
public class SubcommandSprayInfo implements Subcommand {

    public static final SubcommandSprayInfo INSTANCE = new SubcommandSprayInfo();

    private SubcommandSprayInfo() {}

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
    public @NotNull String getPermission() {
        return "bulletcore.command.spray_info";
    }

    @Override
    public void execute(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            ONLY_PLAYERS.sendMessage(sender, null);
            return;
        }

        String operation = args[1];
        if (!OPERATIONS.contains(operation)) {
            INVALID_OPERATION.sendMessage(sender, Map.of("operation", operation));
            return;
        }

        switch (operation) {
            case "on" -> {
                SprayHandler.getSprayContext(player).setSendMessage(true);
                SPRAY_INFO_ON.sendMessage(sender, null);
            }
            case "off" -> {
                SprayHandler.getSprayContext(player).setSendMessage(false);
                SPRAY_INFO_OFF.sendMessage(sender, null);
            }
        }
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof Player)) return EMPTY_LIST;

        if (args.length == 2)
            return StringUtil.copyPartialMatches(args[1], OPERATIONS, new ArrayList<>());

        return EMPTY_LIST;
    }
}