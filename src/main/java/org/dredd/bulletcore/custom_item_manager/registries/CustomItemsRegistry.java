package org.dredd.bulletcore.custom_item_manager.registries;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.dredd.bulletcore.custom_item_manager.exceptions.ItemRegisterException;
import org.dredd.bulletcore.models.CustomBase;
import org.dredd.bulletcore.models.ammo.Ammo;
import org.dredd.bulletcore.models.armor.Armor;
import org.dredd.bulletcore.models.grenades.Grenade;
import org.dredd.bulletcore.models.weapons.Weapon;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Central access point and utility class for interacting with all custom item registries.
 *
 * <p>This class exposes singleton instances of all item registries and provides unified
 * operations that apply across all item types.
 *
 * <p>All registry instances are eagerly initialized and follow the
 * <a href="https://en.wikipedia.org/wiki/Singleton_pattern">Singleton pattern</a></p>
 *
 * @author dredd
 * @since 1.0.0
 */
public final class CustomItemsRegistry {

    /**
     * Global registry for all custom items, regardless of specific type.
     */
    public static final AllItemRegistry all = AllItemRegistry.getInstance();

    /**
     * Registry for all {@link Ammo} items.
     */
    public static final AmmoRegistry ammo = AmmoRegistry.getInstance();

    /**
     * Registry for all {@link Armor} items.
     */
    public static final ArmorRegistry armor = ArmorRegistry.getInstance();

    /**
     * Registry for all {@link Grenade} items.
     */
    public static final GrenadeRegistry grenade = GrenadeRegistry.getInstance();

    /**
     * Registry for all {@link Weapon} items.
     */
    public static final WeaponRegistry weapon = WeaponRegistry.getInstance();

    /**
     * Private constructor to prevent instantiation.
     */
    private CustomItemsRegistry() {}

    /**
     * Registers a {@link CustomBase} item in the appropriate typed registry
     * and also in the {@code all} registry.
     *
     * @param item the item to register
     * @throws ItemRegisterException if the item is null or has an unknown type
     */
    public static void register(@Nullable CustomBase item) throws ItemRegisterException {
        if (item == null) throw new ItemRegisterException("Item cannot be null");
        switch (item) {
            case Ammo ammoItem -> ammo.register(ammoItem);
            case Armor armorItem -> armor.register(armorItem);
            case Grenade grenadeItem -> grenade.register(grenadeItem);
            case Weapon weaponItem -> weapon.register(weaponItem);
            default -> throw new ItemRegisterException("Unknown custom item type: " + item.getClass().getSimpleName());
        }
        all.register(item);
    }

    /**
     * Clears all registered items from all registries.
     */
    public static void clearAll() {
        all.clearAll();
        ammo.clearAll();
        armor.clearAll();
        grenade.clearAll();
        weapon.clearAll();
    }

    /**
     * Checks whether given custom model data is valid and not already used by any registered item.
     *
     * @param customModelData the custom model data to check
     * @return {@code true} if the custom model data is valid and not used; {@code false} otherwise
     */
    public static boolean canModelDataBeUsed(int customModelData) {
        return customModelData != 0 && customModelData % 100 == 0 && !all.exists(customModelData);
    }

    /**
     * Checks whether a given name is valid and not already used by any registered item.
     *
     * @param name the name to check
     * @return {@code true} if the name is non-null, non-blank, and not used; {@code false} otherwise
     */
    public static boolean canNameBeUsed(@Nullable String name) {
        return name != null && isValidFormat(name) && !all.exists(name);
    }

    /**
     * Checks whether a given input is valid and can be used for custom item names.
     *
     * @param input the input to check
     * @return {@code true} if the input matches the format; {@code false} otherwise
     */
    public static boolean isValidFormat(@NotNull String input) {
        return input.matches("[a-z0-9/._-]+");
    }

    /**
     * Retrieves the base custom model data from the given {@link ItemStack}.
     * <p>
     * The "base" is defined as the largest multiple of 100 less than or equal to the item's
     * custom model data (i.e., {@code customModelData - (customModelData % 100)}).
     * <p>
     * If the stack is {@code null}, lacks item meta, or does not have a custom model data set,
     * this method returns {@code 0}.
     *
     * @param stack the item stack to inspect, may be {@code null}
     * @return the base custom model data if present, or {@code 0} otherwise
     */
    private static int getBaseModelDataOrZero(@Nullable ItemStack stack) {
        if (stack == null || !stack.hasItemMeta()) return 0;
        ItemMeta meta = stack.getItemMeta();
        if (meta == null || !meta.hasCustomModelData()) return 0;
        int customModelData = meta.getCustomModelData();
        return customModelData - (customModelData % 100);
    }

