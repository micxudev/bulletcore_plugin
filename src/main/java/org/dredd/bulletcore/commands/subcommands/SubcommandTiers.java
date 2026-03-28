package org.dredd.bulletcore.commands.subcommands;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.command.CommandSender;
import org.bukkit.util.StringUtil;
import org.dredd.bulletcore.tiers.Tier;
import org.dredd.bulletcore.tiers.TierKit;
import org.dredd.bulletcore.tiers.TiersManager;
import org.dredd.bulletcore.utils.ServerUtils;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import static org.dredd.bulletcore.config.messages.component.ComponentMessage.PLAYER_NOT_FOUND;
import static org.dredd.bulletcore.config.messages.component.ComponentMessage.TIER_ALREADY_SET;
import static org.dredd.bulletcore.config.messages.component.ComponentMessage.TIER_KIT_NOT_FOUND;
import static org.dredd.bulletcore.config.messages.component.ComponentMessage.TIER_NOT_FOUND;
import static org.dredd.bulletcore.config.messages.component.ComponentMessage.TIER_SET_SUCCESS;
import static org.dredd.bulletcore.utils.ServerUtils.EMPTY_LIST;

/**
 * Manages tiers for the player.
 *
 * @author dredd
 * @since 1.0.0
 */
public enum SubcommandTiers implements Subcommand {

    INSTANCE;

    private static final String NONE_TIER_OPTION = "--none";

    @Override
    public @NotNull String getName() {
        return "tiers";
    }

    @Override
    public @NotNull String getUsageArgs() {
        return "<player> <kit> <tier>";
    }

    @Override
    public int getMinArgs() {
        return 3;
    }

    @Override
    public @NotNull String getPermission() {
        return "bulletcore.command.tiers";
    }

    @Override
    public void execute(@NotNull CommandSender sender, @NonNull @NotNull String[] args) {
        final String playerName = args[1];
        final UUID playerId = ServerUtils.getPlayerUUID(playerName);
        if (playerId == null) {
            PLAYER_NOT_FOUND.sendMessage(sender, Map.of("player", playerName));
            return;
        }

        final String kitName = args[2];
        final TierKit kit = TiersManager.getTierKitByNameOrNull(kitName);
        if (kit == null) {
            TIER_KIT_NOT_FOUND.sendMessage(sender, Map.of("kit", kitName));
            return;
        }

        final String tierName = args[3];
        final Tier tier;
        if (tierName.equals(NONE_TIER_OPTION)) {
            tier = null;
        } else {
            tier = TiersManager.getTierByNameOrNull(tierName);
            if (tier == null) {
                TIER_NOT_FOUND.sendMessage(sender, Map.of("tier", tierName));
                return;
            }
        }

        final boolean added = TiersManager.setTier(sender, playerId, playerName, kit, tier);
        (added ? TIER_SET_SUCCESS : TIER_ALREADY_SET).sendMessage(
            sender,
            Map.of(
                "player", playerName,
                "kit", kitName,
                "tier", tierName
            )
        );
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NonNull @NotNull String[] args) {
        final String playerName = args[1];
        if (args.length == 2)
            return StringUtil.copyPartialMatches(playerName, ServerUtils.getKnownPlayerNames(), new ArrayList<>());

        if (ServerUtils.getPlayerUUID(playerName) == null) return EMPTY_LIST;


        final String kitName = args[2];
        if (args.length == 3)
            return StringUtil.copyPartialMatches(kitName, TiersManager.getAllTierKitNames(), new ArrayList<>());

        if (TiersManager.getTierKitByNameOrNull(kitName) == null) return EMPTY_LIST;


        final String tierName = args[3];
        if (args.length == 4) {
            final List<String> output = new ArrayList<>();
            output.add(NONE_TIER_OPTION);
            return StringUtil.copyPartialMatches(tierName, TiersManager.getAllTierNames(), output);
        }

        return EMPTY_LIST;
    }
}