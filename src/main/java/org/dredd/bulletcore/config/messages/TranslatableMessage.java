package org.dredd.bulletcore.config.messages;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.format.Style;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Locale;

import static org.dredd.bulletcore.utils.ComponentUtils.*;

/**
 * Enum of translatable client-side messages (e.g., used on item lore).
 * <p>
 * Messages and styles can be resolved via {@link TranslatableMessage#asTranslatable(Object...)}.
 *
 * @author dredd
 * @since 1.0.0
 */
public enum TranslatableMessage {

    /**
     * Represents bullet count; used on weapon item lore
     */
    LORE_WEAPON_BULLETS(List.of(KEY_STYLE, ARG_STYLE_GREEN, ARG_STYLE_BLUE)),

    /**
     * Represents the damage; used on weapon item lore
     */
    LORE_WEAPON_DAMAGE(List.of(KEY_STYLE, ARG_STYLE_RED, ARG_STYLE_RED, ARG_STYLE_RED, ARG_STYLE_RED)),

    /**
     * Represents the distance; used on weapon item lore
     */
    LORE_WEAPON_DISTANCE(List.of(KEY_STYLE, ARG_STYLE_RED)),

    /**
     * Represents the ammo used by the weapon; used on weapon item lore
     */
    LORE_WEAPON_AMMO(List.of(KEY_STYLE, ARG_STYLE_YELLOW)),

    /**
     * Represents ammo count; used on ammo item lore
     */
    LORE_AMMO_COUNT(List.of(KEY_STYLE, ARG_STYLE_GREEN, ARG_STYLE_BLUE)),

    /**
     * Represents durability (measured in damage that can absorb before an armor piece breaks); used on armor item lore
     */
    LORE_ARMOR_DURABILITY(List.of(KEY_STYLE, ARG_STYLE_GREEN, ARG_STYLE_BLUE)),

    /**
     * Represents damage reduction from weapons (measured in percent); used on armor item lore
     */
    LORE_ARMOR_DAMAGE_REDUCTION(List.of(KEY_STYLE, ARG_STYLE_RED)),

    /**
     * Represents vanilla armor points (measured in points (0-30)); used on armor item lore
     */
    LORE_ARMOR_ARMOR_POINTS(List.of(KEY_STYLE, ARG_STYLE_RED)),

    /**
     * Represents vanilla toughness points (measured in points (0-20)); used on armor item lore
     */
    LORE_ARMOR_TOUGHNESS_POINTS(List.of(KEY_STYLE, ARG_STYLE_RED)),

    /**
     * Represents knockback resistance (measured in percent); used on armor item lore
     */
    LORE_ARMOR_KNOCKBACK_RESISTANCE(List.of(KEY_STYLE, ARG_STYLE_RED)),

    /**
     * Represents knockback resistance from explosions (measured in percent); used on armor item lore
     */
    LORE_ARMOR_EXPLOSION_KNOCKBACK_RESISTANCE(List.of(KEY_STYLE, ARG_STYLE_RED));

    /**
     * The default MiniMessage-formatted styles used if no styles were loaded from the config.
     */
    public final List<Component> defStyles;

    TranslatableMessage(List<Component> defStyles) {
        this.defStyles = defStyles;
    }

    /**
     * Returns a {@link Component} representing a translatable message with the given arguments.<br>
     *
     * @param args the arguments to be used in the message.
     * @return translatable {@link Component} with the given arguments.
     */
    public @NotNull TranslatableComponent asTranslatable(@NotNull Object... args) {
        int expectedArgs = defStyles.size() - 1;
        if (args.length != expectedArgs) {
            throw new IllegalArgumentException("Invalid number of arguments for " + this +
                ": expected " + expectedArgs + ", but got " + args.length);
        }

        List<Component> styles = StylesManager.get().getStyles(this);
        var iterator = styles.listIterator();
        Style keyStyle = iterator.next().style();

        Component[] styledArgs = new Component[expectedArgs];
        for (int i = 0; i < expectedArgs; i++)
            styledArgs[i] = iterator.next().append(Component.text(String.valueOf(args[i])));

        return Component.translatable("bulletcore." + this.name().toLowerCase(Locale.ROOT), keyStyle, styledArgs);
    }
}