package org.dredd.bulletcore.tiers;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import it.unimi.dsi.fastutil.Pair;
import net.kyori.adventure.text.Component;
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

    private static TiersManager instance;

    public static void load(@NotNull BulletCore plugin) {
        instance = new TiersManager(plugin);
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
        final Map<String, TierKit> result = new HashMap<>();

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

    public static @Nullable Tier getTierByNameOrNull(@NotNull String tierName) {
        return instance.tiersByName.get(tierName);
    }

    public static @NotNull @Unmodifiable Collection<String> getAllTierNames() {
        return Collections.unmodifiableSet(instance.tiersByName.keySet());
    }

    public static @Nullable TierKit getTierKitByNameOrNull(@NotNull String kitName) {
        return instance.tierKitsByName.get(kitName);
    }

    public static @NotNull @Unmodifiable Collection<String> getAllTierKitNames() {
        return Collections.unmodifiableSet(instance.tierKitsByName.keySet());
    }

    public static boolean setTier(@NotNull CommandSender sender,
                                  @NotNull Player player,
                                  @NotNull TierKit kit,
                                  @NotNull Tier tier) {
        final var playerTiers = instance.playerTiersStorage.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>());

        final var currentTierName = playerTiers.get(kit.name());
        if (currentTierName != null && currentTierName.equals(tier.name())) return false;

        playerTiers.put(kit.name(), tier.name());

        // 1. TODO: recalculate points

        // 2. TODO: set tag for player

        // 3. Save log entry
        final String logMessage =
            "sender: " + sender.getName() +
                ", kit: " + kit.name() +
                ", tier: " + tier.name() +
                ", player: " + player.getName();

        LogUtils.appendLogAsync(logMessage, instance.tiersLogFile);

        // 4. Save storage
        JsonUtils.saveAsync(instance.playerTiersStorage, instance.tiersDataFile, true);

        return true;
    }
}