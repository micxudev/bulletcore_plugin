package org.dredd.bulletcore.custom_item_manager.registries;

import org.bukkit.inventory.ItemStack;
import org.dredd.bulletcore.custom_item_manager.MaterialStorage;
import org.dredd.bulletcore.custom_item_manager.exceptions.ItemRegisterException;
import org.dredd.bulletcore.models.CustomBase;
import org.dredd.bulletcore.models.ammo.Ammo;
import org.dredd.bulletcore.models.armor.Armor;
import org.dredd.bulletcore.models.grenades.Grenade;
import org.dredd.bulletcore.models.weapons.Weapon;
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
        MaterialStorage.clearAll();
    }

    /**
     * Checks whether a given name is valid and not already used by any registered item.
     *
     * @param name the name to check
     * @return {@code true} if the name is non-null, non-blank, and not used; {@code false} otherwise
     */
    public static boolean canNameBeUsed(@Nullable String name) {
        return name != null && !name.isBlank() && !all.exists(name);
    }

    /**
     * Returns the {@link CustomBase} item associated with the given {@link ItemStack}.
     * @param stack the item stack to check
     * @return the item if found, or {@code null} if not registered or the item stack is null
     */
    public static @Nullable CustomBase getItemOrNull(@Nullable ItemStack stack) {
        MaterialStorage materialStorage = MaterialStorage.getFromItem(stack);
        return (materialStorage == null) ? null : all.getItemOrNull(materialStorage);
    }
}