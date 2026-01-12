package org.dredd.bulletcore.models.weapons;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.dredd.bulletcore.config.messages.component.ComponentMessage;
import org.dredd.bulletcore.custom_item_manager.exceptions.ItemLoadException;
import org.dredd.bulletcore.custom_item_manager.registries.CustomItemsRegistry;
import org.dredd.bulletcore.models.CustomBase;
import org.dredd.bulletcore.models.ammo.Ammo;
import org.dredd.bulletcore.models.weapons.damage.WeaponDamage;
import org.dredd.bulletcore.models.weapons.reloading.DefaultReloadHandler;
import org.dredd.bulletcore.models.weapons.reloading.ReloadHandler;
import org.dredd.bulletcore.models.weapons.reloading.ReloadType;
import org.dredd.bulletcore.models.weapons.shooting.ShootingHandler;
import org.dredd.bulletcore.models.weapons.shooting.recoil.WeaponRecoil;
import org.dredd.bulletcore.models.weapons.shooting.spray.WeaponSpray;
import org.dredd.bulletcore.models.weapons.skins.SkinsManager;
import org.dredd.bulletcore.models.weapons.skins.WeaponSkin;
import org.dredd.bulletcore.models.weapons.skins.WeaponSkins;
import org.dredd.bulletcore.utils.ServerUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static org.bukkit.inventory.ItemFlag.HIDE_ADDITIONAL_TOOLTIP;
import static org.bukkit.inventory.ItemFlag.HIDE_UNBREAKABLE;
import static org.bukkit.persistence.PersistentDataType.INTEGER;
import static org.dredd.bulletcore.config.messages.component.ComponentMessage.WEAPON_STATUS;
import static org.dredd.bulletcore.config.messages.translatable.TranslatableMessage.LORE_WEAPON_AMMO;
import static org.dredd.bulletcore.config.messages.translatable.TranslatableMessage.LORE_WEAPON_BULLETS;
import static org.dredd.bulletcore.config.messages.translatable.TranslatableMessage.LORE_WEAPON_DAMAGE;
import static org.dredd.bulletcore.config.messages.translatable.TranslatableMessage.LORE_WEAPON_DISTANCE;
import static org.dredd.bulletcore.utils.FormatterUtils.formatDouble;
import static org.dredd.bulletcore.utils.FormatterUtils.formatDoubles;

/**
 * Represents weapon items.
 *
 * @author dredd
 * @since 1.0.0
 */
public class Weapon extends CustomBase {

    // ----------< Static >----------

    /**
     * Identifier for bullet count on a Weapon ItemStack
     */
    private static final NamespacedKey BULLETS_KEY = new NamespacedKey("bulletcore", "bullets");


    // ----------< Instance >----------

