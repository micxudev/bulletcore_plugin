package org.dredd.bulletcore.models.weapons.reloading;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.dredd.bulletcore.models.weapons.Weapon;
import org.jetbrains.annotations.NotNull;

/**
 * Single reload handler implementation.
 * <p>
 * This implementation refills one bullet at a time into the magazine after the specified reload time.
 *
 * @author dredd
 * @since 1.0.0
 */
public final class SingleReloadHandler extends ReloadHandler {

    /**
     * Private constructor to prevent instantiation outside this class.
     */
    private SingleReloadHandler() {}

    /**
     * Singleton instance of the single reload handler.
     */
    static final SingleReloadHandler INSTANCE = new SingleReloadHandler();

    @Override
    @NotNull String getName() {
        return "single";
    }

    @Override
    @NotNull BukkitRunnable create(@NotNull Player player,
                                   @NotNull Weapon weapon) {
        return new BukkitRunnable() {
            final long singleBulletReloadTime = weapon.reloadTime / weapon.maxBullets;

            long currentBulletMillisLeft = singleBulletReloadTime;

            @Override
            public void run() {
                // make sure the weapon stack didn't change in the meantime
                final ItemStack weaponStack = player.getInventory().getItemInMainHand();
                if (!weapon.isThisWeapon(weaponStack)) {
                    ReloadHandler.cancelReload(player, false);
                    return;
                }

                // the timer is still running
                if (currentBulletMillisLeft > 0) {
                    currentBulletMillisLeft = updateReloadCountdown(player, weapon, weaponStack, currentBulletMillisLeft);
                    return;
                }
                currentBulletMillisLeft = singleBulletReloadTime; // reset reload time for the next bullet

                // ammo calculations
                final int bulletCount = weapon.getBulletCount(weaponStack);

                // finish reload if (weapon_fully_loaded or player_out_of_ammo)
                if (bulletCount >= weapon.maxBullets || weapon.ammo.removeAmmo(player, 1) <= 0) {
                    ReloadHandler.finishReload(player, weapon, bulletCount);
                    return;
                }

                // add 1 bullet to the weapon
                final int newBulletCount = bulletCount + 1;
                weapon.setBulletCount(weaponStack, newBulletCount);

                // play an add bullet sound
                weapon.sounds.play(player, weapon.sounds.addBullet);

                // finish reload if (weapon_is_now_fully_loaded or player_is_now_out_of_ammo)
                if (newBulletCount >= weapon.maxBullets || !weapon.ammo.hasAmmo(player)) {
                    ReloadHandler.finishReload(player, weapon, newBulletCount);
                }
            }
        };
    }

    @Override
    public boolean isShootingAllowed(@NotNull Player player) {
        // the single reload implementation allows shooting during reload
        // but must cancel the reload before that
        ReloadHandler.cancelReload(player, false);
        return true;
    }
}