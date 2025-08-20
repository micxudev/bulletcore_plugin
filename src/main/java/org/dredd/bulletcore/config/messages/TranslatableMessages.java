package org.dredd.bulletcore.config.messages;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;

import static org.dredd.bulletcore.utils.ComponentUtils.MINI;

/**
 * Represents translatable component messages used primarily on the client side<br>
 * to provide translation in static places (e.g., item's lore)
 * <p>
 * Each enum constant defines:
 * <ul>
 *   <li>A unique {@link #translationKey} used to look up messages in lang files on the client</li>
 *   <li>A MiniMessage-formatted default styles {@link #defStyles}, used if no styles were loaded from the config</li>
 * </ul>
 *
 * <p>Messages and styles can be resolved via {@link TranslatableMessages#of(Object...)} method.
 *
 * @author dredd
 * @since 1.0.0
 */
public enum TranslatableMessages {

    /**
     * Represents bullet count; used on weapon item lore
     */
    LORE_WEAPON_BULLETS(
        "lore_weapon_bullets",
        new LinkedHashMap<>() {{
            put("key", "<!i><white>");
            put("count", "<green>");
            put("max", "<blue>");
        }}
    ),

    /**
     * Represents the damage; used on weapon item lore
     */
    LORE_WEAPON_DAMAGE(
        "lore_weapon_damage",
        new LinkedHashMap<>() {{
            put("key", "<!i><white>");
            put("damage_head", "<red>");
            put("damage_body", "<red>");
            put("damage_legs", "<red>");
            put("damage_feet", "<red>");
        }}
    ),

    /**
     * Represents the distance; used on weapon item lore
     */
    LORE_WEAPON_DISTANCE(
        "lore_weapon_distance",
        new LinkedHashMap<>() {{
            put("key", "<!i><white>");
            put("distance", "<red>");
        }}
    ),

    /**
     * Represents the ammo used by the weapon; used on weapon item lore
     */
    LORE_WEAPON_AMMO(
        "lore_weapon_ammo",
        new LinkedHashMap<>() {{
            put("key", "<!i><white>");
            put("ammo", "<yellow>");
        }}
    ),

    /**
     * Represents ammo count; used on ammo item lore
     */
    LORE_AMMO_COUNT(
        "lore_ammo_count",
        new LinkedHashMap<>() {{
            put("key", "<!i><white>");
            put("count", "<green>");
            put("max", "<blue>");
        }}
    ),

    /**
     * Represents durability (measured in damage that can absorb before an armor piece breaks); used on armor item lore
     */
    LORE_ARMOR_DURABILITY(
        "lore_armor_durability",
        new LinkedHashMap<>() {{
            put("key", "<!i><white>");
            put("current", "<green>");
            put("max", "<blue>");
        }}
    ),

    /**
     * Represents damage reduction from weapons (measured in percent); used on armor item lore
     */
    LORE_ARMOR_DAMAGE_REDUCTION(
        "lore_armor_damage_reduction",
        new LinkedHashMap<>() {{
            put("key", "<!i><white>");
            put("reduction", "<red>");
        }}
    ),

    /**
     * Represents vanilla armor points (measured in points (0-30)); used on armor item lore
     */
    LORE_ARMOR_ARMOR_POINTS(
        "lore_armor_armor_points",
        new LinkedHashMap<>() {{
            put("key", "<!i><white>");
            put("points", "<red>");
        }}
    ),

    /**
     * Represents vanilla toughness points (measured in points (0-20)); used on armor item lore
     */
    LORE_ARMOR_TOUGHNESS_POINTS(
        "lore_armor_toughness_points",
        new LinkedHashMap<>() {{
            put("key", "<!i><white>");
            put("points", "<red>");
        }}
    ),

    /**
     * Represents knockback resistance (measured in percent); used on armor item lore
     */
    LORE_ARMOR_KNOCKBACK_RESISTANCE(
        "lore_armor_knockback_resistance",
        new LinkedHashMap<>() {{
            put("key", "<!i><white>");
            put("resistance", "<red>");
        }}
    ),

    /**
     * Represents knockback resistance from explosions (measured in percent); used on armor item lore
     */
    LORE_ARMOR_EXPLOSION_KNOCKBACK_RESISTANCE(
        "lore_armor_explosion_knockback_resistance",
        new LinkedHashMap<>() {{
            put("key", "<!i><white>");
            put("resistance", "<red>");
        }}
    );

    /**
     * The key used to look up the localized version of the message in language files on the client.
     */
    public final String translationKey;

    /**
     * The default MiniMessage-formatted styles used if no styles were loaded from the config.
     */
    public final LinkedHashMap<String, String> defStyles;

    TranslatableMessages(String translationKey, LinkedHashMap<String, String> defStyles) {
        this.translationKey = translationKey;
        this.defStyles = defStyles;
    }

    /**
     * Returns a {@link Component} representing a translatable message with the given arguments.<br>
     *
     * @param args the arguments to be used in the message.
     * @return translatable {@link Component} with the given arguments.
     */
    public @NotNull Component of(Object... args) {
        int expectedArgs = defStyles.size() - 1;
        if (args.length != expectedArgs) {
            throw new IllegalArgumentException("Invalid number of arguments for " + translationKey +
                ": expected " + expectedArgs + ", but got " + args.length);
        }

        var styles = StylesManager.get().getStyles(translationKey);
        if (styles == null) styles = defStyles;

        Component[] compArgs = new Component[expectedArgs];
        var iterator = styles.values().iterator();
        String keyStyle = iterator.next();

        for (int i = 0; i < expectedArgs; i++) {
            String argStyle = iterator.next();
            compArgs[i] = MINI.deserialize(argStyle).append(Component.text(String.valueOf(args[i])));
        }

        return Component.translatable(
            "bulletcore." + translationKey,
            MINI.deserialize(keyStyle).style(),
            compArgs
        );
    }
}