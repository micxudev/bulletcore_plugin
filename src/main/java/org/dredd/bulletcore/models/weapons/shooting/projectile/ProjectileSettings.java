package org.dredd.bulletcore.models.weapons.shooting.projectile;

import java.util.Locale;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.dredd.bulletcore.BulletCore;
import org.dredd.bulletcore.custom_item_manager.exceptions.ItemLoadException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static org.dredd.bulletcore.models.weapons.shooting.projectile.AProjectile.NOT_USED;

/**
 * Immutable configuration describing the physical behavior and visual
 * representation of a weapon projectile.
 * <p>
 * <b>Unit conversion</b>
 * <ul>
 *     <li>Configuration values are typically defined using real-world units
 *     (meters and seconds).</li>
 *     <li>The projectile simulation operates in Minecraft units
 *     (blocks per tick).</li>
 * </ul>
 *
 * Therefore, several values are converted during loading:
 *
 * <ul>
 *     <li>velocity: meters/second → blocks/tick</li>
 *     <li>gravity: meters/second² → blocks/tick²</li>
 * </ul>
 *
 * Since Minecraft runs at <b>~20 ticks per second</b>:
 *
 * <ul>
 *     <li>velocity is divided by <b>20</b></li>
 *     <li>gravity is divided by <b>200</b></li>
 * </ul>
 *
 * After construction, all instance values are already converted
 * to the units expected by the projectile simulation.
 */
public final class ProjectileSettings {

    // ----------< Static >----------

    /**
     * Value representing an invisible projectile.
     * <p>
     * Used as the default {@code disguiseType} when no visual representation
     * should be created for the projectile.
     */
    private static final String INVISIBLE = "INVISIBLE";

    // -----< Loader >-----

    public static @NotNull ProjectileSettings load(@NotNull YamlConfiguration config) throws ItemLoadException {
        final String sectionPath = "projectile";
        ConfigurationSection section = config.getConfigurationSection(sectionPath);
        if (section == null) {
            section = config.createSection(sectionPath);
        }
        return new ProjectileSettings(section);
    }

    // -----< Helpers >-----

    /**
     * Loads the {@link EntityType} used to visually represent the projectile.
     *
     * @param section the configuration section containing disguise settings
     *
     * @return the {@link EntityType} to use for the projectile disguise,
     *         or {@code null} if the projectile should be invisible
     *
     * @throws ItemLoadException if the value is not a valid {@link EntityType}
     */
    private static @Nullable EntityType loadDisguiseType(@NotNull ConfigurationSection section) throws ItemLoadException {
        final String disguiseType = section.getString("disguiseType", INVISIBLE).toUpperCase(Locale.ROOT);
        if (INVISIBLE.equals(disguiseType)) return null;
        try {
            return EntityType.valueOf(disguiseType);
        } catch (IllegalArgumentException e) {
            throw new ItemLoadException("Expected valid EntityType in 'disguiseType', but got: '" + disguiseType + "'");
        }
    }

    /**
     * Loads the data required by a disguise entity, depending on its type.
     * <p>
     * For example:
     * <ul>
     *     <li>{@link Material} is required for:
     *         <ul>
     *             <li>{@link EntityType#FALLING_BLOCK}</li>
     *             <li>{@link EntityType#BLOCK_DISPLAY}</li>
     *         </ul>
     *     </li>
     *     <li>{@link ItemStack} is required for:
     *         <ul>
     *             <li>{@link EntityType#ITEM}</li>
     *             <li>{@link EntityType#ITEM_DISPLAY}</li>
     *             <li>{@link EntityType#ARMOR_STAND}</li>
     *             <li>{@link EntityType#FIREWORK_ROCKET}</li>
     *         </ul>
     *     </li>
     * </ul>
     * <p>
     * If the entity type does not require additional data or is {@code null}, returns {@code null}.
     * <p>
     * Special handling for {@link EntityType#FIREWORK_ROCKET}: if the provided
     * {@link ItemStack} does not have valid {@link FireworkMeta}, a default
     * firework is used and an error is logged.
     *
     * @param section the configuration section containing disguise data
     * @param type    the {@link EntityType} of the disguise, or {@code null}
     *
     * @return the processed disguise data (Material, ItemStack, etc.), or {@code null}
     *
     * @throws ItemLoadException if the configuration value is missing or invalid
     */
    private static @Nullable Object loadDisguiseData(@NotNull ConfigurationSection section,
                                                     @Nullable EntityType type) throws ItemLoadException {
        if (type == null) return null;
        return switch (type) {
            case FALLING_BLOCK, BLOCK_DISPLAY -> {
                // Material is expected
                final String materialName = section.getString("disguiseData", "null");
                final Material material = Material.getMaterial(materialName.toUpperCase(Locale.ROOT));
                if (material == null)
                    throw new ItemLoadException(
                        type + " requires valid material name in 'disguiseData', but got: '" + materialName + "'"
                    );

                yield material;
            }
            case ITEM, FIREWORK_ROCKET, ARMOR_STAND, ITEM_DISPLAY -> {
                // ItemStack is expected
                final var itemStack = section.getItemStack("disguiseData", null);
                if (itemStack == null)
                    throw new ItemLoadException(type + " requires valid ItemStack format in 'disguiseData'");

                if (type == EntityType.FIREWORK_ROCKET && !(itemStack.getItemMeta() instanceof FireworkMeta)) {
                    BulletCore.logError(type + " requires valid FireworkMeta format; using default");
                    yield new ItemStack(Material.FIREWORK_ROCKET);
                }

                yield itemStack;
            }
            default -> null;
        };
    }


    // ----------< Instance >----------

    // -----< Attributes >-----

