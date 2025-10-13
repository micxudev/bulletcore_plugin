package org.dredd.bulletcore.models.armor;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.dredd.bulletcore.custom_item_manager.exceptions.ItemLoadException;
import org.dredd.bulletcore.models.CustomBase;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static net.kyori.adventure.text.Component.text;
import static org.bukkit.attribute.Attribute.*;
import static org.bukkit.attribute.AttributeModifier.Operation.ADD_NUMBER;
import static org.bukkit.inventory.EquipmentSlotGroup.ARMOR;
import static org.bukkit.inventory.ItemFlag.*;
import static org.bukkit.persistence.PersistentDataType.DOUBLE;
import static org.dredd.bulletcore.config.messages.translatable.TranslatableMessage.*;
import static org.dredd.bulletcore.utils.ComponentUtils.WHITE;
import static org.dredd.bulletcore.utils.FormatterUtils.formatDouble;
import static org.dredd.bulletcore.utils.FormatterUtils.formatPercent;
import static org.dredd.bulletcore.utils.ServerUtils.rndNamespacedKey;

/**
 * Represents armor items.
 *
 * @author dredd
 * @since 1.0.0
 */
public class Armor extends CustomBase {

    /**
     * Identifier for the custom durability on an Armor ItemStack
     */
    private static final NamespacedKey DURABILITY_KEY = new NamespacedKey("bulletcore", "durability");

    /**
     * The amount of weapon damage the armor piece can absorb before it breaks.
     */
    public final double maxDurability;

    /**
     * The formatted version of {@link #maxDurability}.
     */
    public final String formattedMaxDurability;

    /**
     * Weapon damage reduction percent.
     */
    public final double damageReduction;

    /**
     * Whether the armor piece is unbreakable.
     */
    public final boolean unbreakable;

    /**
     * Attribute modifiers for this armor piece.
     */
    private final Multimap<Attribute, AttributeModifier> modifiers;

    public Armor(@NotNull YamlConfiguration config) throws ItemLoadException {
        super(config);

        this.maxDurability = Math.clamp(config.getDouble("maxDurability", 100.0D), 1.0D, Double.MAX_VALUE);
        this.formattedMaxDurability = formatDouble(maxDurability);

        this.damageReduction = Math.clamp(config.getDouble("damageReduction", 0.5D), 0.0D, 1.0D);

        this.unbreakable = config.getBoolean("unbreakable", true);

        this.modifiers = LinkedListMultimap.create();
        int armorPoints = Math.clamp(config.getInt("armorPoints", 0), 0, 30);
        int toughnessPoints = Math.clamp(config.getInt("toughnessPoints", 0), 0, 20);
        double knockbackResistance = Math.clamp(config.getDouble("knockbackResistance", 0.0D), 0.0D, 1.0D);
        double explosionKnockbackResistance = Math.clamp(config.getDouble("explosionKnockbackResistance", 0.0D), 0.0D, 1.0D);

        if (armorPoints > 0)
            modifiers.put(GENERIC_ARMOR, new AttributeModifier(rndNamespacedKey(), armorPoints, ADD_NUMBER, ARMOR));
        if (toughnessPoints > 0)
            modifiers.put(GENERIC_ARMOR_TOUGHNESS, new AttributeModifier(rndNamespacedKey(), toughnessPoints, ADD_NUMBER, ARMOR));
        if (knockbackResistance > 0)
            modifiers.put(GENERIC_KNOCKBACK_RESISTANCE, new AttributeModifier(rndNamespacedKey(), knockbackResistance, ADD_NUMBER, ARMOR));
        if (explosionKnockbackResistance > 0)
            modifiers.put(GENERIC_EXPLOSION_KNOCKBACK_RESISTANCE, new AttributeModifier(rndNamespacedKey(), explosionKnockbackResistance, ADD_NUMBER, ARMOR));

        super.lore.add(0, text("Durability will be here on ItemStack creation", WHITE));
        super.lore.add(1, LORE_ARMOR_DAMAGE_REDUCTION.toTranslatable(formatPercent(damageReduction)));
        super.lore.add(2, LORE_ARMOR_ARMOR_POINTS.toTranslatable(Integer.toString(armorPoints)));
        super.lore.add(3, LORE_ARMOR_TOUGHNESS_POINTS.toTranslatable(Integer.toString(toughnessPoints)));
        super.lore.add(4, LORE_ARMOR_KNOCKBACK_RESISTANCE.toTranslatable(formatPercent(knockbackResistance)));
        super.lore.add(5, LORE_ARMOR_EXPLOSION_KNOCKBACK_RESISTANCE.toTranslatable(formatPercent(explosionKnockbackResistance)));
    }

    @Override
    public @NotNull ItemStack createItemStack() {
        ItemStack stack = createBaseItemStack();
        ItemMeta meta = stack.getItemMeta();

        meta.setUnbreakable(unbreakable);
        meta.addItemFlags(HIDE_ATTRIBUTES, HIDE_UNBREAKABLE, HIDE_ADDITIONAL_TOOLTIP);
        meta.setAttributeModifiers(modifiers);

        stack.setItemMeta(meta);
        setDurability(stack, maxDurability);
        return stack;
    }

    @Override
    public boolean onRMB(@NotNull Player player, @NotNull ItemStack usedItem) {
        //System.out.println("Right-click with Armor");
        return false;
    }

    @Override
    public boolean onLMB(@NotNull Player player, @NotNull ItemStack usedItem) {
        //System.out.println("Left-click with Armor");
        return false;
    }

    @Override
    public boolean onSwapTo(@NotNull Player player, @NotNull ItemStack usedItem) {
        //System.out.println("Swapped to Armor");
        return false;
    }

    @Override
    public boolean onSwapAway(@NotNull Player player, @NotNull ItemStack usedItem) {
        //System.out.println("Swapped away from Armor");
        return false;
    }

    /**
     * Retrieves the current durability value stored in the given {@link ItemStack}'s metadata.
     *
     * @param stack The {@link ItemStack} representing {@link Armor} to retrieve the durability value from.
     * @return The durability value currently stored in the armor stack.<br>
     * Returns {@code 0.0} if the stack did not store armor durability value metadata.
     */
    public double getDurability(@NotNull ItemStack stack) {
        return stack.getItemMeta().getPersistentDataContainer().getOrDefault(DURABILITY_KEY, DOUBLE, 0.0D);
    }

    /**
     * Sets the custom durability for the given {@link ItemStack}, updating both persistent data and lore display.
     *
     * @param stack      The armor {@link ItemStack} to modify.
     * @param durability The new custom durability to set for this armor stack.
     */
    public void setDurability(@NotNull ItemStack stack, double durability) {
        ItemMeta meta = stack.getItemMeta();
        meta.getPersistentDataContainer().set(DURABILITY_KEY, DOUBLE, durability);

        List<Component> lore = meta.lore();
        if (lore != null && !lore.isEmpty()) {
            lore.set(0, LORE_ARMOR_DURABILITY.toTranslatable(formatDouble(durability), formattedMaxDurability));
            meta.lore(lore);
            stack.setItemMeta(meta);
        }
    }
}