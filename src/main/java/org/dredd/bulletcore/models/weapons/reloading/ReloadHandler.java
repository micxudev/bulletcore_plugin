package org.dredd.bulletcore.models.weapons.reloading;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.dredd.bulletcore.BulletCore;
import org.dredd.bulletcore.config.ConfigManager;
import org.dredd.bulletcore.models.weapons.Weapon;
import org.dredd.bulletcore.models.weapons.shooting.ShootingHandler;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.dredd.bulletcore.config.messages.ComponentMessage.WEAPON_RELOAD;
import static org.dredd.bulletcore.config.messages.ComponentMessage.WEAPON_RELOAD_CANCEL;

/**
 * Defines a weapon reload handler interface used to refill ammo/bullets into weapons.
 *
 * @author dredd
 * @since 1.0.0
 */
public abstract class ReloadHandler {

    // -----< Static >-----
    /**
     * Stores currently running reload tasks for each player.
     */
    private static final Map<UUID, BukkitTask> reloadTasks = new HashMap<>();

    /**
     * Checks whether the specified player is currently reloading a weapon.
     *
     * @param player the player to check; must not be {@code null}
     * @return {@code true} if the player is currently reloading, {@code false} otherwise
     */
    public static boolean isReloading(@NotNull Player player) {
        return reloadTasks.containsKey(player.getUniqueId());
    }

    /**
     * Clears all reload tasks. Called when the plugin is reloaded or disabled.
     */
    public static void clearAllReloadTasks() {
        reloadTasks.values().forEach(BukkitTask::cancel);
        reloadTasks.clear();
    }

    /**
     * Cancels the reload task for the specified player.
     *
     * @param player  the player whose reload task should be canceled; must not be {@code null}
     * @param success whether the reload was successful or not; {@code true} if successful, {@code false} otherwise
     */
    public static void cancelReload(@NotNull Player player, boolean success) {
        BukkitTask task = reloadTasks.remove(player.getUniqueId());
        if (task == null) return;
        task.cancel();
        player.setCooldown(player.getInventory().getItemInMainHand().getType(), 0);

        if (!success && ConfigManager.get().enableHotbarMessages)
            player.sendActionBar(WEAPON_RELOAD_CANCEL.toComponent(player, null));
    }

    /**
     * Completes the reload process for the specified player and weapon.
     *
     * @param player        the player reloading the weapon; must not be null
     * @param weapon        the weapon being reloaded; must not be null
     * @param loadedBullets the new number of bullets loaded in the weapon
     */
    static void finishReload(@NotNull Player player, @NotNull Weapon weapon, int loadedBullets) {
        if (ConfigManager.get().enableHotbarMessages)
            weapon.sendActionbar(player, loadedBullets);

        weapon.sounds.play(player, weapon.sounds.reloadEnd);

        ReloadHandler.cancelReload(player, true);
    }

    /**
     * Shows a reload countdown message to the specified player.
     *
     * @param player     the player reloading the weapon; must not be null
     * @param weapon     the weapon being reloaded; must not be null
     * @param weaponItem the weapon stack; must not be null
     * @param millisLeft the number of milliseconds remaining in the reload countdown; must be greater than 0
     * @return the number of milliseconds remaining for the next countdown tick
     */
    static long showReloadCountdown(@NotNull Player player, @NotNull Weapon weapon, @NotNull ItemStack weaponItem, long millisLeft) {
        if (ConfigManager.get().enableHotbarMessages)
            player.sendActionBar(WEAPON_RELOAD.toComponent(player,
                Map.of(
                    "bullets", Integer.toString(weapon.getBulletCount(weaponItem)),
                    "maxbullets", Integer.toString(weapon.maxBullets),
                    "total", Integer.toString(weapon.ammo.getAmmoCount(player)),
                    "time", Double.toString(millisLeft / 1000D)
                )
            ));

        return millisLeft - 100L;
    }

    // -----< Non-Static >-----

    /**
     * Called when a player initiates a weapon reload.
     *
     * @param player      the player initiating the reload; never {@code null}
     * @param weapon      the weapon being reloaded; never {@code null}
     * @param weaponStack the weapon {@code ItemStack} at the time reload starts; never {@code null}
     */
    public void tryReload(@NotNull Player player, @NotNull Weapon weapon, @NotNull ItemStack weaponStack) {
        if (isReloading(player)) return;

        if (weapon.isAutomatic) ShootingHandler.cancelAutoShooting(player);
        ConfigManager config = ConfigManager.get();

        // check bullet count on Weapon
        int bulletCount = weapon.getBulletCount(weaponStack);
        if (bulletCount >= weapon.maxBullets) {
            if (config.enableHotbarMessages)
                weapon.sendActionbar(player, bulletCount);
            return;
        }

        // check ammo count in a player's inventory
        int playerAmmoCount = weapon.ammo.getAmmoCount(player);
        if (playerAmmoCount <= 0) {
            if (config.enableHotbarMessages)
                weapon.sendActionbar(player, bulletCount);
            return;
        }

        weapon.sounds.play(player, weapon.sounds.reloadStart);
        long actualReloadTime = weapon.reloadHandler instanceof SingleReloadHandler
            ? (weapon.reloadTime / weapon.maxBullets) * (weapon.maxBullets - bulletCount)
            : weapon.reloadTime;
        player.setCooldown(weapon.material, (int) (actualReloadTime / 50L));

        BukkitRunnable runnable = create(player, weapon);

        // runs the reload task every 2 ticks (~100 ms), starting immediately.
        BukkitTask reloadTask = runnable.runTaskTimer(BulletCore.getInstance(), 0L, 2L);
        reloadTasks.put(player.getUniqueId(), reloadTask);
    }

    // -----< Abstract >-----

    /**
     * The name used to identify this reload handler.
     */
    abstract @NotNull String getName();

    /**
     * Creates a reload task for the specified player and weapon.
     * <p>
     * This method is invoked from {@link ReloadHandler#tryReload(Player, Weapon, ItemStack)} when a reload begins.
     * Implementations should return a {@link BukkitRunnable} that performs the reload logic every {@code 2 ticks}
     * (e.g., visual/audio feedback, ammo transfer).
     *
     * @param player the player initiating the reload; never {@code null}
     * @param weapon the weapon being reloaded; never {@code null}
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
     * @param player the player to check; must not be {@code null}
     * @return {@code true} if the player is allowed to shoot at this time, {@code false} otherwise
     */
    public abstract boolean isShootingAllowed(@NotNull Player player);
}