    /**
     * Type of entity used to visually represent the projectile.
     * <p>
     * If {@code null}, the projectile is invisible and only simulated internally.
     */
    public final @Nullable EntityType disguiseType;

    /**
     * Additional data required by some disguise entity types.
     * @see #loadDisguiseData(ConfigurationSection, EntityType)
     */
    public final @Nullable Object disguiseData;


    /**
     * Initial projectile speed when fired.
     * <p>
     * Stored internally in <b>blocks per tick</b>.
     */
    public final double muzzleVelocity;

    /**
     * Downward acceleration applied to the projectile each tick.
     * <p>
     * Stored internally in <b>blocks per tick²</b>.
     */
    public final double gravity;

    /**
     * Minimum allowed projectile speed.
     * <p>
     * If the projectile speed drops below this value, it may either be clamped
     * or removed depending on configuration.
     * <p>
     * Stored internally in <b>blocks per tick</b>.
     */
    public final double minSpeed;

    /**
     * Maximum allowed projectile speed.
     * <p>
     * If the projectile speed exceeds this value, it may either be clamped
     * or removed depending on configuration.
     * <p>
     * Stored internally in <b>blocks per tick</b>.
     */
    public final double maxSpeed;

    /**
     * Maximum distance the projectile may travel before being removed.
     * <p>
     * Measured in blocks.
     */
    public final double maxDistance;


    /**
     * Drag coefficient applied to the projectile velocity each tick.
     * <p>
     * Values:
     * <ul>
     *     <li>{@code 1.0} — no drag</li>
     *     <li>{@code < 1.0} — slows the projectile</li>
     *     <li>{@code > 1.0} — speeds up the projectile</li>
     * </ul>
     */
    public final double drag;

    /**
     * Drag coefficient applied when the projectile is inside a liquid.
     */
    public final double dragInWater;

    /**
     * Drag coefficient applied when the world is experiencing rain or snow.
     */
    public final double dragWhenRainingOrSnowing;


    /**
     * Radius used for entity collision detection via ray tracing.
     * <p>
     * When checking for collisions with entities, each entity's bounding box
     * is expanded by this value in all directions.
     * <p>
     * Larger values make it easier for the projectile to hit entities.
     */
    public final double raySize;

    /**
     * Maximum number of ticks the projectile may exist.
     * <p>
     * 20 ticks ≈ 1 second.
     */
    public final int maxAliveTicks;

    /**
     * Whether the projectile should be removed when it reaches the configured minimum speed.
     */
    public final boolean removeWhenMinSpeedReached;

    /**
     * Whether the projectile should be removed when it reaches the configured maximum speed.
     */
    public final boolean removeWhenMaxSpeedReached;

    /**
     * Whether the projectile should perform collision detection with entities.
     * <p>
     * If disabled, the projectile will only collide with blocks.
     */
    public final boolean enableEntityCollisions;


    // -----< Construction >-----

    /**
     * Creates a new {@link ProjectileSettings} instance from a configuration section.
     *
     * <p>
     * During construction:
     * <ul>
     *     <li>configuration values are validated and clamped</li>
     *     <li>units are converted to the internal simulation format</li>
     *     <li>disguise settings are parsed and validated</li>
     * </ul>
     *
     * @param config configuration section containing projectile settings
     *
     * @throws ItemLoadException if the configuration contains invalid values
     */
    private ProjectileSettings(@NotNull ConfigurationSection config) throws ItemLoadException {
        this.disguiseType = loadDisguiseType(config);
        this.disguiseData = loadDisguiseData(config, disguiseType);

        this.muzzleVelocity = Math.clamp(config.getDouble("muzzleVelocity", 150.0D), 1.0D, 1000.0D) / 20.0D; // m/s → blocks/tick
        this.gravity = Math.clamp(config.getDouble("gravity", 0.5D), 0.0D, 100.0D) / 200.0D; // m/s² → blocks/tick²
        this.minSpeed = clampDivideIfUsed(config.getDouble("minSpeed", NOT_USED), 1.0D, 1000.0D, 20.0D); // m/s → blocks/tick
        this.maxSpeed = clampDivideIfUsed(config.getDouble("maxSpeed", NOT_USED), 1.0D, 1000.0D, 20.0D); // m/s → blocks/tick
        this.maxDistance = clampDivideIfUsed(config.getDouble("maxDistance", 80.0D), 1.0D, 300.0D, 1.0D);

        this.drag = Math.clamp(config.getDouble("drag", 1.0D), 0.01D, 10.0D);
        this.dragInWater = Math.clamp(config.getDouble("dragInWater", 0.9D), 0.01D, 10.0D);
        this.dragWhenRainingOrSnowing = Math.clamp(config.getDouble("dragWhenRainingOrSnowing", 0.96D), 0.01D, 10.0D);

        this.raySize = Math.clamp(config.getDouble("raySize", 0.15D), 0.0D, 1.0D);
        this.maxAliveTicks = Math.clamp(config.getInt("maxAliveTicks", 600), 1, 600);
        this.removeWhenMinSpeedReached = config.getBoolean("removeWhenMinSpeedReached", false);
        this.removeWhenMaxSpeedReached = config.getBoolean("removeWhenMaxSpeedReached", false);
        this.enableEntityCollisions = config.getBoolean("enableEntityCollisions", true);
    }

    /**
     * Clamps and converts a configuration value if it is intended for use.
     *
     * @param raw the raw value read from the configuration
     * @param min minimum allowed value for clamping
     * @param max maximum allowed value for clamping
     * @param divisor factor to convert the clamped value into internal units
     *
     * @return clamped and converted value suitable for use in the projectile simulation
     */
    private double clampDivideIfUsed(double raw, double min, double max, double divisor) {
        return (raw == NOT_USED) ? NOT_USED : Math.clamp(raw, min, max) / divisor;
    }
}