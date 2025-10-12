package org.dredd.bulletcore.config.particles;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.dredd.bulletcore.BulletCore;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.NoSuchElementException;

/**
 * Utility class for loading and spawning configured particles.
 * <p>
 * Particle configurations must be located under the {@code particles.<key>} path in the YAML.
 *
 * @author dredd
 * @since 1.0.0
 */
public class ParticleManager {

    /**
     * Private constructor to prevent instantiation.
     */
    private ParticleManager() {}

    /**
     * Parses a {@link ConfiguredParticle} from the given config using the key under {@code particles.<key>}.
     *
     * @param cfg the YAML configuration to parse from
     * @param key the key under {@code particles.<key>} to look up
     * @return a validated {@link ConfiguredParticle} instance
     * @throws NoSuchElementException   if the configuration is missing
     * @throws IllegalArgumentException if the configuration is invalid
     */
    private static @NotNull ConfiguredParticle parseParticle(@NotNull FileConfiguration cfg,
                                                             @NotNull String key) {
        String fullKey = "particles." + key;
        ConfigurationSection section = cfg.getConfigurationSection(fullKey);
        if (section == null)
            throw new NoSuchElementException("Missing particle configuration for key: " + fullKey);

        String particleName = section.getString("particle");

        Particle particle;
        try {
            particle = Particle.valueOf(particleName.toUpperCase(Locale.ROOT));
        } catch (Throwable e) {
            throw new IllegalArgumentException("Invalid particle '" + particleName + "' for key: " + fullKey);
        }

        int count = Math.clamp(section.getInt("count", 1), 0, Integer.MAX_VALUE);

        Object data;
        try {
            data = getParticleData(particle.getDataType(), section);
        } catch (Throwable e) {
            throw new IllegalArgumentException("Invalid particle data for key: " + fullKey + " - " + e.getMessage());
        }

        return new ConfiguredParticle(particle, count, data);
    }

    /**
     * Parses particle-specific data for a given particle type.
     * <p>
     * Most particles have {@link Void} data (no extra values), in which case this
     * method returns {@code null}.<br>
     * For special particle types, additional values are required.
     *
     * @param dataType the expected particle data type, as returned by {@link Particle#getDataType()}
     * @param section  the configuration section containing particle options
     * @return the parsed particle data object, or {@code null} if no data is required
     * @throws IllegalArgumentException if required values are missing, invalid, or unsupported
     */
    private static @Nullable Object getParticleData(@NotNull Class<?> dataType,
                                                    @NotNull ConfigurationSection section) {
        // 0. most of the particles have Void data class
        if (dataType == Void.class) return null;

        // 1. DUST
        if (dataType == Particle.DustOptions.class) {
            Color color = parseColor(section.getString("color"));
            float size = clampSize(section.getDouble("size", 1.0));
            return new Particle.DustOptions(color, size);
        }

        // 2. ITEM
        if (dataType == ItemStack.class) {
            Material type = parseItem(section.getString("item"));
            ItemStack itemStack = new ItemStack(type);
            if (itemStack.isEmpty())
                throw new IllegalArgumentException("Empty stack is not allowed for particle item");
            return itemStack;
        }

        // 3. BLOCK, FALLING_DUST, DUST_PILLAR, BLOCK_MARKER
        if (dataType == BlockData.class) {
            Material type = parseItem(section.getString("item"));
            return Bukkit.createBlockData(type);
        }

        // 4. DUST_COLOR_TRANSITION
        if (dataType == Particle.DustTransition.class) {
            Color fromColor = parseColor(section.getString("from-color"));
            Color toColor = parseColor(section.getString("to-color"));
            float size = clampSize(section.getDouble("size", 1.0));
            return new Particle.DustTransition(fromColor, toColor, size);
        }

        // 5. VIBRATION
        // if (dataType == Vibration.class) // Unsupported

        // 6. SCULK_CHARGE
        if (dataType == Float.class)
            return (float) section.getDouble("roll", 1.0);

        // 7. SHRIEK
        if (dataType == Integer.class)
            return section.getInt("delay", 0);

        // 8. ENTITY_EFFECT
        if (dataType == Color.class)
            return parseColor(section.getString("color"));

        throw new IllegalArgumentException("Unsupported particle data type: " + dataType.getSimpleName());
    }

    /**
     * Parses a hex-encoded RGB color string into a Bukkit {@link Color}.
     * <p>
     * The expected format is <code>#RRGGBB</code> (e.g. <code>#FF0000</code> for red).
     *
     * @param colorStr the color string to parse, must be non-null and in format "#RRGGBB"
     * @return the parsed {@link Color} instance
     * @throws IllegalArgumentException if the string is null, malformed, or not a valid hex RGB value
     */
    private static @NotNull Color parseColor(@Nullable String colorStr) {
        try {
            return Color.fromRGB(Integer.parseInt(colorStr.substring(1), 16));
        } catch (Throwable e) {
            throw new IllegalArgumentException("Expected hex color format #RRGGBB, but got: '" + colorStr + "'");
        }
    }

    /**
     * Clamps a particle size value into the allowed range [0.01, 4.0] (taken from NMS).
     *
     * @param size the raw size value to clamp
     * @return the clamped size as a float
     */
    private static float clampSize(double size) {
        return (float) Math.clamp(size, 0.01, 4.0);
    }

    /**
     * Parses a material name into a Bukkit {@link Material}.
     *
     * @param itemName the material name to parse, case-insensitive
     * @return the corresponding {@link Material} enum constant
     * @throws IllegalArgumentException if the name is null, empty, or not a valid material
     */
    private static @NotNull Material parseItem(@Nullable String itemName) {
        try {
            return Material.valueOf(itemName.toUpperCase(Locale.ROOT));
        } catch (Throwable e) {
            throw new IllegalArgumentException("This particle requires 'item' value, but got: '" + itemName + "'");
        }
    }

    /**
     * Loads a {@link ConfiguredParticle} from config, falling back to a default if parsing fails.
     *
     * @param cfg the YAML configuration
     * @param key the key under {@code particles.<key>} to load
     * @param def the fallback {@link ConfiguredParticle} to use if parsing fails
     * @return a valid {@link ConfiguredParticle}, either parsed, or fallback
     */
    public static @NotNull ConfiguredParticle loadParticle(@NotNull FileConfiguration cfg,
                                                           @NotNull String key,
                                                           @NotNull ConfiguredParticle def) {
        try {
            return parseParticle(cfg, key);
        } catch (NoSuchElementException ignored) {
            // Ignored, the particle configuration is optional
        } catch (IllegalArgumentException e) {
            BulletCore.logError(e.getMessage() + "; Falling back to default particle");
        }
        return def;
    }

    /**
     * Spawns the given {@link ConfiguredParticle} at the specified location in the world.
     *
     * @param world    the world to spawn the particle in
     * @param location the location where the particle should appear
     * @param particle the configured particle to spawn
     */
    public static void spawnParticle(@NotNull World world,
                                     @NotNull Location location,
                                     @NotNull ConfiguredParticle particle) {
        world.spawnParticle(particle.particle(), location, particle.count(), particle.data());
    }
}