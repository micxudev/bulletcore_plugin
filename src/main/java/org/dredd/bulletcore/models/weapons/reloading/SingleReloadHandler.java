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
            final ConfigManager config = ConfigManager.get();

            @Override
            public void run() {
                if (currentBulletMillisLeft > 0) {
                    double secs = currentBulletMillisLeft / 1000D;
                    player.sendActionBar(noItalic("Reloading " + secs + "sec", WHITE));
                    currentBulletMillisLeft -= 100L;
                    return;
                }

                currentBulletMillisLeft = singleBulletReloadTime; // reset reload time for the next bullet

                if (weapon.ammo.removeAmmo(player, 1) <= 0) {
                    // no ammo found in player's inventory
                    ReloadHandler.cancelReload(player, false);
                    return;
                }

                // update weapon ammo count
                ItemStack weaponItem = player.getInventory().getItemInMainHand();
                int weaponBulletsCount = weapon.getBulletCount(weaponItem);

                int newWeaponBulletsCount = weaponBulletsCount + 1;
                weapon.setBulletCount(weaponItem, newWeaponBulletsCount);

                player.getWorld().playSound(player.getLocation(),
                    Sound.BLOCK_TRIPWIRE_ATTACH /* add bullet sound */, 1f, 1.5f
                );

                if (newWeaponBulletsCount >= weapon.maxBullets) {
                    // this was the last bullet; the weapon is now fully loaded
                    if (config.enableHotbarReload)
                        player.sendActionBar(noItalic("Reloaded " + newWeaponBulletsCount + " / " + weapon.maxBullets, WHITE));

                    player.getWorld().playSound(player.getLocation(),
                        Sound.BLOCK_PISTON_CONTRACT /* reload end sound */, 1f, 1.5f
                    );

                    ReloadHandler.cancelReload(player, true);
                }
            }
        };
    }

    @Override
    public boolean isShootingAllowed(@NotNull Player player) {
        // The single bullet reload implementation allows shooting during reload
        // but should cancel the reload before that
        cancelReload(player, false);
        return true;
    }
}