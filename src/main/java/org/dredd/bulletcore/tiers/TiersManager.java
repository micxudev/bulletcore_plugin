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
import java.util.function.Supplier;

import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.dredd.bulletcore.BulletCore;
import org.dredd.bulletcore.utils.ComponentUtils;
import org.dredd.bulletcore.utils.JsonUtils;
import org.dredd.bulletcore.utils.LogUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import tools.jackson.core.type.TypeReference;

public final class TiersManager {

    // ----------< Static >----------

    private static final String TIERS_FOLDER_NAME = "tiers";

    private static final String CONFIG_FILE_NAME = "tiers.yml";

    private static final String DATA_FILE_NAME = "tiers.json";

    private static final String LOG_FILE_NAME = "tiers.log";

    private static final int TOP_MAX_SIZE = 10; // can be added to config

    private static TiersManager instance;

    public static void load(@NotNull BulletCore plugin) {
        instance = new TiersManager(plugin);
        instance.tops.rebuildOnStartUp();
    }


    // ----------< Instance >----------

    // -----< Attributes >-----

    private final BulletCore plugin;

    // 1. Config
    private final Map<String, Tier> tiersByName;

    private final Map<String, TierKit> tierKitsByName;

    // 2. Data
    private final File tiersDataFile;

    /**
     * Tiers storage format:
     * <pre>{@code
     * PlayerUUID -> {
     *     "tier_kit_name" -> "tier_name",
     *     ...
     * }
     * }</pre>
     */
    private final Map<UUID, Map<String, String>> playerTiersStorage;

    private final Tops tops;

    // 3. Log
    private final File tiersLogFile;

    // -----< Construction >-----

    private TiersManager(@NotNull BulletCore plugin) {
        this.plugin = plugin;

        final File tiersFolder = new File(plugin.getDataFolder(), TIERS_FOLDER_NAME);
        if (!tiersFolder.exists() && !tiersFolder.mkdirs()) {
            plugin.logError("Failed to create tiers folder \"" + tiersFolder + "\"");
        }

        // 1. Config
        final var tiersConfig = loadConfig(new File(tiersFolder, CONFIG_FILE_NAME));
        this.tiersByName = tiersConfig.left();
        this.tierKitsByName = tiersConfig.right();

        // 2. Data
        this.tiersDataFile = new File(tiersFolder, DATA_FILE_NAME);
        this.playerTiersStorage = JsonUtils.load(tiersDataFile, new TypeReference<>() {}, new HashMap<>());

        this.tops = new Tops(TOP_MAX_SIZE);

        // 3. Log
        this.tiersLogFile = new File(tiersFolder, LOG_FILE_NAME);
    }

    private @NotNull Pair<Map<String, Tier>, Map<String, TierKit>> loadConfig(@NotNull File configFile) {
        try {
            if (!configFile.exists()) {
                writeDefaultConfig(configFile);
                return Pair.of(Collections.emptyMap(), Collections.emptyMap());
            }

            final var config = new YamlConfiguration();
            config.load(configFile);

            final Map<String, Tier> tiers;
            final Map<String, TierKit> kits;

            final ConfigurationSection tiersSection = config.getConfigurationSection("tiers");
            if (tiersSection != null) {
                tiers = loadTiers(tiersSection);
                plugin.logInfo("-Loaded " + tiers.size() + " tiers");
            } else {
                tiers = Collections.emptyMap();
                plugin.logError("Missing 'tiers' section in file \"" + configFile + "\"");
            }

            final ConfigurationSection kitsSection = config.getConfigurationSection("kits");
            if (kitsSection != null) {
                kits = loadTierKits(kitsSection);
                plugin.logInfo("-Loaded " + kits.size() + " tier kits");
            } else {
                kits = Collections.emptyMap();
                plugin.logError("Missing 'kits' section in file \"" + configFile + "\"");
            }

            return Pair.of(tiers, kits);

        } catch (Exception e) {
            plugin.logError("Failed to load tiers config file \"" + configFile + "\": " + e.getMessage());
            return Pair.of(Collections.emptyMap(), Collections.emptyMap());
        }
    }

