package org.dredd.bulletcore.tiers;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.dredd.bulletcore.BulletCore;
import org.dredd.bulletcore.utils.JsonUtils;
import org.dredd.bulletcore.utils.LogUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import tools.jackson.core.type.TypeReference;

/**
 * Utility class for managing tiers.
 *
 * @author dredd
 * @since 1.0.0
 */
public final class TiersManager {

    /**
     * Private constructor to prevent instantiation.
     */
    private TiersManager() {}

    // ----------< Attributes >----------

    private static BulletCore plugin;

    private static Config config;

    private static File tiersDataFile;

    /**
     * Tiers storage format:
     * <pre>{@code
     * PlayerUUID -> {
     *     "tier_kit_name" -> "tier_name",
     *     ...
     * }
     * }</pre>
     */
    private static Map<UUID, Map<String, String>> playerTiersStorage;

    private static Tops tops;

    private static File tiersLogFile;

    // ----------< Init >----------

    /**
     * Loads the tiers data, config from the files, and initializes the top lists.
     */
    public static void load(@NotNull BulletCore plugin) {
        TiersManager.plugin = plugin;

        final File tiersFolder = new File(plugin.getDataFolder(), "tiers");
        if (!tiersFolder.exists() && !tiersFolder.mkdirs()) {
            plugin.logError("Failed to create tiers folder \"" + tiersFolder + "\"");
        }

        config = new Config(new File(tiersFolder, "tiers.yml"));

        tiersDataFile = new File(tiersFolder, "tiers.json");
        playerTiersStorage = JsonUtils.load(tiersDataFile, new TypeReference<>() {}, new HashMap<>());
        tops = new Tops();

        tiersLogFile = new File(tiersFolder, "tiers.log");

        tops.rebuildOnStartUp();
    }

    // ----------< Public API >----------

    public static @Nullable Tier getTierByNameOrNull(@Nullable String tierName) {
        return tierName != null ? config.tiersByName.get(tierName) : null;
    }

    public static @NotNull @Unmodifiable Collection<String> getAllTierNames() {
        return Collections.unmodifiableSet(config.tiersByName.keySet());
    }

    public static @Nullable TierKit getTierKitByNameOrNull(@Nullable String kitName) {
        return kitName != null ? config.tierKitsByName.get(kitName) : null;
    }

    public static @NotNull @Unmodifiable Collection<String> getAllTierKitNames() {
        return Collections.unmodifiableSet(config.tierKitsByName.keySet());
    }

    public static boolean setTier(@NotNull CommandSender sender,
                                  @NotNull UUID playerId,
                                  @NotNull String playerName,
                                  @NotNull TierKit tierKit,
                                  @Nullable Tier tier) {
        final String kitName = tierKit.name();
        final String newTierName = (tier == null) ? null : tier.name();

        final var playerKitTiers =
            playerTiersStorage.computeIfAbsent(playerId, k -> new HashMap<>());

        final String oldTierName;
        if (newTierName == null) {
            // remove tier request
            oldTierName = playerKitTiers.remove(kitName);
            if (oldTierName == null) return false;

            if (playerKitTiers.isEmpty()) {
                playerTiersStorage.remove(playerId);
            }

            // remove old tier
            final Tier tierToRemove = getTierByNameOrNull(oldTierName);
            if (tierToRemove != null) {
                tops.onTierRemove(
                    playerId,
                    playerName,
                    kitName,
                    tierToRemove,
                    getHighestKitTier(playerKitTiers)
                );
            }

        } else {
            // set tier request
            oldTierName = playerKitTiers.put(kitName, newTierName);
            if (newTierName.equals(oldTierName)) return false;

            // set new tier
            tops.onTierSet(
                playerId,
                playerName,
                tierKit,
                getTierByNameOrNull(oldTierName),
                tier,
                getHighestKitTier(playerKitTiers)
            );
        }

        // Save log entry
        final String logMessage =
            "sender: " + sender.getName() +
                ", kit: " + kitName +
                ", old tier: " + (oldTierName == null ? "--none" : oldTierName) +
                ", new tier: " + (newTierName == null ? "--none" : newTierName) +
                ", player: " + playerName;

        LogUtils.appendLogAsync(logMessage, tiersLogFile);

        // Save storage
        JsonUtils.saveAsync(playerTiersStorage, tiersDataFile, true);

        return true;
    }

    public static @Nullable TiersManager.Tops.Entry getGlobalTopEntry(int place) {
        return tops.globalTop.getTopEntry(place);
    }

    public static @Nullable TiersManager.Tops.Entry getKitTopEntry(@NotNull String kitName, int place) {
        final var kitTop = tops.topsByKitName.get(kitName);
        return kitTop != null ? kitTop.getTopEntry(place) : null;
    }