    /**
     * Returns the {@link CustomBase} item associated with the given {@link ItemStack}.
     *
     * @param stack the item stack to check, may be null
     * @return the item if {@code stack} represents a custom item, or {@code null} otherwise
     */
    public static @Nullable CustomBase getItemOrNull(@Nullable ItemStack stack) {
        int modelData = getBaseModelDataOrZero(stack);
        return (modelData == 0) ? null : all.getItemOrNull(modelData);
    }

    /**
     * Checks if the given {@link ItemStack} corresponds to a {@link CustomBase} item.
     *
     * @param stack the item stack to check, may be null
     * @return {@code true} if {@code stack} represents a custom item, {@code false} otherwise
     */
    public static boolean isCustomItem(@Nullable ItemStack stack) {
        return getItemOrNull(stack) != null;
    }

    /**
     * Returns the {@link Ammo} item associated with the given {@link ItemStack}.
     *
     * @param stack the item stack to check, may be null
     * @return the item if {@code stack} represents ammo, or {@code null} otherwise
     */
    public static @Nullable Ammo getAmmoOrNull(@Nullable ItemStack stack) {
        int modelData = getBaseModelDataOrZero(stack);
        return (modelData == 0) ? null : ammo.getItemOrNull(modelData);
    }

    /**
     * Checks if the given {@link ItemStack} corresponds to an {@link Ammo} item.
     *
     * @param stack the item stack to check, may be null
     * @return {@code true} if {@code stack} represents ammo, {@code false} otherwise
     */
    public static boolean isAmmo(@Nullable ItemStack stack) {
        return getAmmoOrNull(stack) != null;
    }

    /**
     * Returns the {@link Armor} item associated with the given {@link ItemStack}.
     *
     * @param stack the item stack to check, may be null
     * @return the item if {@code stack} represents armor, or {@code null} otherwise
     */
    public static @Nullable Armor getArmorOrNull(@Nullable ItemStack stack) {
        int modelData = getBaseModelDataOrZero(stack);
        return (modelData == 0) ? null : armor.getItemOrNull(modelData);
    }

    /**
     * Checks if the given {@link ItemStack} corresponds to an {@link Armor} item.
     *
     * @param stack the item stack to check, may be null
     * @return {@code true} if {@code stack} represents armor, {@code false} otherwise
     */
    public static boolean isArmor(@Nullable ItemStack stack) {
        return getArmorOrNull(stack) != null;
    }

    /**
     * Returns the {@link Grenade} item associated with the given {@link ItemStack}.
     *
     * @param stack the item stack to check, may be null
     * @return the item if {@code stack} represents a grenade, or {@code null} otherwise
     */
    public static @Nullable Grenade getGrenadeOrNull(@Nullable ItemStack stack) {
        int modelData = getBaseModelDataOrZero(stack);
        return (modelData == 0) ? null : grenade.getItemOrNull(modelData);
    }

    /**
     * Checks if the given {@link ItemStack} corresponds to a {@link Grenade} item.
     *
     * @param stack the item stack to check, may be null
     * @return {@code true} if {@code stack} represents a grenade, {@code false} otherwise
     */
    public static boolean isGrenade(@Nullable ItemStack stack) {
        return getGrenadeOrNull(stack) != null;
    }

    /**
     * Returns the {@link Weapon} item associated with the given {@link ItemStack}.
     *
     * @param stack the item stack to check, may be null
     * @return the item if {@code stack} represents a weapon, or {@code null} otherwise
     */
    public static @Nullable Weapon getWeaponOrNull(@Nullable ItemStack stack) {
        int modelData = getBaseModelDataOrZero(stack);
        return (modelData == 0) ? null : weapon.getItemOrNull(modelData);
    }

    /**
     * Checks if the given {@link ItemStack} corresponds to a {@link Weapon} item.
     *
     * @param stack the item stack to check, may be null
     * @return {@code true} if {@code stack} represents a weapon, {@code false} otherwise
     */
    public static boolean isWeapon(@Nullable ItemStack stack) {
        return getWeaponOrNull(stack) != null;
    }
}