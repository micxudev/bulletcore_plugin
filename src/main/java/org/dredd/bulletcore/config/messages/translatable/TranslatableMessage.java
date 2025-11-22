package org.dredd.bulletcore.config.messages.translatable;

import java.util.List;
import java.util.Locale;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.format.Style;
import org.dredd.bulletcore.utils.ComponentUtils;
import org.jetbrains.annotations.NotNull;

import static org.dredd.bulletcore.config.messages.translatable.TranslatableMessage.Defaults.ConfigStyle;
import static org.dredd.bulletcore.config.messages.translatable.TranslatableMessage.Defaults.ConfigStyles;
import static org.dredd.bulletcore.config.messages.translatable.TranslatableMessage.Defaults.KEY_STYLE;
import static org.dredd.bulletcore.config.messages.translatable.TranslatableMessage.Defaults.STYLE_BLUE;
import static org.dredd.bulletcore.config.messages.translatable.TranslatableMessage.Defaults.STYLE_GREEN;
import static org.dredd.bulletcore.config.messages.translatable.TranslatableMessage.Defaults.STYLE_RED;
import static org.dredd.bulletcore.config.messages.translatable.TranslatableMessage.Defaults.STYLE_YELLOW;

/**
 * Defines client-side translatable messages (e.g., shown on custom item lore).
 * <p>
 * Messages and styles can be resolved via {@link #toTranslatable(String...)}.
 *
 * @author dredd
 * @since 1.0.0
 */
public enum TranslatableMessage {

    // ----------< Enum Fields >----------

    // -----< Weapon >-----

    /**
     * Bullet count shown on weapon lore.
     */
    LORE_WEAPON_BULLETS(new ConfigStyles(
        KEY_STYLE,
        List.of(
            new ConfigStyle("count", STYLE_GREEN),
            new ConfigStyle("max", STYLE_BLUE)
        )
    )),

    /**
     * Weapon damage value shown on weapon lore.
     */
    LORE_WEAPON_DAMAGE(new ConfigStyles(
        KEY_STYLE,
        List.of(
            new ConfigStyle("head", STYLE_RED),
            new ConfigStyle("body", STYLE_RED),
            new ConfigStyle("legs", STYLE_RED),
            new ConfigStyle("feet", STYLE_RED)
        )
    )),

    /**
     * Weapon range shown on weapon lore.
     */
    LORE_WEAPON_DISTANCE(new ConfigStyles(
        KEY_STYLE,
        List.of(new ConfigStyle("distance", STYLE_RED))
    )),

    /**
     * Ammo type used by the weapon shown on weapon lore.
     */
    LORE_WEAPON_AMMO(new ConfigStyles(
        KEY_STYLE,
        List.of(new ConfigStyle("ammo", STYLE_YELLOW))
    )),


    // -----< Ammo >-----

    /**
     * Ammo count shown on ammo lore.
     */
    LORE_AMMO_COUNT(new ConfigStyles(
        KEY_STYLE,
        List.of(
            new ConfigStyle("count", STYLE_GREEN),
            new ConfigStyle("max", STYLE_BLUE)
        )
    )),


    // -----< Armor >-----

    /**
     * Armor durability shown on armor lore.
     */
    LORE_ARMOR_DURABILITY(new ConfigStyles(
        KEY_STYLE,
        List.of(
            new ConfigStyle("current", STYLE_GREEN),
            new ConfigStyle("max", STYLE_BLUE)
        )
    )),

    /**
     * Damage reduction percentage shown on armor lore.
     */
    LORE_ARMOR_DAMAGE_REDUCTION(new ConfigStyles(
        KEY_STYLE,
        List.of(new ConfigStyle("reduction", STYLE_RED))
    )),

    /**
     * Vanilla armor points (0–30) shown on armor lore.
     */
    LORE_ARMOR_ARMOR_POINTS(new ConfigStyles(
        KEY_STYLE,
        List.of(new ConfigStyle("points", STYLE_RED))
    )),

    /**
     * Vanilla toughness points (0–20) shown on armor lore.
     */
    LORE_ARMOR_TOUGHNESS_POINTS(new ConfigStyles(
        KEY_STYLE,
        List.of(new ConfigStyle("points", STYLE_RED))
    )),

    /**
     * Knockback resistance percentage shown on armor lore.
     */
    LORE_ARMOR_KNOCKBACK_RESISTANCE(new ConfigStyles(
        KEY_STYLE,
        List.of(new ConfigStyle("resistance", STYLE_RED))
    )),