    public static @Nullable Pair<TierKit, Tier> getPlayerHighestKitTier(@NotNull UUID playerId) {
        return tops.highestKitTierByPlayer.get(playerId);
    }

    public static int getPlayerTotalPoints(@NotNull UUID playerId) {
        return tops.totalPointsByPlayer.getOrDefault(playerId, 0);
    }

    public static @Nullable Tier getPlayerTierOnKit(@NotNull UUID playerId,
                                                    @NotNull String kitName) {
        final var playerKitTiers = playerTiersStorage.get(playerId);
        if (playerKitTiers == null) return null;

        final String tierName = playerKitTiers.get(kitName);

        return getTierByNameOrNull(tierName);
    }

    public static @NotNull String getEmptyPlaceholderValue() {
        return config.emptyPlaceholderValue;
    }

    // ----------< Internal >----------

    private static @Nullable Pair<TierKit, Tier> getHighestKitTier(@NotNull Map<String, String> playerKitTiers) {
        if (playerKitTiers.isEmpty()) return null;

        TierKit highestTierKit = null;
        Tier highestTier = null;
        int highestTierPoints = Integer.MIN_VALUE;

        for (final var entry : config.tierKitsByName.entrySet()) {
            final String kitName = entry.getKey();

            final String tierName = playerKitTiers.get(kitName);
            if (tierName == null) continue; // player does not have tier for this kit

            final Tier tier = getTierByNameOrNull(tierName);
            if (tier == null) continue; // tier not found in config

            // if there are 2 kits, where the tiers are the same
            // the one defined first in config is returned (it has "more priority")
            if (tier.points() > highestTierPoints) {
                highestTierKit = entry.getValue();
                highestTier = tier;
                highestTierPoints = tier.points();
            }
        }

        if (highestTierKit == null) return null;

        return Pair.of(highestTierKit, highestTier);
    }


    private static final class Config {

        // ----------< Static >----------

        /**
         * Valid name pattern for tier, kit-tier names.
         */
        public static final Pattern VALID_NAME = Pattern.compile("[a-zA-Z0-9]+");

        private static boolean isValidName(@Nullable String name) {
            return name != null && VALID_NAME.matcher(name).matches();
        }


        // ----------< Instance >----------

        // -----< Attributes >-----

        private final String emptyPlaceholderValue;

        private final int globalTopSize;

        private final Map<String, Tier> tiersByName;

        private final Map<String, TierKit> tierKitsByName;

        // -----< Construction >-----

        private Config(@NotNull File configFile) {
            final YamlConfiguration config = loadConfig(configFile);

            this.emptyPlaceholderValue = config.getString("emptyPlaceholderValue", "<empty>");
            this.globalTopSize = config.getInt("globalTopSize", 10);

            final ConfigurationSection tiersSection = config.getConfigurationSection("tiers");
            if (tiersSection != null) {
                this.tiersByName = loadTiers(tiersSection);
                plugin.logInfo("-Loaded " + tiersByName.size() + " tier(s)");
            } else {
                this.tiersByName = Collections.emptyMap();
                plugin.logError("Missing 'tiers' section in file \"" + configFile + "\"");
            }

            final ConfigurationSection kitsSection = config.getConfigurationSection("kits");
            if (kitsSection != null) {
                this.tierKitsByName = loadTierKits(kitsSection);
                plugin.logInfo("-Loaded " + tierKitsByName.size() + " tier kit(s)");
            } else {
                this.tierKitsByName = Collections.emptyMap();
                plugin.logError("Missing 'kits' section in file \"" + configFile + "\"");
            }
        }

        // -----< Load/Save >-----

        private @NotNull YamlConfiguration loadConfig(@NotNull File configFile) {
            final var config = new YamlConfiguration();
            try {
                if (!configFile.exists()) {
                    writeDefaultConfig(configFile);
                    plugin.logInfo("Created default tiers config file \"" + configFile + "\"");
                    return config;
                }

                config.load(configFile);

            } catch (Exception e) {
                plugin.logError("Failed to load tiers config file \"" + configFile + "\": " + e.getMessage());
            }
            return config;
        }

