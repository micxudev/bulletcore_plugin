package org.dredd.bulletcore.models.weapons;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CrossbowMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.dredd.bulletcore.config.messages.ComponentMessage;
import org.dredd.bulletcore.models.CustomBase;
import org.dredd.bulletcore.models.ammo.Ammo;
import org.dredd.bulletcore.models.weapons.damage.WeaponDamage;
import org.dredd.bulletcore.models.weapons.reloading.ReloadHandler;
import org.dredd.bulletcore.models.weapons.shooting.ShootingHandler;
import org.dredd.bulletcore.models.weapons.shooting.recoil.WeaponRecoil;
import org.dredd.bulletcore.models.weapons.shooting.spray.WeaponSpray;
import org.dredd.bulletcore.models.weapons.skins.WeaponSkins;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static org.bukkit.inventory.ItemFlag.HIDE_ADDITIONAL_TOOLTIP;
import static org.bukkit.inventory.ItemFlag.HIDE_UNBREAKABLE;
import static org.bukkit.persistence.PersistentDataType.INTEGER;
import static org.dredd.bulletcore.config.messages.ComponentMessage.WEAPON_ACTIONBAR;
import static org.dredd.bulletcore.config.messages.MessageManager.of;
import static org.dredd.bulletcore.config.messages.TranslatableMessages.LORE_WEAPON_BULLETS;

/**
 * Represents weapon items.
 *
 * @author dredd
 * @since 1.0.0
 */
public class Weapon extends CustomBase {

    /**
     * Identifier for bullet count on a Weapon ItemStack
     */
    private static final NamespacedKey BULLETS_KEY = new NamespacedKey("bulletcore", "bullets");

    /**
     * The ammo used by this weapon.
     */
    public final Ammo ammo;

    /**
     * Manages weapon reloading behavior.
     */
    public final ReloadHandler reloadHandler;

    /**
     * Maximum distance a bullet can travel before it is discarded.
     */
    public final double maxDistance;

    /**
     * This number of milliseconds must elapse before the next shot is available.
     */
    public final long delayBetweenShots;

    /**
     * Stores per-weapon last shot timestamps for each player.<br>
     * Used with {@link #delayBetweenShots} to prevent multiple shots in a short period of time.
     */
    private final Map<UUID, Long> lastShots;

    /**
     * The maximum number of bullets the weapon can hold in its magazine/chamber.
     */
    public final int maxBullets;

    /**
     * This number of milliseconds must elapse before the weapon is reloaded.
     */
    public final long reloadTime;

    /**
     * Controls whether this weapon has automatic shooting.
     */
    public final boolean isAutomatic;

    /**
     * Defines the temporary knockback resistance applied to the victim
     * while processing this weapon's damage.
     */
    public final double victimKnockbackResistance;

    /**
     * Weapon damage values for each body part.
     */
    public final WeaponDamage damage;

    /**
     * Controls weapon recoil.
     */
    public final WeaponRecoil recoil;

    /**
     * Controls weapon spray/spread/sway
     */
    public final WeaponSpray spray;

    /**
     * Manages weapon sounds.
     */
    public final WeaponSounds sounds;

    /**
     * Stores weapon skins.
     */
    public final WeaponSkins skins;

    /**
     * Constructs a new {@link Weapon} instance.
     * <p>
     * All parameters must be already validated.
     */
    public Weapon(BaseAttributes attrs, Ammo ammo, ReloadHandler reloadHandler, double maxDistance, long delayBetweenShots, int maxBullets, long reloadTime, boolean isAutomatic, double victimKnockbackResistance, WeaponDamage damage, WeaponRecoil recoil, WeaponSpray spray, WeaponSounds sounds, WeaponSkins skins) {
        super(attrs);
        this.ammo = ammo;
        this.reloadHandler = reloadHandler;
        this.maxDistance = maxDistance;
        this.delayBetweenShots = delayBetweenShots;
        this.lastShots = new HashMap<>();
        this.maxBullets = maxBullets;
        this.reloadTime = reloadTime;
        this.isAutomatic = isAutomatic;
        this.victimKnockbackResistance = victimKnockbackResistance;
        this.damage = damage;
        this.recoil = recoil;
        this.spray = spray;
        this.sounds = sounds;
        this.skins = skins;
    }

    /**
     * Triggered when a player attempts to drop a weapon.
     * This method is invoked specifically when the drop action is initiated manually with the drop key (Q).
     *
     * @param player   the player who attempts to drop the weapon
     * @param usedItem the item being dropped, which is this weapon object
     */
    public void onDrop(@NotNull Player player, @NotNull ItemStack usedItem) {
        //System.out.println("Weapon drop attempt on key (Q)");
    }

