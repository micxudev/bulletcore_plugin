package org.dredd.bulletcore.models.weapons.reloading;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.dredd.bulletcore.models.weapons.Weapon;
import org.jetbrains.annotations.NotNull;

/**
 * Default reload handler implementation.
 * <p>
 * This implementation refills all bullets at once into the magazine after the specified reload time.
 *
 * @author dredd
 * @since 1.0.0
 */
public final class DefaultReloadHandler extends ReloadHandler {

    /**
     * Private constructor to prevent instantiation outside this class.
     */
    private DefaultReloadHandler() {}

    /**
     * Singleton instance of the default reload handler.
     */
    public static final DefaultReloadHandler INSTANCE = new DefaultReloadHandler();

    @Override
    public @NotNull String getName() {
        return "default";
    }

    @Override
    @NotNull BukkitRunnable create(@NotNull Player player,
                                   @NotNull Weapon weapon) {
        return new BukkitRunnable() {
            long millisLeft = weapon.reloadTime;

            @Override
            public void run() {
                // make sure the weapon stack didn't change in the meantime
                final ItemStack weaponStack = player.getInventory().getItemInMainHand();
                if (!weapon.isThisWeapon(weaponStack)) {
                    ReloadHandler.cancelReload(player, false);
                    return;
                }

                // the timer is still running
                if (millisLeft > 0) {
                    millisLeft = updateReloadCountdown(player, weapon, weaponStack, millisLeft);
                    return;
                }

                // ammo calculations
                final int bulletCount = weapon.getBulletCount(weaponStack);
                final int bulletsToReload = weapon.maxBullets - bulletCount;
                final int removedCount = weapon.ammo.removeAmmo(player, bulletsToReload);

                // add bullets to the weapon if changed
                final int newBulletCount = bulletCount + removedCount;
                if (bulletCount != newBulletCount)
                    weapon.setBulletCount(weaponStack, newBulletCount);

                ReloadHandler.finishReload(player, weapon, newBulletCount);
            }
        };
    }

    @Override
    public boolean isShootingAllowed(@NotNull Player player) {
        // the default reload implementation does not allow shooting during reload
        return !ReloadHandler.isReloading(player);
    }
}