        private void writeDefaultConfig(@NotNull File configFile) throws Exception {
            final var config = new YamlConfiguration();

            config.set("emptyPlaceholderValue", "<empty>");
            config.set("globalTopSize", 10);

            final ConfigurationSection tiersSection = config.createSection("tiers");

            final ConfigurationSection tierSection = tiersSection.createSection("tierName");
            tierSection.set("points", 100);
            tierSection.set("displayName", "<b><blue>Tier Name</b><white>");


            final ConfigurationSection kitsSection = config.createSection("kits");

            final ConfigurationSection kitSection = kitsSection.createSection("kitName");
            kitSection.set("icon", "<symbol>");
            kitSection.set("displayName", "<b><red>Kit Name</b><white>");
            kitSection.set("topSize", 10);

            config.save(configFile);
        }

        private @NotNull Map<String, Tier> loadTiers(@NotNull ConfigurationSection tiersSection) {
            final Map<String, Tier> result = new HashMap<>();

            for (final String tierName : tiersSection.getKeys(false)) {
                final ConfigurationSection tierSection = tiersSection.getConfigurationSection(tierName);

                if (!isValidName(tierName)) {
                    plugin.logError("Skipping tier \"" + tierName + "\": Does not match pattern " + VALID_NAME.pattern());
                    continue;
                }

                if (tierSection == null) {
                    plugin.logError("Skipping tier \"" + tierName + "\": Is not a section");
                    continue;
                }

                final int points = Math.clamp(tierSection.getInt("points", 100), 0, 1_000);
                final String displayName = tierSection.getString("displayName", tierName);

                final Tier tier = new Tier(tierName, points, displayName);
                result.put(tierName, tier);
            }

            return Collections.unmodifiableMap(result);
        }

        private @NotNull Map<String, TierKit> loadTierKits(@NotNull ConfigurationSection kitsSection) {
            final Map<String, TierKit> result = new LinkedHashMap<>();

            for (final String kitName : kitsSection.getKeys(false)) {
                final ConfigurationSection kitSection = kitsSection.getConfigurationSection(kitName);

                if (!isValidName(kitName)) {
                    plugin.logError("Skipping tier kit \"" + kitName + "\": Does not match pattern " + VALID_NAME.pattern());
                    continue;
                }

                if (kitSection == null) {
                    plugin.logError("Skipping tier kit \"" + kitName + "\": Is not a section");
                    continue;
                }

                final String icon = kitSection.getString("icon", "");
                final String displayName = kitSection.getString("displayName", kitName);
                final int topSize = Math.clamp(kitSection.getInt("topSize", 10), 1, 100);

                final TierKit tierKit = new TierKit(kitName, icon, displayName, topSize);
                result.put(kitName, tierKit);
            }

            return Collections.unmodifiableMap(result);
        }
    }


    private static final class Tops {

        // -----< Attributes >-----

        private final TopList globalTop;

        private final Map<String, TopList> topsByKitName;

        private final Map<UUID, Pair<TierKit, Tier>> highestKitTierByPlayer;

        private final Object2IntMap<UUID> totalPointsByPlayer;

        // -----< Construction >-----

        private Tops() {
            this.globalTop = new TopList(config.globalTopSize);
            this.topsByKitName = new HashMap<>();
            this.highestKitTierByPlayer = new HashMap<>();
            this.totalPointsByPlayer = new Object2IntOpenHashMap<>();
        }

        // -----< Internal Updates >-----

        private void rebuildOnStartUp() {
            for (final var entry : playerTiersStorage.entrySet()) {
                final UUID playerId = entry.getKey();

                final String playerName = Bukkit.getOfflinePlayer(playerId).getName();
                if (playerName == null) {
                    // most probably player .dat file was deleted
                    plugin.logError("Could not load player data for UUID \"" + playerId + "\"");
                    continue;
                }


                // kitName -> tierName
                final Map<String, String> playerKitTiers = entry.getValue();
                if (playerKitTiers.isEmpty()) continue;

                int playerTotalPoints = 0;

                for (final var kitEntry : playerKitTiers.entrySet()) {
                    final String kitName = kitEntry.getKey();

                    final TierKit tierKit = config.tierKitsByName.get(kitName);
                    if (tierKit == null) {
                        plugin.logError("Saved in data-file tier kit \"" + kitName + "\" no longer found in config. Did you delete/rename it?");
                        continue;
                    }

                    final String tierName = kitEntry.getValue();

                    final Tier tier = config.tiersByName.get(tierName);
                    if (tier == null) {
                        plugin.logError("Saved in data-file tier \"" + tierName + "\" no longer found in config. Did you delete/rename it?");
                        continue;
                    }

                    playerTotalPoints += tier.points();

                    updateKitTop(playerId, playerName, tierKit, tier);
                }

                final var playerHighestKitTier = getHighestKitTier(playerKitTiers);
                if (playerHighestKitTier == null) continue;

                highestKitTierByPlayer.put(playerId, playerHighestKitTier);
                totalPointsByPlayer.put(playerId, playerTotalPoints);
                updateGlobalTop(playerId, playerName, playerHighestKitTier.right(), playerTotalPoints);
            }
        }


