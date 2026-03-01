package org.dredd.bulletcore.models.weapons.shooting.projectile;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import org.dredd.bulletcore.BulletCore;
import org.dredd.bulletcore.compatibility.entity.FakeEntity;
import org.dredd.bulletcore.compatibility.entity.FakeEntity_1_21_1;
import org.dredd.bulletcore.models.weapons.Weapon;
import org.jetbrains.annotations.NotNull;

/**
 * Handles spawning and ticking all projectiles ({@link AProjectile}).
 *
 * @author dredd
 * @since 1.0.0
 */
public final class ProjectileSpawner implements Runnable {

    // ----------< Static >----------

    private static ProjectileSpawner INSTANCE;

    public static void init(@NotNull BulletCore plugin) {
        if (INSTANCE == null) {
            INSTANCE = new ProjectileSpawner(plugin);
        }
    }

    public static void destroy() {
        if (INSTANCE != null) {
            INSTANCE.stopAndClear();
            INSTANCE = null;
        }
    }

    public static void spawn(@NotNull AProjectile projectile) {
        if (INSTANCE == null)
            throw new IllegalStateException("ProjectileSpawner not initialized");
        INSTANCE.spawn0(projectile);
    }

    // TODO: improve
    public static void createShootSpawn(@NotNull Location startLocation,
                                        @NotNull Vector normalizedDirection,
                                        @NotNull Weapon weapon,
                                        @NotNull Player shooter) {
        // This location can be mutated by FakeEntity (due to offset used for Armorstand)
        final Location perProjectileLocation = startLocation.clone();

        final ProjectileSettings settings = weapon.projectileSettings;
        final EntityType disguiseType = settings.disguiseType;

        // Because of that FakeEntity must be created before WeaponProjectile
        final FakeEntity fakeEntity = (disguiseType != null)
            ? new FakeEntity_1_21_1(disguiseType, perProjectileLocation, settings.disguiseData)
            : null;

        final Vector motion = normalizedDirection.clone().multiply(settings.muzzleVelocity);
        final WeaponProjectile projectile = new WeaponProjectile(weapon, shooter, perProjectileLocation, motion);

        projectile.spawnDisguise(fakeEntity);
        ProjectileSpawner.spawn(projectile);
    }

    // ----------< Instance >----------

    // -----< Attributes >-----

    /**
     * All projectiles that are currently alive (survived the first tick).
     */
    private final List<AProjectile> projectiles;

    /**
     * Runnable task responsible for ticking all the alive projectiles.
     */
    private final BukkitTask task;

    // -----< Construction >-----

    private ProjectileSpawner(@NotNull Plugin plugin) {
        this.projectiles = new ArrayList<>(64);
        this.task = Bukkit.getScheduler().runTaskTimer(plugin, this, 1L, 1L);
    }

    // -----< Execution >-----

    /**
     * Spawns and does the first tick of the given projectile.
     * <p>
     * Removes the projectile if it died after the first tick.<br>
     * Adds the projectile to the list of all alive projectiles if it survived the first tick.
     */
    private void spawn0(@NotNull AProjectile projectile) {
        if (projectile.tick()) {
            // projectile died after first tick, remove it
            projectile.remove();
        } else {
            // projectile survived first tick, add it
            projectiles.add(projectile);
        }
    }

    /**
     * Ticks all alive projectiles and removes them if<br>
     * {@link AProjectile#tick()} returns true meaning the projectile should die.
     */
    @Override
    public void run() {
        final var projectiles = this.projectiles;
        for (int i = 0; i < projectiles.size(); ) {
            final AProjectile projectile = projectiles.get(i);
            if (projectile.tick()) {
                projectile.remove();
                projectiles.remove(i); // shifts left
            } else {
                i++;
            }
        }
    }

    /**
     * Stops the runnable, despawns all, and clears the list of all alive projectiles.<br>
     * After that no projectiles will be able to tick.<br>
     * Called on plugin disable.
     */
    private void stopAndClear() {
        task.cancel();
        projectiles.forEach(AProjectile::remove);
        projectiles.clear();
    }
}