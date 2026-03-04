package org.dredd.bulletcore.models.weapons.shooting;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import org.dredd.bulletcore.BulletCore;
import org.dredd.bulletcore.config.ConfigManager;
import org.dredd.bulletcore.listeners.trackers.PlayerActionTracker;
import org.dredd.bulletcore.models.weapons.Weapon;
import org.dredd.bulletcore.models.weapons.reloading.ReloadHandler;
import org.dredd.bulletcore.models.weapons.shooting.projectile.AProjectile;
import org.dredd.bulletcore.models.weapons.shooting.projectile.ProjectileFactory;
import org.dredd.bulletcore.models.weapons.shooting.recoil.RecoilHandler;
import org.dredd.bulletcore.models.weapons.shooting.spray.SprayHandler;
import org.jetbrains.annotations.NotNull;

/**
 * Handles weapon shooting (e.g., single, automatic).
 * <p>
 * Is also responsible for checking all the necessary preconditions<br>
 * before calling the actual shooting logic ({@link #shoot(Player, Weapon)})<br>
 * or scheduling an automatic shooting task ({@link #runAutoShootingTask(Player, Weapon, long, long)})
 * <p>
 * Such preconditions include:
 * <ul>
 *   <li>If the reload state allows shooting ({@link ReloadHandler#isShootingAllowed(Player)})</li>
 *   <li>If the weapon is automatic ({@link Weapon#isAutomatic}) (where applicable)</li>
 *   <li>If the shooter is already auto shooting ({@link #isAutoShooting(Player)})</li>
 *   <li>If the required amount of time passed ({@link Weapon#delayBetweenShots}) since the last trigger pull {@link Weapon#getLastTriggerPullTime(Player)}</li>
 * </ul>
 *
 * @author dredd
 * @since 1.0.0
 */
public final class ShootingHandler {

    /**
     * Private constructor to prevent instantiation.
     */
    private ShootingHandler() {}

    /**
     * Stores currently running automatic shooting tasks for each player.
     */
    private static final Map<UUID, BukkitTask> AUTO_SHOOTING_TASKS = new HashMap<>();

    // ----------< Public API >----------

    /**
     * Checks whether the specified player is currently shooting in automatic mode.
     *
     * @param player the player to check
     * @return {@code true} if the player is currently shooting in automatic mode, {@code false} otherwise
     */
    public static boolean isAutoShooting(@NotNull Player player) {
        return AUTO_SHOOTING_TASKS.containsKey(player.getUniqueId());
    }

    /**
     * Cancels the automatic shooting task for the specified player.
     *
     * @param player the player whose shooting task should be canceled
     */
    public static void cancelAutoShooting(@NotNull Player player) {
        final BukkitTask task = AUTO_SHOOTING_TASKS.remove(player.getUniqueId());
        if (task != null) task.cancel();
    }

    /**
     * Clears all shooting tasks. Called when the plugin is reloaded or disabled.
     */
    public static void cancelAllAutoShootingTasks() {
        AUTO_SHOOTING_TASKS.values().forEach(BukkitTask::cancel);
        AUTO_SHOOTING_TASKS.clear();
    }

    /**
     * Attempts to shoot in response to the shooting trigger (LMB).
     *
     * @param player the player who is trying to shoot
     * @param weapon the weapon used
     */
    public static void tryShootOnLMB(@NotNull Player player,
                                     @NotNull Weapon weapon) {
        if (!weapon.reloadHandler.isShootingAllowed(player)) return;
        if (weapon.isAutomatic && isAutoShooting(player)) return;

        final long currentTime = System.currentTimeMillis();
        final long lastShot = weapon.getLastTriggerPullTime(player);
        if (currentTime - lastShot < weapon.delayBetweenShots) return;

        if (weapon.isAutomatic && player.isSneaking()) {
            if (!shoot(player, weapon)) return;

            runAutoShootingTask(player, weapon, weapon.ticksDelayBetweenShots, weapon.ticksDelayBetweenShots);
        } else {
            if (weapon.isAutomatic) PlayerActionTracker.recordSingleShotAutomatic(player.getUniqueId());
            shoot(player, weapon);
        }
    }