    /**
     * Explosion knockback resistance percentage shown on armor lore.
     */
    LORE_ARMOR_EXPLOSION_KNOCKBACK_RESISTANCE(new ConfigStyles(
        KEY_STYLE,
        List.of(new ConfigStyle("resistance", STYLE_RED))
    ));


    // ----------< Instance >----------

    /**
     * The default styles used if styles were not loaded from the config.
     */
    final ConfigStyles defaultStyles;

    /**
     * The config key used to identify this message in the config file.
     */
    final String configKey;

    /**
     * The translation key to identify this message in the language files.
     */
    private final String translationKey;

    TranslatableMessage(@NotNull ConfigStyles defaultStyles) {
        this.defaultStyles = defaultStyles;
        this.configKey = this.name().toLowerCase(Locale.ROOT);
        this.translationKey = "bulletcore." + configKey;
    }

    // -----< Public Translation API >-----

    /**
     * Builds a {@link TranslatableComponent} for this message using the provided arguments.
     * <p>
     * Each argument is converted to a {@link TextComponent} and styled with the
     * corresponding entry from {@link MessageStyles#argumentStyles()}.
     * The translation key is styled with {@link MessageStyles#keyStyle()}.
     *
     * @param args the arguments to insert into the message; the number of arguments
     *             must match the size of {@link MessageStyles#argumentStyles()}
     * @return a styled {@link TranslatableComponent} for this message
     * @throws IllegalArgumentException if the number of provided arguments does not match the expected count
     */
    public @NotNull TranslatableComponent toTranslatable(@NotNull String... args) {
        final MessageStyles styles = StylesManager.instance().stylesFor(this);

        final int expectedArgs = styles.argumentStyles().size();
        if (args.length != expectedArgs)
            throw new IllegalArgumentException(this + " expects " + expectedArgs + " args, got " + args.length);

        final Component[] styledArgs = new Component[expectedArgs];
        for (int i = 0; i < expectedArgs; i++)
            styledArgs[i] = Component.text(args[i], styles.argumentStyles().get(i));

        return Component.translatable(translationKey, styles.keyStyle(), styledArgs);
    }

    /**
     * Holds the styles for a {@link TranslatableMessage}.
     *
     * @param keyStyle       the style applied to the translation key
     * @param argumentStyles the ordered list of styles for each message argument;
     *                       its size must match the number of arguments expected by the message
     */
    record MessageStyles(Style keyStyle, List<Style> argumentStyles) {}


    // ----------< Defaults >----------

    /**
     * Provides default style definitions used when no config overrides are present.
     */
    static final class Defaults {

        /**
         * Wraps the key style and argument styles for a message.
         */
        record ConfigStyles(ConfigStyle key, List<ConfigStyle> arguments) {

            /**
             * Converts {@link ConfigStyles} to {@link MessageStyles}.
             */
            MessageStyles toMessageStyles() {
                return new MessageStyles(
                    key.fallback().parsedStyle(),
                    arguments.stream()
                        .map(s -> s.fallback().parsedStyle())
                        .toList()
                );
            }
        }

        /**
         * Represents a single style with its config key and a fallback.
         */
        record ConfigStyle(String configKey, ResolvedStyle fallback) {}

        /**
         * Default key style (non-italic, white).
         */
        static final ConfigStyle KEY_STYLE = new ConfigStyle("key", new ResolvedStyle("<!i><white>"));

        /**
         * Common fallback styles for arguments.
         */
        static final ResolvedStyle STYLE_RED = new ResolvedStyle("<red>");

        static final ResolvedStyle STYLE_GREEN = new ResolvedStyle("<green>");

        static final ResolvedStyle STYLE_BLUE = new ResolvedStyle("<blue>");

        static final ResolvedStyle STYLE_YELLOW = new ResolvedStyle("<yellow>");

        /**
         * Represents a MiniMessage style parsed from a raw input string.
         * <p>
         * Holds both the raw MiniMessage input string and its parsed {@link Style} representation.
         */
        static final class ResolvedStyle {

            private final String rawInput;

            private final Style parsedStyle;

            ResolvedStyle(String rawInput) {
                this.rawInput = rawInput;
                this.parsedStyle = ComponentUtils.deserialize(rawInput).style();
            }

            String rawInput() {return rawInput;}

            Style parsedStyle() {return parsedStyle;}
        }
    }
}