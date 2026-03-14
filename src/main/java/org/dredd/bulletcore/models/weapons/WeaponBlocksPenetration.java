package org.dredd.bulletcore.models.weapons;

import java.util.Locale;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.dredd.bulletcore.BulletCore;
import org.dredd.bulletcore.config.materials.MaterialCategory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class WeaponBlocksPenetration {

    // ----------< Static >----------

    private static final int TOTAL_MATERIALS = MaterialCategory.AllMaterials.TOTAL_MATERIALS;

    // -----< Loader >-----

    /**
     * Loads a {@link WeaponBlocksPenetration} from a YAML config.
     *
     * @param config the configuration to load from
     * @return a new {@link WeaponBlocksPenetration} instance
     */
    public static @NotNull WeaponBlocksPenetration load(@NotNull YamlConfiguration config) {
        return new WeaponBlocksPenetration(config);
    }


    // ----------< Instance >----------

    // -----< Attributes >-----

    private final int[] blockPenetrationLimits;

    // -----< Construction >-----

    /**
     * Private constructor. Use {@link #load(YamlConfiguration)} instead.
     */
    private WeaponBlocksPenetration(@NotNull YamlConfiguration config) {
        this.blockPenetrationLimits = parseBlockPenetrationLimits(config);
    }

    /**
     * Parses block penetration limits from the given configuration section.
     * <p>
     * Each key is expected to be the name of a {@link Material} representing
     * a block, and each value defines the maximum number of blocks of that
     * material a bullet may penetrate before being stopped.
     *
     * @param config the configuration to load from
     * @return an array of block materials to their penetration limits
     */
    private int[] parseBlockPenetrationLimits(@Nullable YamlConfiguration config) {
        final int[] result = new int[TOTAL_MATERIALS];

        final ConfigurationSection section = config.getConfigurationSection("blocks_penetration");
        if (section == null) return result;

        for (final String key : section.getKeys(false)) {
            final Material material = Material.getMaterial(key.toUpperCase(Locale.ROOT));

            if (material == null) {
                BulletCore.logError("Invalid material name: " + key);
                continue;
            }

            if (!material.isBlock()) {
                BulletCore.logError(material + " is not a block material; skipping.");
                continue;
            }

            final int penetrationLimit = Math.clamp(section.getInt(key, 1), 1, 100);

            result[material.ordinal()] = penetrationLimit;
        }

        return result;
    }

    // -----< Accessors >-----

    /**
     * Returns the block penetration limit for the given material.
     * <p>
     * The returned value represents how many blocks of this material
     * a bullet may go through before being stopped.
     *
     * @param material the block material being tested
     * @return the penetration limit, or {@code 0} if none is defined
     */
    public int getPenetrationLimit(@NotNull Material material) {
        return blockPenetrationLimits[material.ordinal()];
    }
}