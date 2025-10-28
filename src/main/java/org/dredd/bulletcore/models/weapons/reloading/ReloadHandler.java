package org.dredd.bulletcore.models.weapons.reloading;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.dredd.bulletcore.BulletCore;
import org.dredd.bulletcore.config.ConfigManager;
import org.dredd.bulletcore.models.weapons.Weapon;
import org.dredd.bulletcore.models.weapons.shooting.ShootingHandler;
import org.dredd.bulletcore.utils.FormatterUtils;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.dredd.bulletcore.config.messages.component.ComponentMessage.WEAPON_RELOADING;
import static org.dredd.bulletcore.config.messages.component.ComponentMessage.WEAPON_RELOAD_CANCELED;

/**
 * Defines a weapon reload handler used to refill ammo/bullets into weapons.
 *
 * @author dredd
 * @since 1.0.0
 */
public abstract class ReloadHandler {

    // ----------< Static >----------

    /**
     * Stores currently running reload tasks for each player.
     */
    private static final Map<UUID, BukkitTask> RELOAD_TASKS = new HashMap<>();

    // -----< Public API >-----

    /**
     * Checks whether the specified player is currently reloading a weapon.
     *
     * @param player the player to check
     * @return {@code true} if the player is currently reloading, {@code false} otherwise
     */
    public static boolean isReloading(@NotNull Player player) {
        return RELOAD_TASKS.containsKey(player.getUniqueId());
    }

    /**
     * Cancels the reload task for the specified player.
     *
     * @param player  the player whose reload task should be canceled
     * @param success whether the reload was successful or not
     */
    public static void cancelReload(@NotNull Player player,
                                    boolean success) {
        BukkitTask task = RELOAD_TASKS.remove(player.getUniqueId());
        if (task == null) return;
        task.cancel();
        player.setCooldown(player.getInventory().getItemInMainHand().getType(), 0);

        if (!success && ConfigManager.instance().enableHotbarMessages)
            WEAPON_RELOAD_CANCELED.sendActionBar(player, null);
    }

    /**
     * Clears all reload tasks. Called when the plugin is reloaded or disabled.
     */
    public static void cancelAllReloadTasks() {
        RELOAD_TASKS.values().forEach(BukkitTask::cancel);
        RELOAD_TASKS.clear();
    }

    // -----< Internal API >-----

    /**
     * Completes the reload process for the specified player and weapon.
     *
     * @param player        the player reloading the weapon
     * @param weapon        the weapon being reloaded
     * @param loadedBullets the new number of bullets loaded in the weapon
     */
    static void finishReload(@NotNull Player player,
                             @NotNull Weapon weapon,
                             int loadedBullets) {
        cancelReload(player, true);
        weapon.sounds.play(player, weapon.sounds.reloadEnd);

        if (ConfigManager.instance().enableHotbarMessages)
            weapon.sendWeaponStatus(player, loadedBullets);
    }

    /**
     * Updates a reload countdown and shows a message to the specified player.
     *
     * @param player     the player reloading the weapon
     * @param weapon     the weapon being reloaded
     * @param stack      the weapon stack
     * @param millisLeft the number of milliseconds remaining in the reload countdown
     * @return the number of milliseconds remaining for the next countdown tick
     */
    static long updateReloadCountdown(@NotNull Player player,
                                      @NotNull Weapon weapon,
                                      @NotNull ItemStack stack,
                                      long millisLeft) {
        if (ConfigManager.instance().enableHotbarMessages)
            WEAPON_RELOADING.sendActionBar(player,
                Map.of(
                    "bullets", Integer.toString(weapon.getBulletCount(stack)),
                    "maxbullets", weapon.maxBulletsString,
                    "total", Integer.toString(weapon.ammo.getAmmoCount(player)),
                    "time", FormatterUtils.formatDouble2(millisLeft / 1000D)
                )
            );

        return millisLeft - 100L;
    }


    // ----------< Instance Behavior >----------

    /**
     * Called when a player initiates a weapon reload.
     *
     * @param player      the player initiating the reload
     * @param weapon      the weapon being reloaded
     * @param weaponStack the weapon stack at the time reload starts
     */
    public void tryReload(@NotNull Player player,
                          @NotNull Weapon weapon,
                          @NotNull ItemStack weaponStack) {
        if (isReloading(player)) return;
        if (weapon.isAutomatic) ShootingHandler.cancelAutoShooting(player);

        // stop if (weapon_fully_loaded or player_out_of_ammo)
        int bulletCount = weapon.getBulletCount(weaponStack);
        if (bulletCount >= weapon.maxBullets || !weapon.ammo.hasAmmo(player)) {
            if (ConfigManager.instance().enableHotbarMessages)
                weapon.sendWeaponStatus(player, bulletCount);
            return;
        }

        // play reload start sound
        weapon.sounds.play(player, weapon.sounds.reloadStart);

        // show more accurate reload time for single-reload weapons
        int ticksToReload = weapon.ticksReloadTime;
        if (weapon.reloadHandler instanceof SingleReloadHandler && bulletCount != 0) {
            int bulletsToReload = weapon.maxBullets - bulletCount;
            ticksToReload = (weapon.ticksReloadTime / weapon.maxBullets) * bulletsToReload;
        }
        player.setCooldown(weapon.material, ticksToReload);

        // run the reload task every 2 ticks (~100 ms), starting immediately.
        BukkitTask reloadTask = create(player, weapon).runTaskTimer(BulletCore.instance(), 0L, 2L);
        RELOAD_TASKS.put(player.getUniqueId(), reloadTask);
    }

    // -----< Abstract >-----

    /**
     * The name used to identify this reload handler.
     */
    abstract @NotNull String getName();

    /**
     * Creates a reload task for the specified player and weapon.
     * <p>
     * This method is invoked from {@link #tryReload(Player, Weapon, ItemStack)} when a reload begins.
     * Implementations should return a {@link BukkitRunnable} that performs the reload logic every {@code 2 ticks}
     * (e.g., visual/audio feedback, ammo transfer).
     *
     * @param player the player initiating the reload
     * @param weapon the weapon being reloaded
     * @return a {@link BukkitRunnable} that performs the reload behavior
     */
    abstract @NotNull BukkitRunnable create(@NotNull Player player, @NotNull Weapon weapon);

    /**
     * Determines whether the player is currently allowed to shoot with their weapon.
     * <p>
     * This method is typically used to control whether a shooting action (e.g., firing a weapon)
     * can proceed based on the player's reload state and weapon behavior.
     * <p>
     * Implementations may vary:
     * <ul>
     *   <li>Some weapons (e.g., automatic rifles) may block shooting during reload.</li>
     *   <li>Others (e.g., revolvers) may allow shooting mid-reload, potentially canceling the reload process.</li>
     * </ul>
     *
     * @param player the player to check
     * @return {@code true} if the player is allowed to shoot now, {@code false} otherwise
     */
    public abstract boolean isShootingAllowed(@NotNull Player player);
}