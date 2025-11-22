package org.dredd.bulletcore.config.messages.translatable;

import java.io.File;
import java.util.EnumMap;
import java.util.List;

import net.kyori.adventure.text.format.Style;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.dredd.bulletcore.BulletCore;
import org.dredd.bulletcore.utils.ComponentUtils;
import org.jetbrains.annotations.NotNull;

import static org.dredd.bulletcore.config.messages.translatable.TranslatableMessage.Defaults.ConfigStyle;
import static org.dredd.bulletcore.config.messages.translatable.TranslatableMessage.MessageStyles;

/**
 * Manages style definitions for {@link TranslatableMessage}.
 * <p>
 * Loads styles from the file or falls back to default if the file is missing or invalid.
 *
 * @author dredd
 * @since 1.0.0
 */
public final class StylesManager {

    // ----------< Static >----------

    private static final String STYLES_FILE_NAME = "styles.yml";

    private static final List<String> STYLES_HEADER = List.of("Wiki: <link>");

    private static StylesManager instance;

    static StylesManager instance() {
        return instance;
    }

    public static void load(@NotNull BulletCore plugin) {
        instance = new StylesManager(plugin);
    }


    // ----------< Instance >----------

    // -----< Attributes >-----

    private final BulletCore plugin;

    private final EnumMap<TranslatableMessage, MessageStyles> styles;

    // -----< Construction >-----

    private StylesManager(@NotNull BulletCore plugin) {
        this.plugin = plugin;

        final File stylesFile = new File(plugin.getDataFolder(), STYLES_FILE_NAME);
        final boolean isFirstLoading = !stylesFile.exists();

        this.styles = isFirstLoading
            ? initializeDefaults(stylesFile)
            : loadStylesFromFile(stylesFile);
    }

    // -----< API >-----

    /**
     * Returns the styles for the given message.
     */
    @NotNull MessageStyles stylesFor(@NotNull TranslatableMessage message) {
        return styles.get(message);
    }

    // -----< First Loading >-----

    /**
     * Creates the default styles file on the first startup and returns the default styles.
     */
    private @NotNull EnumMap<TranslatableMessage, MessageStyles> initializeDefaults(@NotNull File stylesFile) {
        try {
            writeDefaultStyles(stylesFile);
            plugin.logInfo("Created default styles file \"" + stylesFile + "\"");
        } catch (Exception e) {
            plugin.logError("Failed to create default styles file \"" + stylesFile + "\": " + e.getMessage());
        }
        return loadDefaultStyles();
    }

    /**
     * Returns default styles for all translatable messages.
     */
    private @NotNull EnumMap<TranslatableMessage, MessageStyles> loadDefaultStyles() {
        final EnumMap<TranslatableMessage, MessageStyles> result = new EnumMap<>(TranslatableMessage.class);
        for (final var msg : TranslatableMessage.values())
            result.put(msg, msg.defaultStyles.toMessageStyles());
        return result;
    }

    /**
     * Writes default styles to the given file.
     */
    private void writeDefaultStyles(@NotNull File file) throws Exception {
        final var config = new YamlConfiguration();
        config.options().setHeader(STYLES_HEADER);

        for (final var msg : TranslatableMessage.values()) {
            final var section = config.createSection(msg.configKey);

            final var key = msg.defaultStyles.key();
            section.set(key.configKey(), key.fallback().rawInput());

            final var args = msg.defaultStyles.arguments();
            for (final var arg : args)
                section.set(arg.configKey(), arg.fallback().rawInput());
        }

        config.save(file);
    }

    // -----< Regular Loading >-----

    /**
     * Loads styles from the given file or falls back to default on failure.
     */
    private @NotNull EnumMap<TranslatableMessage, MessageStyles> loadStylesFromFile(@NotNull File stylesFile) {
        try {
            final var config = new YamlConfiguration();
            config.load(stylesFile);
            return parseStyles(config);
        } catch (Exception e) {
            plugin.logError("Failed to load styles file \"" + stylesFile + "\":\n" + e.getMessage() + "\nusing default styles.");
            return loadDefaultStyles();
        }
    }

    /**
     * Parses styles from the given configuration.
     */
    private @NotNull EnumMap<TranslatableMessage, MessageStyles> parseStyles(@NotNull YamlConfiguration config) {
        final EnumMap<TranslatableMessage, MessageStyles> result = new EnumMap<>(TranslatableMessage.class);

        for (final var msg : TranslatableMessage.values()) {
            final var section = config.getConfigurationSection(msg.configKey);
            if (section == null) {
                plugin.logError("Missing styles section for key \"" + msg.configKey + "\"; using default.");
                result.put(msg, msg.defaultStyles.toMessageStyles());
                continue;
            }

            final Style keyStyle = parseStyle(section, msg.defaultStyles.key());
            final List<Style> argStyles = msg.defaultStyles.arguments().stream()
                .map(arg -> parseStyle(section, arg))
                .toList();

            result.put(msg, new MessageStyles(keyStyle, argStyles));
        }
        return result;
    }

    /**
     * Parses a single style from the configuration or falls back to default.
     */
    private @NotNull Style parseStyle(@NotNull ConfigurationSection section,
                                      @NotNull ConfigStyle style) {
        final String value = section.getString(style.configKey(), null);
        if (value == null) {
            plugin.logError("Styles section \"" + section.getCurrentPath() + "\" is missing style for key \"" + style.configKey() + "\"; using default.");
            return style.fallback().parsedStyle();
        }
        return ComponentUtils.deserialize(value).style();
    }
}