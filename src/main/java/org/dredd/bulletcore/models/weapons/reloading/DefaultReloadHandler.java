package org.dredd.bulletcore.models.weapons.reloading;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.dredd.bulletcore.models.weapons.Weapon;
import org.jetbrains.annotations.NotNull;

/**
 * Default reload handler implementation.
 * <p>This implementation refills all bullets at once into the magazine after the specified reload time.
 *
 * @author dredd
 * @since 1.0.0
 */
public class DefaultReloadHandler extends ReloadHandler {

    /**
     * Singleton instance of the default reload handler.
     */
    static final DefaultReloadHandler INSTANCE = new DefaultReloadHandler();

    /**
     * Private constructor to prevent instantiation.
     */
    private DefaultReloadHandler() {}

    @Override
    @NotNull String getName() {
        return "default";
    }

    @Override
    @NotNull BukkitRunnable create(@NotNull Player player, @NotNull Weapon weapon) {
        return new BukkitRunnable() {
            long millisLeft = weapon.reloadTime;

            @Override
            public void run() {
                if (millisLeft > 0) {
                    millisLeft = showReloadCountdown(player, weapon, millisLeft);
                    return;
                }

                // ammo calculations
                ItemStack weaponItem = player.getInventory().getItemInMainHand();
                int weaponBulletsCount = weapon.getBulletCount(weaponItem);

                int reloadCount = weapon.maxBullets - weaponBulletsCount;
                int removedCount = weapon.ammo.removeAmmo(player, reloadCount);

                // add bullets to the weapon
                int newWeaponBulletsCount = weaponBulletsCount + removedCount;
                weapon.setBulletCount(weaponItem, newWeaponBulletsCount);

                ReloadHandler.finishReload(player, weapon, newWeaponBulletsCount);
            }
        };
    }

    @Override
    public boolean isShootingAllowed(@NotNull Player player) {
        // the default implementation does not allow shooting during reload
        return !isReloading(player);
    }
}