package org.dredd.bulletcore.models.weapons.reloading;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.dredd.bulletcore.config.ConfigManager;
import org.dredd.bulletcore.models.weapons.Weapon;
import org.jetbrains.annotations.NotNull;

import static org.dredd.bulletcore.utils.ComponentUtils.WHITE;
import static org.dredd.bulletcore.utils.ComponentUtils.noItalic;

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
            final ConfigManager config = ConfigManager.get();

            @Override
            public void run() {
                if (millisLeft > 0) {
                    double secs = millisLeft / 1000D;
                    if (config.enableHotbarMessages)
                        player.sendActionBar(noItalic("Reloading " + secs + "sec", WHITE));
                    millisLeft -= 100L;
                    return;
                }

                // ammo calculations
                ItemStack weaponItem = player.getInventory().getItemInMainHand();
                int weaponBulletsCount = weapon.getBulletCount(weaponItem);

                int reloadCount = weapon.maxBullets - weaponBulletsCount;
                int removedCount = weapon.ammo.removeAmmo(player, reloadCount);

                // update weapon ammo count
                int newWeaponBulletsCount = weaponBulletsCount + removedCount;
                weapon.setBulletCount(weaponItem, newWeaponBulletsCount);

                if (config.enableHotbarMessages)
                    weapon.sendActionbar(player, newWeaponBulletsCount);

                player.getWorld().playSound(player.getLocation(),
                    Sound.BLOCK_PISTON_CONTRACT /* reload end sound */, 1f, 1.5f
                );

                ReloadHandler.cancelReload(player, true);
            }
        };
    }

    @Override
    public boolean isShootingAllowed(@NotNull Player player) {
        // the default implementation does not allow shooting during reload
        return !isReloading(player);
    }
}