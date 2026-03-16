package org.dredd.bulletcore.models.weapons.shooting.projectile;

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
import org.dredd.bulletcore.config.materials.MaterialCategory;
import org.dredd.bulletcore.config.materials.MaterialsManager;
import org.dredd.bulletcore.models.weapons.Weapon;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Performs ray tracing using Bukkit {@link World#rayTrace}
 * to detect collisions with blocks and entities.
 *
 * @author dredd
 * @since 1.0.0
 */
public final class ProjectileRayTracer {

    // ----------< Static >----------

    /**
     * Total number of materials defined in the server.
     * <p>
     * Used to size the {@link #penetratedBlocks} array, which tracks how many blocks
     * of each material type have already been penetrated by the projectile.
     */
    private static final int TOTAL_MATERIALS = MaterialCategory.AllMaterials.TOTAL_MATERIALS;


    // ----------< Instance >----------

    // -----< Attributes >-----

    /**
     * The world in which the ray tracing is performed.
     */
    private final World world;

    /**
     * Tracks how many blocks of each material type the projectile has already
     * penetrated.
     * <p>
     * The array index corresponds to {@link Material#ordinal()}.
     * <p>
     * Lazily initialized on the first penetration check to avoid unnecessary
     * allocations when block penetration is not used.
     */
    private int[] penetratedBlocks;

    /**
     * Predicate determining whether an entity can be hit by the projectile.
     * <p>
     * If the predicate returns:
     * <ul>
     *     <li>{@code true} — the entity will be considered a valid collision target.</li>
     *     <li>{@code false} — the projectile will ignore the entity and continue.</li>
     * </ul>
     * <p>
     * This predicate may be {@code null} if entity collisions are disabled.
     */
    private final @Nullable Predicate<Entity> canHit;

    /**
     * Predicate determining whether a block stops the projectile.
     * <p>
     * If the predicate returns:
     * <ul>
     *     <li>{@code true} — the block collision stops the projectile.</li>
     *     <li>{@code false} — the projectile passes through the block.</li>
     * </ul>
     * <p>
     * This predicate also implements block penetration logic.
     */
    private final Predicate<Block> canCollide;

    /**
     * Radius used when performing entity collision detection during ray tracing.
     * <p>
     * See {@link ProjectileSettings#raySize}.
     */
    private final double raySize;

    /**
     * Whether entity collision detection is enabled for this projectile.
     */
    private final boolean enableEntityCollisions;

    // -----< Construction >-----

    /**
     * Creates a new ray tracer configured for a specific weapon and shooter.
     *
     * @param weapon  the weapon that spawned the projectile
     * @param shooter the player who fired the projectile
     * @param world   the world where the projectile exists
     */
    public ProjectileRayTracer(@NotNull Weapon weapon,
                               @NotNull Player shooter,
                               @NotNull World world) {
        this.world = world;

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

            // Lazy initialize penetratedBlocks array on first collision check
            int[] penetratedBlocks = this.penetratedBlocks;
            if (penetratedBlocks == null) {
                penetratedBlocks = this.penetratedBlocks = new int[TOTAL_MATERIALS];
            }

            // Check how many blocks of this material already penetrated
            // by the current pellet incremented by 1 and updated in the array.
            final int newValue = ++penetratedBlocks[blockType.ordinal()];

            return newValue > penetrationLimit;
        };

        this.raySize = settings.raySize;
        this.enableEntityCollisions = enableEntityCollisions;
    }

    // -----< Ray Casting >-----

    /**
     * Performs a ray trace to detect the first collision along the projectile's path.
     * <p>
     * The trace starts at the specified position and follows the given direction
     * up to the provided maximum distance.
     * <p>
     * Depending on the projectile configuration, the trace may detect collisions with:
     * <ul>
     *     <li>Blocks only, or</li>
     *     <li>Both blocks and entities.</li>
     * </ul>
     * <p>
     * Block and entity collisions are filtered using the predicates configured
     * during construction.
     *
     * @param start       the starting location of the ray trace
     * @param direction   the direction of the projectile's movement
     * @param maxDistance the maximum distance the ray can travel
     *
     * @return the first detected {@link RayTraceResult}, or {@code null} if no
     * collision occurred within the specified distance
     */
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
            FluidCollisionMode.ALWAYS,
            false,
            canCollide
        );
    }
}