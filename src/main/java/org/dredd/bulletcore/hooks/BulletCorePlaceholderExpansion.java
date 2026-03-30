package org.dredd.bulletcore.hooks;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.dredd.bulletcore.tiers.Tier;
import org.dredd.bulletcore.tiers.TiersManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * PlaceholderAPI expansion for BulletCore.
 * <p>
 * Placeholders use the format: {@code %bulletcore_params%}.
 * <p>
 * Requests are handled by {@link #onRequest(OfflinePlayer, String)},
 * where {@code params} is the part after the identifier.
 * <p>
 * Return values:
 * <ul>
 *   <li>{@code null} – invalid placeholder (no replacement)</li>
 *   <li>empty string – valid placeholder with no value available</li>
 *   <li>non-empty string – resolved value</li>
 * </ul>
 *
 * @author dredd
 * @since 1.0.0
 */
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

    /**
     * Parses a positive integer starting at the given offset.
     * <p>
     * Reads consecutive digit characters and stops at the first non-digit.
     * <p>
     * Indexing is 1-based (e.g. "player_1" → place = 1).
     *
     * @param str    source string
     * @param offset index to start parsing from
     * @return parsed integer, or {@code 0} if no digits are found
     */
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
            Tiers module placeholders:

            global top:
            %bulletcore_tiers_top_global_player_<place>%
            %bulletcore_tiers_top_global_points_<place>%

            kit top:
            %bulletcore_tiers_top_kit_<kitName>_player_<place>%
            %bulletcore_tiers_top_kit_<kitName>_tier_name_<place>%
            %bulletcore_tiers_top_kit_<kitName>_tier_displayname_<place>%

            player:
            %bulletcore_tiers_player_total_points%

            player highest:
            %bulletcore_tiers_player_highest_kit_name%
            %bulletcore_tiers_player_highest_kit_icon%
            %bulletcore_tiers_player_highest_kit_displayname%
            %bulletcore_tiers_player_highest_tier_name%
            %bulletcore_tiers_player_highest_tier_displayname%

            player kit:
            %bulletcore_tiers_player_kit_<kitName>_tier_name%
            %bulletcore_tiers_player_kit_<kitName>_tier_displayname%
        */

        private static @Nullable String handle(@Nullable OfflinePlayer player,
                                               @NotNull String params) {
            if (params.startsWith("top_", 6)) {
                return handleTop(params);
            }

            if (params.startsWith("player_", 6)) {
                return handlePlayer(player, params);
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
                if (place <= 0) return null;
                final var entry = TiersManager.getGlobalTopEntry(place);
                return entry != null ? entry.playerName() : TiersManager.getEmptyPlaceholderValue();
            }

            if (params.startsWith("points_", 17)) {
                final int place = parsePlace(params, 24);
                if (place <= 0) return null;
                final var entry = TiersManager.getGlobalTopEntry(place);
                return entry != null ? Integer.toString(entry.points()) : TiersManager.getEmptyPlaceholderValue();
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
                if (place <= 0) return null;
                final var entry = TiersManager.getKitTopEntry(kitName, place);
                return entry != null ? entry.playerName() : TiersManager.getEmptyPlaceholderValue();
            }

            if (params.startsWith("tier_name_", next)) {
                final int place = parsePlace(params, next + 10);
                if (place <= 0) return null;
                final var entry = TiersManager.getKitTopEntry(kitName, place);
                return entry != null ? entry.tier().name() : TiersManager.getEmptyPlaceholderValue();
            }

            if (params.startsWith("tier_displayname_", next)) {
                final int place = parsePlace(params, next + 17);
                if (place <= 0) return null;
                final var entry = TiersManager.getKitTopEntry(kitName, place);
                return entry != null ? entry.tier().displayName() : TiersManager.getEmptyPlaceholderValue();
            }

            return null;
        }


        private static @Nullable String handlePlayer(@Nullable OfflinePlayer player,
                                                     @NotNull String params) {
            if (player == null) return TiersManager.getEmptyPlaceholderValue();

            if (params.startsWith("total_points", 13)) {
                final int totalPoints = TiersManager.getPlayerTotalPoints(player.getUniqueId());
                return Integer.toString(totalPoints);
            }

            if (params.startsWith("highest_", 13)) {
                return handlePlayerHighest(player, params);
            }

            if (params.startsWith("kit_", 13)) {
                return handlePlayerKit(player, params);
            }

            return null;
        }

        private static @Nullable String handlePlayerHighest(@NotNull OfflinePlayer player,
                                                            @NotNull String params) {
            final var pair = TiersManager.getPlayerHighestKitTier(player.getUniqueId());
            if (pair == null) return TiersManager.getEmptyPlaceholderValue();

            final var kit = pair.left();
            final var tier = pair.right();

            if (params.startsWith("kit_name", 21)) return kit.name();
            if (params.startsWith("kit_icon", 21)) return kit.icon();
            if (params.startsWith("kit_displayname", 21)) return kit.displayName();
            if (params.startsWith("tier_name", 21)) return tier.name();
            if (params.startsWith("tier_displayname", 21)) return tier.displayName();

            return null;
        }

        private static @Nullable String handlePlayerKit(@NotNull OfflinePlayer player,
                                                        @NotNull String params) {
            // find next '_' -> end of kitName
            final int kitEnd = params.indexOf('_', 17);
            if (kitEnd == -1) return null;

            final String kitName = params.substring(17, kitEnd);

            final int next = kitEnd + 1;

            final Tier tier = TiersManager.getPlayerTierOnKit(player.getUniqueId(), kitName);
            if (tier == null) return TiersManager.getEmptyPlaceholderValue();

            if (params.startsWith("tier_name", next)) return tier.name();
            if (params.startsWith("tier_displayname", next)) return tier.displayName();

            return null;
        }
    }
}