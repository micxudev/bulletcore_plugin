package org.dredd.bulletcore.hooks;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.dredd.bulletcore.tiers.TiersManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class BulletCorePlaceholderExpansion extends PlaceholderExpansion {

    @Override
    public @NotNull String getIdentifier() {return "bulletcore";}

    @Override
    public @NotNull String getAuthor() {return "dredd";}

    @Override
    public @NotNull String getVersion() {return "1.0.0";}

    @Override
    public boolean persist() {return true;}

    @Override
    public boolean canRegister() {return true;}

    @Override
    public @Nullable String onRequest(@Nullable OfflinePlayer player,
                                      @NotNull String params) {
        if (params.startsWith("tiers_")) {
            return TiersModule.handle(player, params);
        }

        return null; // null -> incorrect placeholder -> no replacement
    }


    // ----------< Helpers >----------

    // place starts at 1, not 0
    private static int parsePlace(@NotNull String str, int offset) {
        int result = 0;
        for (int i = offset; i < str.length(); i++) {
            final char c = str.charAt(i);
            if (c < '0' || c > '9') break;
            result = result * 10 + (c - '0');
        }
        return result;
    }


    // ----------< Modules >----------

    private static final class TiersModule {

        /*
            tiers module placeholders:

            global top:
            %bulletcore_tiers_top_global_player_<place>%
            %bulletcore_tiers_top_global_tier_<place>%
            %bulletcore_tiers_top_global_points_<place>%

            kit top:
            %bulletcore_tiers_top_kit_<kitName>_player_<place>%
            %bulletcore_tiers_top_kit_<kitName>_tier_<place>%
            %bulletcore_tiers_top_kit_<kitName>_points_<place>%

            player highest kit-tier:
            %bulletcore_tiers_highest_total_points%
            %bulletcore_tiers_highest_kit_name%
            %bulletcore_tiers_highest_kit_icon%
            %bulletcore_tiers_highest_kit_displayname%
            %bulletcore_tiers_highest_tier_name%
            %bulletcore_tiers_highest_tier_displayname%
        */

        private static @Nullable String handle(@Nullable OfflinePlayer player,
                                               @NotNull String params) {
            if (params.startsWith("top_", 6)) {
                return handleTop(params);
            }

            if (params.startsWith("highest_", 6)) {
                return handleHighest(player, params);
            }

            return null;
        }


        private static @Nullable String handleTop(@NotNull String params) {
            if (params.startsWith("global_", 10)) {
                return handleTopGlobal(params);
            }

            if (params.startsWith("kit_", 10)) {
                return handleTopKit(params);
            }

            return null;
        }

        private static @Nullable String handleTopGlobal(@NotNull String params) {
            if (params.startsWith("player_", 17)) {
                final int place = parsePlace(params, 24);
                final var entry = TiersManager.getGlobalTopEntry(place);
                return entry != null ? entry.playerName() : "";
            }

            if (params.startsWith("tier_", 17)) {
                final int place = parsePlace(params, 22);
                final var entry = TiersManager.getGlobalTopEntry(place);
                return entry != null ? entry.tier().name() : "";
            }

            if (params.startsWith("points_", 17)) {
                final int place = parsePlace(params, 24);
                final var entry = TiersManager.getGlobalTopEntry(place);
                return entry != null ? Integer.toString(entry.points()) : "";
            }

            return null;
        }

        private static @Nullable String handleTopKit(@NotNull String params) {
            // find next '_' -> end of kitName
            final int kitEnd = params.indexOf('_', 14);
            if (kitEnd == -1) return null;

            final String kitName = params.substring(14, kitEnd);

            final int next = kitEnd + 1;

            if (params.startsWith("player_", next)) {
                final int place = parsePlace(params, next + 7);
                final var entry = TiersManager.getKitTopEntry(kitName, place);
                return entry != null ? entry.playerName() : "";
            }

            if (params.startsWith("tier_", next)) {
                final int place = parsePlace(params, next + 5);
                final var entry = TiersManager.getKitTopEntry(kitName, place);
                return entry != null ? entry.tier().name() : "";
            }

            if (params.startsWith("points_", next)) {
                final int place = parsePlace(params, next + 7);
                final var entry = TiersManager.getKitTopEntry(kitName, place);
                return entry != null ? Integer.toString(entry.points()) : "";
            }

            return null;
        }


        private static @Nullable String handleHighest(@Nullable OfflinePlayer player,
                                                      @NotNull String params) {
            if (player == null) return "";

            if (params.startsWith("total_points", 14)) {
                final int totalPoints = TiersManager.getPlayerTotalPoints(player.getUniqueId());
                return Integer.toString(totalPoints);
            }

            final var pair = TiersManager.getPlayerHighestKitTier(player.getUniqueId());
            if (pair == null) return "";

            final var kit = pair.left();
            final var tier = pair.right();

            if (params.startsWith("kit_name", 14)) return kit.name();
            if (params.startsWith("kit_icon", 14)) return kit.icon();
            if (params.startsWith("kit_displayname", 14)) return kit.displayName();
            if (params.startsWith("tier_name", 14)) return tier.name();
            if (params.startsWith("tier_displayname", 14)) return tier.displayName();

            return null;
        }
    }
}