    // -----< Attributes >-----

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
     * The same as {@link #delayBetweenShots}, but converted to ticks.
     */
    public final long ticksDelayBetweenShots;

    /**
     * Stores the timestamp of the last trigger pull per weapon for each player.<br>
     * Used with {@link #delayBetweenShots} to prevent firing again too quickly after pulling the trigger.
     */
    private final Object2LongMap<UUID> lastTriggerPulls;

    /**
     * The maximum number of bullets the weapon can hold in its magazine/chamber.
     */
    public final int maxBullets;

    /**
     * String representation of {@link #maxBullets}
     */
    public final String maxBulletsString;

    /**
     * This number of milliseconds must elapse before the weapon is reloaded.
     */
    public final long reloadTime;

    /**
     * The same as {@link #reloadTime}, but converted to ticks and rounded down to the nearest integer.
     */
    public final int ticksReloadTime;

    /**
     * Whether this weapon has automatic shooting.
     */
    public final boolean isAutomatic;

    /**
     * Defines the temporary knockback resistance applied to the victim
     * while processing this weapon's damage.
     */
    public final double victimKnockbackResistance;

    /**
     * Number of projectiles fired per one ammo unit.
     */
    public final int pelletsPerShot;

    /**
     * Weapon damage values for each body part.
     */
    public final WeaponDamage damage;

    /**
     * Controls weapon recoil.
     */
    public final WeaponRecoil recoil;

    /**
     * Controls weapon spray (aka spread, sway)
     */
    public final WeaponSpray spray;

    /**
     * Manages weapon sounds.
     */
    public final WeaponSounds sounds;

    /**
     * Manages weapon bullet trial particle.
     */
    public final BulletTrailParticle trailParticle;

    /**
     * Stores weapon skins.
     */
    public final WeaponSkins skins;

    // -----< Construction >-----

    /**
     * Loads and validates a weapon item definition from the given config.
     *
     * @param config the YAML configuration source
     * @throws ItemLoadException if validation fails
     */
    public Weapon(@NotNull YamlConfiguration config) throws ItemLoadException {
        super(config);

        final String ammoName = config.getString("ammo", null);
        this.ammo = (ammoName == null) ? null : CustomItemsRegistry.AMMO.getItemOrNull(ammoName);
        if (ammo == null)
            throw new ItemLoadException("Invalid 'ammo' name: " + ammoName);

        final String reloadHandlerName = config.getString("reloadHandler", DefaultReloadHandler.INSTANCE.getName());
        this.reloadHandler = ReloadType.getHandlerOrNull(reloadHandlerName);
        if (reloadHandler == null)
            throw new ItemLoadException("Invalid 'reloadHandler' name: " + reloadHandlerName);

        this.maxDistance = Math.clamp(config.getDouble("maxDistance", 64.0D), 1.0D, 300.0D);
        this.delayBetweenShots = Math.clamp(config.getLong("delayBetweenShots", 500L), 50L, Long.MAX_VALUE);
        this.ticksDelayBetweenShots = Math.max(1L, delayBetweenShots / 50L);
        this.lastTriggerPulls = new Object2LongOpenHashMap<>();
        this.maxBullets = Math.clamp(config.getInt("maxBullets", 10), 1, Integer.MAX_VALUE);
        this.maxBulletsString = Integer.toString(maxBullets);
        this.reloadTime = Math.clamp(config.getLong("reloadTime", 3000L), 100L, Long.MAX_VALUE);
        this.ticksReloadTime = Math.max(2, (int) (reloadTime / 50L));
        this.isAutomatic = config.getBoolean("isAutomatic", false);
        this.victimKnockbackResistance = Math.clamp(config.getDouble("victimKnockbackResistance", 0.0D), 0.0D, 1.0D);
        this.pelletsPerShot = Math.clamp(config.getInt("pelletsPerShot", 1), 1, 20);

        this.damage = WeaponDamage.load(config);
        this.recoil = WeaponRecoil.load(config);
        this.spray = WeaponSpray.load(config);
        this.sounds = WeaponSounds.load(config);
        this.trailParticle = BulletTrailParticle.load(config);
        this.skins = WeaponSkins.load(config, super.customModelData, super.displayName);

        super.lore.add(0, Component.empty()); // Bullets will be here on ItemStack creation
        super.lore.add(1, LORE_WEAPON_DAMAGE.toTranslatable(formatDoubles(damage.head(), damage.body(), damage.legs(), damage.feet())));
        super.lore.add(2, LORE_WEAPON_DISTANCE.toTranslatable(formatDouble(maxDistance)));
        super.lore.add(3, LORE_WEAPON_AMMO.toTranslatable(ammo.displayNameString));
    }

    // -----< Weapon Behavior >-----

    @Override
    protected void applyCustomAttributes(@NotNull ItemStack stack) {
        final ItemMeta meta = stack.getItemMeta();

        // Add only weapon-specific attributes
        meta.setUnbreakable(true);
        meta.addItemFlags(HIDE_UNBREAKABLE, HIDE_ADDITIONAL_TOOLTIP);

        stack.setItemMeta(meta);
        setBulletCount(stack, maxBullets);
    }

    @Override
    public boolean onRMB(@NotNull Player player,
                         @NotNull ItemStack stack) {
        reloadHandler.tryReload(player, this, stack);
        return true;
    }

    @Override
    public boolean onLMB(@NotNull Player player,
                         @NotNull ItemStack stack) {
        ShootingHandler.tryShoot(player, this);
        return true;
    }

    @Override
    public boolean onSwapTo(@NotNull Player player,
                            @NotNull ItemStack stack) {
        ServerUtils.chargeOrDischargeIfCrossbowMeta(stack, player.isSneaking());
        return false;
    }

    @Override
    public boolean onSwapAway(@NotNull Player player,
                              @NotNull ItemStack stack) {
        ReloadHandler.cancelReload(player, false);
        ShootingHandler.cancelAutoShooting(player);
        ServerUtils.dischargeIfCrossbowMeta(stack);
        return false;
    }

    @Override
    public boolean onSwapFromMainToOff(@NotNull Player player,
                                       @NotNull ItemStack currentMainHandItem,
                                       @NotNull ItemStack currentOffHandItem) {
        return true;
    }

    @Override
    public boolean onSwapFromOffToMain(@NotNull Player player,
                                       @NotNull ItemStack currentMainHandItem,
                                       @NotNull ItemStack currentOffHandItem) {
        // Currently, this method is only needed if
        // we want to prevent removing a weapon from the off-hand slot.
        // As for now, we never put a weapon to the off-hand slot.
        // But if we ever do (e.g., we might: place scope, fake weapon)
        // we start returning true.
        return false;
    }

    @Override
    public boolean onDropItem(@NotNull Player player,
                              @NotNull ItemStack stack,
                              boolean isFromGui) {
        if (isFromGui) return false;
        changeSkin(player, stack);
        return true;
    }

    // -----< Weapon-specific Behavior >-----

    /**
     * Change the weapon skin to the next one if the player has any.
     *
     * @param player the player requesting the skin change
     * @param stack  the item stack representing this weapon
     */
    private void changeSkin(@NotNull Player player,
                            @NotNull ItemStack stack) {
        final List<String> playerWeaponSkins = SkinsManager.getPlayerWeaponSkins(player, this);
        if (playerWeaponSkins.isEmpty()) return;

        final ItemMeta meta = stack.getItemMeta();
        final WeaponSkin skin = skins.getNextOrDefault(meta.getCustomModelData(), playerWeaponSkins);
        meta.setCustomModelData(skin.customModelData());
        meta.displayName(skin.displayName());
        stack.setItemMeta(meta);
    }

    // -----< ItemStack | Weapon >-----

    /**
     * Retrieves the current bullet count stored in the given {@link ItemStack}'s metadata.
     *
     * @param stack the stack representing a weapon to retrieve the bullet count from
     * @return the number of bullets currently stored in the weapon stack or
     * {@code 0} if the stack did not store bullet count metadata.
     */
    public int getBulletCount(@NotNull ItemStack stack) {
        final Integer value = stack.getItemMeta().getPersistentDataContainer().get(BULLETS_KEY, INTEGER);
        return value == null ? 0 : Math.max(0, value);
    }

    /**
     * Sets the bullet count for the given {@link ItemStack}, updating both persistent data and lore display.
     *
     * @param stack the weapon stack to modify
     * @param count the number of bullets to set for this weapon stack
     */
    public void setBulletCount(@NotNull ItemStack stack,
                               int count) {
        final ItemMeta meta = stack.getItemMeta();
        meta.getPersistentDataContainer().set(BULLETS_KEY, INTEGER, count);

        final List<Component> lore = meta.lore();
        if (lore != null && !lore.isEmpty()) {
            lore.set(0, LORE_WEAPON_BULLETS.toTranslatable(Integer.toString(count), maxBulletsString));
            meta.lore(lore);
            stack.setItemMeta(meta);
        }
    }

    /**
     * Determines whether the given {@link ItemStack} represents this specific type of weapon.
     *
     * @param stack the stack to check
     * @return {@code true} if the given stack corresponds to this weapon type, {@code false} otherwise.
     */
    public boolean isThisWeapon(@Nullable ItemStack stack) {
        return CustomItemsRegistry.getWeaponOrNull(stack) == this;
    }

    // -----< Player | Weapon >-----

    /**
     * Retrieves the timestamp of the last trigger pull for the given player with this weapon.
     *
     * @param player the player whose last trigger pull time should be retrieved
     * @return the timestamp of the last trigger pull, or {@code 0} if none recorded yet
     */
    public long getLastTriggerPullTime(@NotNull Player player) {
        return lastTriggerPulls.getOrDefault(player.getUniqueId(), 0L);
    }

    /**
     * Updates the timestamp of the last trigger pull for the given player to the current system time.
     *
     * @param player the player whose last trigger pull time should be updated
     */
    public void setLastTriggerPullTime(@NotNull Player player) {
        lastTriggerPulls.put(player.getUniqueId(), System.currentTimeMillis());
    }

    // -----< Utilities >-----

    /**
     * Sends the {@link ComponentMessage#WEAPON_STATUS} message to the specified player.
     *
     * @param player  the player to whom the actionbar message will be sent
     * @param current the current bullet count in the weapon
     */
    public void sendWeaponStatus(@NotNull Player player,
                                 int current) {
        WEAPON_STATUS.sendActionBar(
            player,
            Map.of(
                "displayname", displayNameString,
                "bullets", Integer.toString(current),
                "maxbullets", maxBulletsString,
                "total", Integer.toString(ammo.getAmmoCount(player))
            )
        );
    }
}