    @Override
    public @NotNull ItemStack createItemStack() {
        ItemStack stack = createBaseItemStack();
        ItemMeta meta = stack.getItemMeta();

        // Add only weapon-specific attributes
        meta.setUnbreakable(true);
        meta.addItemFlags(HIDE_UNBREAKABLE, HIDE_ADDITIONAL_TOOLTIP);

        stack.setItemMeta(meta);
        setBulletCount(stack, maxBullets);
        return stack;
    }

    @Override
    public boolean onRMB(@NotNull Player player, @NotNull ItemStack usedItem) {
        reloadHandler.tryReload(player, this, usedItem);
        return material == Material.CROSSBOW; // Condition to cancel charging the crossbow
    }

    @Override
    public boolean onLMB(@NotNull Player player, @NotNull ItemStack usedItem) {
        ShootingHandler.tryShoot(player, this);
        return true;
    }

    @Override
    public boolean onSwapTo(@NotNull Player player, @NotNull ItemStack usedItem) {
        if (!player.isSneaking()) return false;
        //System.err.println("1. Player is sneaking.");

        if (usedItem.getItemMeta() instanceof CrossbowMeta meta) {
            //System.err.println("2.2. Player swapped TO Crossbow Weapon. Charge Crossbow.");
            meta.setChargedProjectiles(Collections.singletonList(new ItemStack(Material.ARROW)));
            usedItem.setItemMeta(meta);
        }

        return false;
    }

    @Override
    public boolean onSwapAway(@NotNull Player player, @NotNull ItemStack usedItem) {
        ReloadHandler.cancelReload(player, false);
        ShootingHandler.cancelAutoShooting(player);

        if (!player.isSneaking()) return false;
        //System.err.println("1. Player is sneaking.");

        if (usedItem.getItemMeta() instanceof CrossbowMeta meta) {
            //System.err.println("2.1. Player swapped FROM Crossbow Weapon. Discharge Crossbow.");
            meta.setChargedProjectiles(null);
            usedItem.setItemMeta(meta);
        }

        return false;
    }

    /**
     * Retrieves the current bullet count stored in the given {@link ItemStack}'s metadata.
     *
     * @param stack The {@link ItemStack} representing {@link Weapon} to retrieve the bullet count from.
     * @return The number of bullets currently stored in the weapon stack.<br>
     * Returns {@code 0} if the stack did not store bullet count metadata.
     */
    public int getBulletCount(@NotNull ItemStack stack) {
        return stack.getItemMeta().getPersistentDataContainer().getOrDefault(BULLETS_KEY, INTEGER, 0);
    }

    /**
     * Sets the bullet count for the given {@link ItemStack}, updating both persistent data and lore display.
     *
     * @param stack The weapon {@link ItemStack} to modify.
     * @param count The number of bullets to set for this weapon stack.
     */
    public void setBulletCount(@NotNull ItemStack stack, int count) {
        ItemMeta meta = stack.getItemMeta();
        meta.getPersistentDataContainer().set(BULLETS_KEY, INTEGER, count);

        List<Component> lore = meta.lore();
        lore.set(0, LORE_WEAPON_BULLETS.of(count, maxBullets));
        meta.lore(lore);
        stack.setItemMeta(meta);
    }

    /**
     * Sends the {@link ComponentMessage#WEAPON_ACTIONBAR} message to a specified player.
     *
     * @param player  the player to whom the actionbar message will be sent; must not be null
     * @param current the current bullet count in the weapon
     */
    public void sendActionbar(@NotNull Player player, int current) {
        player.sendActionBar(of(player, WEAPON_ACTIONBAR,
            Map.of(
                "displayname", displayNameString,
                "bullets", Integer.toString(current),
                "maxbullets", Integer.toString(maxBullets),
                "total", Integer.toString(ammo.getAmmoCount(player))
            )
        ));
    }

    /**
     * Retrieves the last shot time for the given player for this weapon.
     *
     * @param player the player to retrieve the last shot time for; must not be null
     * @return last shot time; or {@code null} if none recorded yet
     */
    public @Nullable Long getLastShotTime(@NotNull Player player) {
        return lastShots.get(player.getUniqueId());
    }

    /**
     * Saves new last shot time for the given player.
     *
     * @param player the player to save the last shot time for; must not be null
     * @param time   the new last shot time; must not be null
     */
    public void setLastShotTime(@NotNull Player player, long time) {
        lastShots.put(player.getUniqueId(), time);
    }
}