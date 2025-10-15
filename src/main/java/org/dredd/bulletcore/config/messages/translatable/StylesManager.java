package org.dredd.bulletcore.config.messages.translatable;

import net.kyori.adventure.text.format.Style;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.dredd.bulletcore.BulletCore;
import org.dredd.bulletcore.utils.ComponentUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.EnumMap;
import java.util.List;

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

    private final BulletCore plugin;
    private final EnumMap<TranslatableMessage, MessageStyles> styles;

    private StylesManager(@NotNull BulletCore plugin) {
        this.plugin = plugin;

        File stylesFile = new File(plugin.getDataFolder(), STYLES_FILE_NAME);
        boolean isFirstLoading = !stylesFile.exists();

        this.styles = isFirstLoading
            ? initializeDefaults(stylesFile)
            : loadStylesFromFile(stylesFile);
    }

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
            plugin.getLogger().info("Created default styles file: " + stylesFile.getName());
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to create file " + stylesFile + " : " + e.getMessage());
        }
        return loadDefaultStyles();
    }

    /**
     * Returns default styles for all translatable messages.
     */
    private @NotNull EnumMap<TranslatableMessage, MessageStyles> loadDefaultStyles() {
        EnumMap<TranslatableMessage, MessageStyles> result = new EnumMap<>(TranslatableMessage.class);
        for (var msg : TranslatableMessage.values())
            result.put(msg, msg.defaultStyles.toMessageStyles());
        return result;
    }

    /**
     * Writes default styles to the given file.
     */
    private void writeDefaultStyles(@NotNull File file) throws Exception {
        var config = new YamlConfiguration();
        config.options().setHeader(STYLES_HEADER);

        for (var msg : TranslatableMessage.values()) {
            var section = config.createSection(msg.configKey);

            var key = msg.defaultStyles.key();
            section.set(key.configKey(), key.fallback().rawInput());

            var args = msg.defaultStyles.arguments();
            for (var arg : args)
                section.set(arg.configKey(), arg.fallback().rawInput());
        }

        config.save(file);
    }

    // -----< Regular Loading >-----

    /**
     * Loads styles from the given file or falls back to default on failure.
     */
    private EnumMap<TranslatableMessage, MessageStyles> loadStylesFromFile(@NotNull File stylesFile) {
        try {
            var config = new YamlConfiguration();
            config.load(stylesFile);
            return parseStyles(config);
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to load styles file: " + stylesFile.getName()
                + ":\n" + e.getMessage() + "\nUsing default styles.");
            return loadDefaultStyles();
        }
    }

    /**
     * Parses styles from the given configuration.
     */
    private @NotNull EnumMap<TranslatableMessage, MessageStyles> parseStyles(@NotNull YamlConfiguration config) {
        EnumMap<TranslatableMessage, MessageStyles> result = new EnumMap<>(TranslatableMessage.class);

        for (var msg : TranslatableMessage.values()) {
            var section = config.getConfigurationSection(msg.configKey);
            if (section == null) {
                plugin.getLogger().severe("Missing section for " + msg.configKey + "; using defaults.");
                result.put(msg, msg.defaultStyles.toMessageStyles());
                continue;
            }

            Style keyStyle = parseStyle(section, msg.defaultStyles.key());
            List<Style> argStyles = msg.defaultStyles.arguments().stream()
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
        String value = section.getString(style.configKey(), null);
        if (value == null) {
            plugin.getLogger().severe(section.getCurrentPath() + " missing style for '"
                + style.configKey() + "'; using default.");
            return style.fallback().parsedStyle();
        }
        return ComponentUtils.deserialize(value).style();
    }
}