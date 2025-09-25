package org.dredd.bulletcore.config.messages;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Locale;

import static org.dredd.bulletcore.utils.ComponentUtils.*;

/**
 * Defines client-side translatable messages (e.g., shown on custom item lore).
 * <p>
 * Messages and styles can be resolved via {@link #asTranslatable(Object...)}.
 *
 * @author dredd
 * @since 1.0.0
 */
public enum TranslatableMessage {

    /**
     * Bullet count shown on weapon lore.
     */
    LORE_WEAPON_BULLETS(new StylesBundle(KEY_STYLE, List.of(ARG_STYLE_GREEN, ARG_STYLE_BLUE))),

    /**
     * Weapon damage value shown on weapon lore.
     */
    LORE_WEAPON_DAMAGE(new StylesBundle(KEY_STYLE, List.of(ARG_STYLE_RED, ARG_STYLE_RED, ARG_STYLE_RED, ARG_STYLE_RED))),

    /**
     * Weapon range shown on weapon lore.
     */
    LORE_WEAPON_DISTANCE(new StylesBundle(KEY_STYLE, List.of(ARG_STYLE_RED))),

    /**
     * Ammo type used by the weapon shown on weapon lore.
     */
    LORE_WEAPON_AMMO(new StylesBundle(KEY_STYLE, List.of(ARG_STYLE_YELLOW))),

    /**
     * Ammo count shown on ammo lore.
     */
    LORE_AMMO_COUNT(new StylesBundle(KEY_STYLE, List.of(ARG_STYLE_GREEN, ARG_STYLE_BLUE))),

    /**
     * Armor durability shown on armor lore.
     */
    LORE_ARMOR_DURABILITY(new StylesBundle(KEY_STYLE, List.of(ARG_STYLE_GREEN, ARG_STYLE_BLUE))),

    /**
     * Damage reduction percentage shown on armor lore.
     */
    LORE_ARMOR_DAMAGE_REDUCTION(new StylesBundle(KEY_STYLE, List.of(ARG_STYLE_RED))),

    /**
     * Vanilla armor points (0–30) shown on armor lore.
     */
    LORE_ARMOR_ARMOR_POINTS(new StylesBundle(KEY_STYLE, List.of(ARG_STYLE_RED))),

    /**
     * Vanilla toughness points (0–20) shown on armor lore.
     */
    LORE_ARMOR_TOUGHNESS_POINTS(new StylesBundle(KEY_STYLE, List.of(ARG_STYLE_RED))),

    /**
     * Knockback resistance percentage shown on armor lore.
     */
    LORE_ARMOR_KNOCKBACK_RESISTANCE(new StylesBundle(KEY_STYLE, List.of(ARG_STYLE_RED))),

    /**
     * Explosion knockback resistance percentage shown on armor lore.
     */
    LORE_ARMOR_EXPLOSION_KNOCKBACK_RESISTANCE(new StylesBundle(KEY_STYLE, List.of(ARG_STYLE_RED)));

    /**
     * The default styles used if styles were not loaded from the config.
     */
    public final StylesBundle defStyles;

    /**
     * The translation key to identify this message in the language files.
     */
    private final String translationKey;

    TranslatableMessage(@NotNull StylesBundle defStyles) {
        this.defStyles = defStyles;
        this.translationKey = "bulletcore." + this.name().toLowerCase(Locale.ROOT);
    }

    /**
     * Builds a {@link TranslatableComponent} for this message using the provided arguments.
     * <p>
     * Each argument is converted to a {@link TextComponent} and styled with the
     * corresponding entry from {@link StylesBundle#argStyles()}.
     * The translation key is styled with {@link StylesBundle#keyStyle()}.
     *
     * @param args the arguments to insert into the message; the number of arguments
     *             must match the size of {@link StylesBundle#argStyles()}
     * @return a styled {@link TranslatableComponent} for this message
     * @throws IllegalArgumentException if the number of provided arguments does not match the expected count
     */
    public @NotNull TranslatableComponent asTranslatable(@NotNull Object... args) {
        StylesBundle styles = StylesManager.get().getStyles(this);

        int expectedArgs = styles.argStyles().size();
        if (args.length != expectedArgs)
            throw new IllegalArgumentException(this + " expects " + expectedArgs + " args, got " + args.length);

        Component[] styledArgs = new Component[expectedArgs];
        for (int i = 0; i < expectedArgs; i++)
            styledArgs[i] = Component.text(args[i].toString(), styles.argStyles().get(i));

        return Component.translatable(translationKey, styles.keyStyle(), styledArgs);
    }
}