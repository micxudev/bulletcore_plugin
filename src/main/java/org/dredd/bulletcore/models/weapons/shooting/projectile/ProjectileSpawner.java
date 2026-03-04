package org.dredd.bulletcore.models.weapons.shooting.projectile;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;
import org.dredd.bulletcore.BulletCore;
import org.jetbrains.annotations.NotNull;

/**
 * Handles spawning and ticking all projectiles ({@link AProjectile}).
 *
 * @author dredd
 * @since 1.0.0
 */
public final class ProjectileSpawner implements Runnable {

    // -----< Attributes >-----

    /**
     * All currently active (alive) projectiles.
     */
    private final List<AProjectile> activeProjectiles;

    /**
     * Task for ticking projectiles every server tick.
     */
    private final BukkitTask task;

    // -----< Construction >-----

    public ProjectileSpawner(@NotNull BulletCore plugin) {
        this.activeProjectiles = new ArrayList<>(64);
        this.task = Bukkit.getScheduler().runTaskTimer(plugin, this, 1L, 1L);
    }

    // -----< Execution >-----

    /**
     * Spawns a projectile, performs its first tick immediately, and registers it if it survives.
     */
    public void spawn(@NotNull AProjectile projectile) {
        final boolean shouldRemove = projectile.tick();

        if (shouldRemove) {
            projectile.remove();
            return;
        }

        activeProjectiles.add(projectile);
    }

    /**
     * Ticks all active projectiles.
     * Removes those that request removal.
     */
    @Override
    public void run() {
        final var projectiles = this.activeProjectiles;

        for (int i = 0; i < projectiles.size(); ) {
            final AProjectile projectile = projectiles.get(i);

            final boolean shouldRemove = projectile.tick();

            if (shouldRemove) {
                projectile.remove();
                projectiles.remove(i); // shifts left, do not increment i
                continue;
            }

            i++;
        }
    }

    /**
     * Stops ticking and removes all active projectiles.
     * Should be called on plugin disable.
     */
    public void shutdown() {
        task.cancel();
        activeProjectiles.forEach(AProjectile::remove);
        activeProjectiles.clear();
    }
}