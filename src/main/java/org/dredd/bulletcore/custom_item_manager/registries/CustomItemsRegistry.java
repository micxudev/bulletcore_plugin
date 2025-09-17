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

import java.util.regex.Pattern;

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
     * Valid name pattern for custom item names.
     */
    private static final Pattern VALID_NAME = Pattern.compile("[a-z0-9/._-]+");

    /**
     * Global registry for all custom items, regardless of specific type.
     */
    public static final ItemRegistry<CustomBase> ALL = ItemRegistry.create();

    /**
     * Registry for all {@link Ammo} items.
     */
    public static final ItemRegistry<Ammo> AMMO = ItemRegistry.create();

    /**
     * Registry for all {@link Armor} items.
     */
    public static final ItemRegistry<Armor> ARMOR = ItemRegistry.create();

    /**
     * Registry for all {@link Grenade} items.
     */
    public static final ItemRegistry<Grenade> GRENADE = ItemRegistry.create();

    /**
     * Registry for all {@link Weapon} items.
     */
    public static final ItemRegistry<Weapon> WEAPON = ItemRegistry.create();

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
        ALL.register(item);
        try {
            switch (item) {
                case Ammo ammoItem -> AMMO.register(ammoItem);
                case Armor armorItem -> ARMOR.register(armorItem);
                case Grenade grenadeItem -> GRENADE.register(grenadeItem);
                case Weapon weaponItem -> WEAPON.register(weaponItem);
                default ->
                    throw new ItemRegisterException("Unknown custom item type: " + item.getClass().getSimpleName());
            }
        } catch (ItemRegisterException e) {
            ALL.unregister(item);
            throw e;
        }
    }

    /**
     * Clears all registered items from all registries.
     */
    public static void clearAll() {
        ALL.clearAll();
        AMMO.clearAll();
        ARMOR.clearAll();
        GRENADE.clearAll();
        WEAPON.clearAll();
    }

    /**
     * Checks whether given custom model data is valid and not already used by any registered item.
     *
     * @param customModelData the custom model data to check
     * @return {@code true} if the custom model data is valid and not used; {@code false} otherwise
     */
    public static boolean canModelDataBeUsed(int customModelData) {
        return customModelData > 0 && customModelData % 100 == 0 && !ALL.exists(customModelData);
    }

    /**
     * Checks whether a given name is valid and not already used by any registered item.
     *
     * @param name the name to check
     * @return {@code true} if the name is non-null, non-blank, and not used; {@code false} otherwise
     */
    public static boolean canNameBeUsed(@Nullable String name) {
        return name != null && isValidFormat(name) && !ALL.exists(name);
    }

    /**
     * Checks whether a given input is valid and can be used for custom item names.
     *
     * @param input the input to check
     * @return {@code true} if the input matches the format; {@code false} otherwise
     */
    public static boolean isValidFormat(@NotNull String input) {
        return VALID_NAME.matcher(input).matches();
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
     * @param stack the item stack to inspect
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
     * Retrieves an item from the given registry based on the {@link ItemStack}'s base custom model data.
     *
     * @param registry the registry to query
     * @param stack    the item stack to inspect
     * @param <T>      the type of item stored in the registry
     * @return the matching item if found, or {@code null} otherwise
     */
    private static <T extends CustomBase> @Nullable T getOrNull(@NotNull ItemRegistry<T> registry,
                                                                @Nullable ItemStack stack) {
        int modelData = getBaseModelDataOrZero(stack);
        return (modelData == 0) ? null : registry.getItemOrNull(modelData);
    }

    /**
     * Checks whether the given {@link ItemStack} corresponds to an item in the specified registry.
     *
     * @param registry the registry to query
     * @param stack    the item stack to inspect
     * @param <T>      the type of item stored in the registry
     * @return {@code true} if the stack maps to an item in the registry, {@code false} otherwise
     */
    private static <T extends CustomBase> boolean isType(@NotNull ItemRegistry<T> registry,
                                                         @Nullable ItemStack stack) {
        return getOrNull(registry, stack) != null;
    }

    public static @Nullable CustomBase getItemOrNull(@Nullable ItemStack stack) {
        return getOrNull(ALL, stack);
    }

    public static boolean isCustomItem(@Nullable ItemStack stack) {
        return isType(ALL, stack);
    }

    public static @Nullable Ammo getAmmoOrNull(@Nullable ItemStack stack) {
        return getOrNull(AMMO, stack);
    }

    public static boolean isAmmo(@Nullable ItemStack stack) {
        return isType(AMMO, stack);
    }

    public static @Nullable Armor getArmorOrNull(@Nullable ItemStack stack) {
        return getOrNull(ARMOR, stack);
    }

    public static boolean isArmor(@Nullable ItemStack stack) {
        return isType(ARMOR, stack);
    }

    public static @Nullable Grenade getGrenadeOrNull(@Nullable ItemStack stack) {
        return getOrNull(GRENADE, stack);
    }

    public static boolean isGrenade(@Nullable ItemStack stack) {
        return isType(GRENADE, stack);
    }

    public static @Nullable Weapon getWeaponOrNull(@Nullable ItemStack stack) {
        return getOrNull(WEAPON, stack);
    }

    public static boolean isWeapon(@Nullable ItemStack stack) {
        return isType(WEAPON, stack);
    }
}