package org.dredd.bulletcore.models.weapons.shooting.projectile;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Predicate;

import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.dredd.bulletcore.config.materials.MaterialsManager;
import org.dredd.bulletcore.models.weapons.Weapon;
import org.dredd.bulletcore.models.weapons.shooting.ShootingHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

// Created per WeaponProjectile since
// there is a state in penetratedBlocks
public class ProjectileRayTracer {

    private final Map<Material, Integer> penetratedBlocks;
    private final Predicate<Entity> entityFilter;
    private final Predicate<Block> canCollide;
    private final double raySize;

    public ProjectileRayTracer(@NotNull Weapon weapon,
                               @NotNull Player shooter) {
        this.penetratedBlocks = new EnumMap<>(Material.class);

        // TODO:
        //   1) handle disableEntityCollisions.
        //   2) Maybe return temp self-collision immunity
        //      if (getAliveTicks() < 10 && entity.getEntityId() == shooter.getEntityId())
        //          return true;
        //
        //      // Don't hit shooter's transport.
        //      // If the shooter is riding
        //      // (e.g., horse and this horse has a passenger who is a shooter -> return true == prevent hit)
        //      return entity.getPassengers().contains(shooter);

        // Unchanged since moved from ShootingHandler
        this.entityFilter = entity ->
            entity instanceof LivingEntity victim && !entity.equals(shooter) && !ShootingHandler.skipHit(victim);

        // Unchanged since moved from ShootingHandler
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

        this.raySize = weapon.projectileSettings.raySize;
    }

    // TODO: improve input
    public @Nullable RayTraceResult cast(@NotNull World world,
                                         @NotNull Vector start,
                                         @NotNull Vector end,
                                         @NotNull Vector direction) {
        return world.rayTrace(
            new Location(world, start.getX(), start.getY(), start.getZ()), // BAD
            direction,
            start.distance(end),  // BAD
            FluidCollisionMode.ALWAYS, // ALWAYS == water/lava will stop bullets (let canCollide predicate handle it)
            false,                     // false == will collide with all blocks (even GRASS, but not AIR)
            raySize,                   // 0 == precise, > 0 == expands, < 0 == shrinks (hitbox for raycast)
            entityFilter,
            canCollide
        );
    }
}