package org.dredd.bulletcore.models.weapons.reloading;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.dredd.bulletcore.custom_item_manager.registries.CustomItemsRegistry;
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
                // make sure the weapon stack didn't change in the meantime
                ItemStack weaponItem = player.getInventory().getItemInMainHand();
                if (CustomItemsRegistry.getWeaponOrNull(weaponItem) != weapon) {
                    ReloadHandler.cancelReload(player, false);
                    return;
                }

                if (millisLeft > 0) {
                    millisLeft = showReloadCountdown(player, weapon, weaponItem, millisLeft);
                    return;
                }

                // ammo calculations
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