    private void writeDefaultConfig(@NotNull File configFile) throws Exception {
        final var config = new YamlConfiguration();

        final ConfigurationSection tiersSection = config.createSection("tiers");

        final ConfigurationSection tierSection = tiersSection.createSection("tier_name");
        tierSection.set("points", 100);
        tierSection.set("displayName", "<!i><b><red>Name</red></b></!i>");

        final ConfigurationSection kitsSection = config.createSection("kits");

        final ConfigurationSection kitSection = kitsSection.createSection("kit_name");
        kitSection.set("icon", "<symbol>");
        kitSection.set("displayName", "<!i><b><red>Name</red></b></!i>");

        config.save(configFile);
    }

    private @NotNull Map<String, Tier> loadTiers(@NotNull ConfigurationSection tiersSection) {
        final Map<String, Tier> result = new HashMap<>();

        for (final String tierName : tiersSection.getKeys(false)) {
            final ConfigurationSection tierSection = tiersSection.getConfigurationSection(tierName);

            if (tierSection == null) {
                plugin.logError("Skipping tier \"" + tierName + "\": is not a config section");
                continue;
            }

            final int points = Math.clamp(tierSection.getInt("points", 100), 0, 1_000);
            final Component displayName = tierSection.getRichMessage("displayName", ComponentUtils.plainWhite(tierName));

            final Tier tier = new Tier(tierName, points, displayName);
            result.put(tierName, tier);
        }

        return Collections.unmodifiableMap(result);
    }

    private @NotNull Map<String, TierKit> loadTierKits(@NotNull ConfigurationSection kitsSection) {
        final Map<String, TierKit> result = new LinkedHashMap<>();

        for (final String kitName : kitsSection.getKeys(false)) {
            final ConfigurationSection kitSection = kitsSection.getConfigurationSection(kitName);

            if (kitSection == null) {
                plugin.logError("Skipping tier kit \"" + kitName + "\": is not a config section");
                continue;
            }

            final String icon = kitSection.getString("icon", "");
            final Component displayName = kitSection.getRichMessage("displayName", ComponentUtils.plainWhite(kitName));

            final TierKit tierKit = new TierKit(kitName, icon, displayName);
            result.put(kitName, tierKit);
        }

        return Collections.unmodifiableMap(result);
    }


    // ----------< Public API >----------

    public static @Nullable Tier getTierByNameOrNull(@Nullable String tierName) {
        return tierName != null ? instance.tiersByName.get(tierName) : null;
    }

    public static @NotNull @Unmodifiable Collection<String> getAllTierNames() {
        return Collections.unmodifiableSet(instance.tiersByName.keySet());
    }

    public static @Nullable TierKit getTierKitByNameOrNull(@Nullable String kitName) {
        return kitName != null ? instance.tierKitsByName.get(kitName) : null;
    }

    public static @NotNull @Unmodifiable Collection<String> getAllTierKitNames() {
        return Collections.unmodifiableSet(instance.tierKitsByName.keySet());
    }

    public static boolean setTier(@NotNull CommandSender sender,
                                  @NotNull Player player,
                                  @NotNull TierKit kit,
                                  @NotNull Tier tier) {
        final UUID playerId = player.getUniqueId();

        final var playerTiers =
            instance.playerTiersStorage.computeIfAbsent(playerId, k -> new HashMap<>());

        final String kitName = kit.name();
        final String newTierName = tier.name();

        final String oldTierName = playerTiers.put(kitName, newTierName);
        if (newTierName.equals(oldTierName)) return false;

        final String playerName = player.getName();

        // 1. Update top
        instance.tops.onTierSet(
            playerId,
            playerName,
            kitName,
            getTierByNameOrNull(oldTierName),
            tier,
            () -> getHighestTier(playerTiers).right() // will compute only if needed
        );

        // 2. TODO: set tag for player

        // 3. Save log entry
        final String logMessage =
            "sender: " + sender.getName() +
                ", kit: " + kitName +
                ", tier: " + newTierName +
                ", player: " + playerName;

        LogUtils.appendLogAsync(logMessage, instance.tiersLogFile);

        // 4. Save storage
        JsonUtils.saveAsync(instance.playerTiersStorage, instance.tiersDataFile, true);

        return true;
    }

    public static @Nullable TiersManager.Tops.Entry getGlobalTopEntry(int place) {
        return instance.tops.globalTop.getTopEntry(place);
    }

