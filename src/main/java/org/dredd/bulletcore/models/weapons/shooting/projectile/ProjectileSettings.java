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

public final class ProjectileSettings {

    // ----------< Static >----------

    private static final String INVISIBLE = "INVISIBLE";

    // -----< Loader >-----

    public static @NotNull ProjectileSettings load(@NotNull YamlConfiguration config) throws ItemLoadException {
        final ConfigurationSection section = config.getConfigurationSection("projectile");
        if (section == null) throw new ItemLoadException("Missing 'projectile' section.");
        return new ProjectileSettings(section);
    }

    // -----< Helpers >-----

    private static @Nullable EntityType loadDisguiseType(@NotNull ConfigurationSection section) throws ItemLoadException {
        final String disguiseType = section.getString("disguiseType", INVISIBLE).toUpperCase(Locale.ROOT);
        if (INVISIBLE.equals(disguiseType)) return null;
        try {
            return EntityType.valueOf(disguiseType);
        } catch (IllegalArgumentException e) {
            throw new ItemLoadException("Expected valid EntityType in 'disguiseType', but got: '" + disguiseType + "'");
        }
    }

    private static @Nullable Object loadDisguiseData(@NotNull ConfigurationSection section,
                                                     @Nullable EntityType type) throws ItemLoadException {
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

    public final @Nullable EntityType disguiseType;
    public final @Nullable Object disguiseData;

    public final double gravity;
    public final double minSpeed;
    public final double maxSpeed;
    public final double maxDistance;

    public final double decrease;
    public final double decreaseInWater;
    public final double decreaseWhenRainingOrSnowing;

    public final double raySize;
    public final int maxAliveTicks;
    public final boolean removeAtMinSpeed;
    public final boolean removeAtMaxSpeed;
    public final boolean disableEntityCollisions;


    // -----< Construction >-----

    private ProjectileSettings(@NotNull ConfigurationSection config) throws ItemLoadException {
        this.disguiseType = loadDisguiseType(config);
        this.disguiseData = loadDisguiseData(config, disguiseType);

        this.gravity = Math.clamp(config.getDouble("gravity", 10.0D), 0.0D, 100.0D) / 200.0D;
        this.minSpeed = clampDivideIfNotUsed(config.getDouble("minSpeed", NOT_USED), 0.0D, 1000.0D, 20.0D);
        this.maxSpeed = clampDivideIfNotUsed(config.getDouble("maxSpeed", NOT_USED), 0.0D, 1000.0D, 20.0D);
        this.maxDistance = clampDivideIfNotUsed(config.getDouble("maxDistance", 64.0D), 1.0D, 300.0D, 1);

        this.decrease = Math.clamp(config.getDouble("decrease", 0.99D), 0.0D, 3.0D);
        this.decreaseInWater = Math.clamp(config.getDouble("decreaseInWater", 0.96D), 0.0D, 3.0D);
        this.decreaseWhenRainingOrSnowing = Math.clamp(config.getDouble("decreaseWhenRainingOrSnowing", 0.98D), 0.0D, 3.0D);

        this.raySize = Math.clamp(config.getDouble("raySize", 0.1D), 0.0D, 1.0D);
        this.maxAliveTicks = Math.clamp(config.getInt("maxAliveTicks", 600), 1, 600);
        this.removeAtMinSpeed = config.getBoolean("removeAtMinSpeed", false);
        this.removeAtMaxSpeed = config.getBoolean("removeAtMaxSpeed", false);
        this.disableEntityCollisions = config.getBoolean("disableEntityCollisions", false);
    }

    private double clampDivideIfNotUsed(double raw, double min, double max, double divisor) {
        return (raw == NOT_USED) ? NOT_USED : Math.clamp(raw, min, max) / divisor;
    }
}