    /**
     * Attempts to shoot in response to the SHIFT key being held down
     * within {@link ConfigManager#fireResumeThreshold} ms
     * after the last single shot using an automatic weapon.
     *
     * @param player the player who is trying to shoot
     * @param weapon the weapon used
     */
    public static void tryAutoShootOnToggleSneak(@NotNull Player player,
                                                 @NotNull Weapon weapon) {
        if (!weapon.isAutomatic) return;

        final long now = System.currentTimeMillis();
        final long lastSingleShot = PlayerActionTracker.getLastSingleShotAutomatic(player.getUniqueId());
        final long threshold = ConfigManager.instance().fireResumeThreshold;
        if ((now - lastSingleShot) >= threshold) return;

        if (!weapon.reloadHandler.isShootingAllowed(player)) return;
        if (isAutoShooting(player)) return;

        final long lastShot = weapon.getLastTriggerPullTime(player);
        long ticksUntilShotAvailable = Math.ceilDiv((weapon.delayBetweenShots - (now - lastShot)), 50L);

        if (ticksUntilShotAvailable <= 0L) {
            ticksUntilShotAvailable = weapon.ticksDelayBetweenShots;
            if (!shoot(player, weapon)) return;
        }

        runAutoShootingTask(player, weapon, ticksUntilShotAvailable, weapon.ticksDelayBetweenShots);
    }


    // ----------< Internal API >----------

    /**
     * Runs a new auto shooting task for the specified player.
     *
     * @param player the player to start the task for
     * @param weapon the weapon used
     * @param delay  the ticks to wait before running the task
     * @param period the ticks to wait between runs
     */
    private static void runAutoShootingTask(@NotNull Player player,
                                            @NotNull Weapon weapon,
                                            long delay,
                                            long period) {
        final BukkitTask autoShootingTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!shoot(player, weapon)) cancelAutoShooting(player);
            }
        }.runTaskTimer(BulletCore.instance(), delay, period);

        AUTO_SHOOTING_TASKS.put(player.getUniqueId(), autoShootingTask);
    }

    /**
     * Does a single fire cycle. For most of the weapons this is a single bullet shot.<br>
     * For some weapons (e.g., shotguns), this may be multiple bullet shots.
     *
     * @param player the player who is shooting
     * @param weapon the weapon used
     * @return {@code true} if the shot was successful, {@code false} otherwise.
     */
    private static boolean shoot(@NotNull Player player,
                                 @NotNull Weapon weapon) {
        // always update the last trigger-pull time whenever this method is called,
        // otherwise certain actions may occur more frequently than allowed
        weapon.setLastTriggerPullTime(player);

        // make sure the weapon stack didn't change in the meantime
        final ItemStack weaponStack = player.getInventory().getItemInMainHand();
        if (!weapon.isThisWeapon(weaponStack)) return false;

        final ConfigManager config = ConfigManager.instance();

        // stop if the weapon is empty
        final int bulletCount = weapon.getBulletCount(weaponStack);
        if (bulletCount <= 0) {
            weapon.sounds.play(player, weapon.sounds.empty);
            if (config.enableHotbarMessages)
                weapon.sendWeaponStatus(player, bulletCount);
            return false;
        }

        // update bullet count
        final int newBulletCount = bulletCount - 1;
        weapon.setBulletCount(weaponStack, newBulletCount);
        if (config.enableHotbarMessages)
            weapon.sendWeaponStatus(player, newBulletCount);

        // play fire sound
        weapon.sounds.play(player, weapon.sounds.fire);

        // update recoil
        RecoilHandler.handleShot(player, weapon.recoil);


        // -----< Creating and Shooting Projectile(s) >-----
        final Location eyeLocation = player.getEyeLocation();
        final Vector aimDirection = eyeLocation.getDirection();

        // create and shoot each pellet separately
        final Vector[] directions = SprayHandler.handleShot(player, weapon, aimDirection);
        for (final Vector direction : directions) {
            final AProjectile projectile = ProjectileFactory.create(eyeLocation, direction, weapon, player);
            BulletCore.projectileSpawner().spawn(projectile);
        }

        // push the shooter backwards (opposite to aim direction)
        if (weapon.recoilImpulse > 0.0D) {
            final Vector recoil = aimDirection.clone().multiply(weapon.recoilImpulse);
            final Vector newVelocity = player.getVelocity().subtract(recoil);
            player.setVelocity(newVelocity);
        }

        return true;
    }
}