    public static @Nullable TiersManager.Tops.Entry getKitTopEntry(@NotNull String kitName, int place) {
        final var kitTop = instance.tops.topsByKitName.get(kitName);
        return kitTop != null ? kitTop.getTopEntry(place) : null;
    }

    // TODO: replace by single map (UUID -> Pair<TierKit, Tier>)
    private static @NotNull Pair<TierKit, Tier> getHighestTier(@NotNull Map<String, String> playerKitTiers) {
        TierKit highestTierKit = TierKit.EMPTY;
        Tier highestTier = Tier.EMPTY;
        int highestTierPoints = highestTier.points();

        for (final var entry : instance.tierKitsByName.entrySet()) {
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

        return Pair.of(highestTierKit, highestTier);
    }


    public final class Tops {

        // -----< Attributes >-----

        private final int topMaxSize;

        private final Object2IntMap<UUID> totalPointsByPlayer;

        private final TopList globalTop;

        private final Map<String, TopList> topsByKitName;

        // -----< Construction >-----

        private Tops(int topMaxSize) {
            this.topMaxSize = topMaxSize;
            this.totalPointsByPlayer = new Object2IntOpenHashMap<>();
            this.globalTop = new TopList(topMaxSize);
            this.topsByKitName = new HashMap<>();
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


                final Map<String, String> kits = entry.getValue();

                int totalPlayerPoints = 0;

                for (final var kitEntry : kits.entrySet()) {
                    final String kitName = kitEntry.getKey();

                    final TierKit tierKit = tierKitsByName.get(kitName);
                    if (tierKit == null) {
                        plugin.logError("Saved in data-file tier kit \"" + kitName + "\" no longer found in config. Did you delete/rename it?");
                        continue;
                    }

                    final String tierName = kitEntry.getValue();

                    final Tier tier = tiersByName.get(tierName);
                    if (tier == null) {
                        plugin.logError("Saved in data-file tier \"" + tierName + "\" no longer found in config. Did you delete/rename it?");
                        continue;
                    }

                    totalPlayerPoints += tier.points();

                    updateKitTop(playerId, playerName, kitName, tier);
                }

                totalPointsByPlayer.put(playerId, totalPlayerPoints);
                updateGlobalTop(playerId, playerName, () -> getHighestTier(kits).right(), totalPlayerPoints);
            }
        }

        private void onTierSet(@NotNull UUID playerId,
                               @NotNull String playerName,
                               @NotNull String kitName,
                               @Nullable Tier oldTier,
                               @NotNull Tier newTier,
                               @NotNull Supplier<Tier> highestPlayerTier) {
            final int oldPoints = totalPointsByPlayer.getOrDefault(playerId, 0);
            final int newPoints = oldPoints - (oldTier != null ? oldTier.points() : 0) + newTier.points();

            totalPointsByPlayer.put(playerId, newPoints);
            updateGlobalTop(playerId, playerName, highestPlayerTier, newPoints);
            updateKitTop(playerId, playerName, kitName, newTier);
        }

        private void updateGlobalTop(@NotNull UUID playerId,
                                     @NotNull String playerName,
                                     @NotNull Supplier<Tier> highestPlayerTier,
                                     int newTotalPoints) {
            globalTop.update(playerId, playerName, highestPlayerTier, newTotalPoints);
        }

        private void updateKitTop(@NotNull UUID playerId,
                                  @NotNull String playerName,
                                  @NotNull String kitName,
                                  @NotNull Tier tier) {
            final TopList kitTop = topsByKitName.computeIfAbsent(kitName, k -> new TopList(topMaxSize));
            kitTop.update(playerId, playerName, () -> tier, tier.points());
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
                                @NotNull Supplier<Tier> tier,
                                int points) {
                final List<Entry> top = this.top;
                final int topMaxSize = this.topMaxSize;

                // 1. remove old entry if exists
                for (int i = 0; i < top.size(); i++) {
                    if (top.get(i).playerId().equals(playerId)) {
                        top.remove(i);
                        break;
                    }
                }

                // 2. find insertion index
                int index = 0;
                for (; index < top.size(); index++) {
                    if (top.get(index).points() < points) break;
                }

                // 3. insert
                if (index < topMaxSize) {
                    top.add(index, new Entry(playerId, playerName, tier.get(), points));
                }

                // 4. trim to topMaxSize
                if (top.size() > topMaxSize) {
                    top.remove(topMaxSize);
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