        private void onTierRemove(@NotNull UUID playerId,
                                  @NotNull String playerName,
                                  @NotNull String kitName,
                                  @NotNull Tier tier,
                                  @Nullable Pair<TierKit, Tier> playerHighestKitTier) {
            if (playerHighestKitTier == null) {
                // player has no more tiers
                highestKitTierByPlayer.remove(playerId);
                totalPointsByPlayer.removeInt(playerId);
                removeFromGlobalTop(playerId);
            } else {
                final int oldPoints = totalPointsByPlayer.getOrDefault(playerId, 0);
                final int newPoints = oldPoints - tier.points();

                highestKitTierByPlayer.put(playerId, playerHighestKitTier);
                totalPointsByPlayer.put(playerId, newPoints);
                updateGlobalTop(playerId, playerName, playerHighestKitTier.right(), newPoints);
            }
            removeFromKitTop(playerId, kitName);
        }

        private void onTierSet(@NotNull UUID playerId,
                               @NotNull String playerName,
                               @NotNull TierKit tierKit,
                               @Nullable Tier oldTier,
                               @NotNull Tier newTier,
                               @NotNull Pair<TierKit, Tier> playerHighestKitTier) {
            final int oldPoints = totalPointsByPlayer.getOrDefault(playerId, 0);
            final int newPoints = oldPoints - (oldTier != null ? oldTier.points() : 0) + newTier.points();

            highestKitTierByPlayer.put(playerId, playerHighestKitTier);
            totalPointsByPlayer.put(playerId, newPoints);
            updateGlobalTop(playerId, playerName, playerHighestKitTier.right(), newPoints);
            updateKitTop(playerId, playerName, tierKit, newTier);
        }


        private void updateGlobalTop(@NotNull UUID playerId,
                                     @NotNull String playerName,
                                     @NotNull Tier playerHighestTier,
                                     int newTotalPoints) {
            globalTop.update(playerId, playerName, playerHighestTier, newTotalPoints);
        }

        private void updateKitTop(@NotNull UUID playerId,
                                  @NotNull String playerName,
                                  @NotNull TierKit tierKit,
                                  @NotNull Tier tier) {
            final TopList kitTop = topsByKitName.computeIfAbsent(tierKit.name(), k -> new TopList(tierKit.topSize()));
            kitTop.update(playerId, playerName, tier, tier.points());
        }


        private void removeFromGlobalTop(@NotNull UUID playerId) {
            globalTop.remove(playerId);
        }

        private void removeFromKitTop(@NotNull UUID playerId,
                                      @NotNull String kitName) {
            final TopList kitTop = topsByKitName.get(kitName);
            if (kitTop == null) return;
            kitTop.remove(playerId);
        }


        private static class TopList {

            private final int topMaxSize;

            private final List<Entry> top;

            private TopList(int topMaxSize) {
                this.topMaxSize = topMaxSize;
                this.top = new ArrayList<>(topMaxSize + 1); // +1 to avoid resize during update
            }

            private @Nullable TiersManager.Tops.Entry getTopEntry(int place) {
                final int index = place - 1;
                if (index < 0 || index >= top.size()) return null;
                return top.get(index);
            }

            private void update(@NotNull UUID playerId,
                                @NotNull String playerName,
                                @NotNull Tier tier,
                                int points) {
                final List<Entry> top = this.top;
                final int topMaxSize = this.topMaxSize;

                // 1. remove old entry if exists
                remove(playerId);

                // 2. find insertion index
                int index = 0;
                for (; index < top.size(); index++) {
                    if (top.get(index).points() < points) break;
                }

                // 3. insert
                if (index < topMaxSize) {
                    top.add(index, new Entry(playerId, playerName, tier, points));
                }

                // 4. trim to topMaxSize
                if (top.size() > topMaxSize) {
                    top.remove(topMaxSize);
                }
            }

            private void remove(@NotNull UUID playerId) {
                final List<Entry> top = this.top;
                for (int i = 0; i < top.size(); i++) {
                    if (top.get(i).playerId().equals(playerId)) {
                        top.remove(i);
                        break;
                    }
                }
            }
        }

        /**
         * Represents an entry in the top.
         *
         * @param playerId UUID of the player
         * @param playerName name of the player
         * @param tier kit top: player tier on this kit, global top: player highest tier on all kits
         * @param points kit top: tier.points(), global top: player total points
         */
        public record Entry(
            UUID playerId,
            String playerName,
            Tier tier,
            int points
        ) {}
    }
}