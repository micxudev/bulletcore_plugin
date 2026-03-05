package org.dredd.bulletcore.models.weapons.shooting.projectile;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Predicate;

import org.bukkit.FluidCollisionMode;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.dredd.bulletcore.config.materials.MaterialsManager;
import org.dredd.bulletcore.models.weapons.Weapon;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ProjectileRayTracer {

    private final World world;
    private final Map<Material, Integer> penetratedBlocks;
    private final @Nullable Predicate<Entity> canHit;
    private final Predicate<Block> canCollide;
    private final double raySize;
    private final boolean enableEntityCollisions;

    public ProjectileRayTracer(@NotNull Weapon weapon,
                               @NotNull Player shooter,
                               @NotNull World world) {
        this.world = world;
        this.penetratedBlocks = new EnumMap<>(Material.class);

        final ProjectileSettings settings = weapon.projectileSettings;
        final boolean enableEntityCollisions = settings.enableEntityCollisions;

        if (enableEntityCollisions) {
            this.canHit = entity -> {
                // return true  == this entity will be hit (bullet stops)
                // return false == the bullet will go through this entity

                if (entity == shooter) return false;

                if (entity.isInvulnerable()) return false;

                if (!(entity instanceof LivingEntity victim)) return false;

                if (victim instanceof ArmorStand) return false;

                if (victim instanceof Player player)
                    return player.getGameMode() != GameMode.SPECTATOR;

                // If the shooter is riding (e.g., horse
                // and this horse has a passenger who is the shooter ->
                // skip hit (bullet will go through))
                return !entity.getPassengers().contains(shooter);
            };
        } else {
            this.canHit = null;
        }

        this.canCollide = block -> {
            // return true  == this block will stop the bullet
            // return false == the bullet will go through this block

            final Material blockType = block.getType();

            if (MaterialsManager.instance().isIgnored(blockType)) return false;

            final int penetrationLimit = weapon.blocksPenetration.getPenetrationLimit(blockType);
            if (penetrationLimit <= 0) return true;

            // Check how many blocks of this material already penetrated
            // by the current pellet incremented by 1 and updated in the map.
            final int newValue = penetratedBlocks.merge(blockType, 1, Integer::sum);
            return newValue > penetrationLimit;
        };

        this.raySize = settings.raySize;
        this.enableEntityCollisions = enableEntityCollisions;
    }

    public @Nullable RayTraceResult cast(@NotNull Location start,
                                         @NotNull Vector direction,
                                         double maxDistance) {
        if (enableEntityCollisions)
            return world.rayTrace(
                start,
                direction,
                maxDistance,
                FluidCollisionMode.ALWAYS, // ALWAYS == water/lava will stop bullets (let canCollide predicate handle it)
                false,                     // false == will collide with all blocks (even GRASS, but not AIR)
                raySize,                   // 0 == precise, > 0 == expands, < 0 == shrinks (hitbox for raycast)
                canHit,
                canCollide
            );

        return world.rayTraceBlocks(
            start,
            direction,
            maxDistance,
            FluidCollisionMode.ALWAYS, // ALWAYS == water/lava will stop bullets (let canCollide predicate handle it)
            false,                     // false == will collide with all blocks (even GRASS, but not AIR)
            canCollide
        );
    }
}