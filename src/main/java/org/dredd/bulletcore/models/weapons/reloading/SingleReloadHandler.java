package org.dredd.bulletcore.models.weapons.reloading;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.dredd.bulletcore.models.weapons.Weapon;
import org.jetbrains.annotations.NotNull;

/**
 * Single reload handler implementation.
 * <p>This implementation refills one bullet at a time into the magazine after the specified reload time.
 *
 * @author dredd
 * @since 1.0.0
 */
public class SingleReloadHandler extends ReloadHandler {

    /**
     * Singleton instance of the single reload handler.
     */
    static final SingleReloadHandler INSTANCE = new SingleReloadHandler();

    /**
     * Private constructor to prevent instantiation.
     */
    private SingleReloadHandler() {}

    @Override
    @NotNull String getName() {
        return "single";
    }

    @Override
    @NotNull BukkitRunnable create(@NotNull Player player, @NotNull Weapon weapon) {
        return new BukkitRunnable() {
            final long singleBulletReloadTime = weapon.reloadTime / weapon.maxBullets;
            long currentBulletMillisLeft = singleBulletReloadTime;

            @Override
            public void run() {
                if (currentBulletMillisLeft > 0) {
                    currentBulletMillisLeft = showReloadCountdown(player, weapon, currentBulletMillisLeft);
                    return;
                }

                currentBulletMillisLeft = singleBulletReloadTime; // reset reload time for the next bullet

                ItemStack weaponItem = player.getInventory().getItemInMainHand();
                int weaponBulletsCount = weapon.getBulletCount(weaponItem);

                // try to consume 1 ammo
                if (weapon.ammo.removeAmmo(player, 1) <= 0) {
                    ReloadHandler.finishReload(player, weapon, weaponBulletsCount);
                    return;
                }

                // add 1 bullet to the weapon
                int newWeaponBulletsCount = weaponBulletsCount + 1;
                weapon.setBulletCount(weaponItem, newWeaponBulletsCount);

                weapon.sounds.play(player, weapon.sounds.addBullet);

                // stop reload if (fully_loaded or out_of_ammo)
                if (newWeaponBulletsCount >= weapon.maxBullets || weapon.ammo.getAmmoCount(player) <= 0) {
                    ReloadHandler.finishReload(player, weapon, newWeaponBulletsCount);
                }
            }
        };
    }

    @Override
    public boolean isShootingAllowed(@NotNull Player player) {
        // the single bullet reload implementation allows shooting during reload
        // but should cancel the reload before that
        ReloadHandler.cancelReload(player, false);
        return true;
    }
}