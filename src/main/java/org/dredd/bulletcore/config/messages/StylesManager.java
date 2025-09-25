package org.dredd.bulletcore.config.messages;

import net.kyori.adventure.text.format.Style;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.dredd.bulletcore.BulletCore;
import org.dredd.bulletcore.utils.ComponentUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Responsible for managing style definitions for {@link TranslatableMessage}.
 *
 * @author dredd
 * @since 1.0.0
 */
public class StylesManager {

    /**
     * The singleton instance of the {@link StylesManager}
     */
    private static StylesManager instance;

    /**
     * Gets the singleton instance of the {@link StylesManager}
     *
     * @return the singleton instance, or {@code null} if called before {@link #reload(BulletCore)}
     */
    public static StylesManager get() {
        return instance;
    }

    /**
     * Initializes or reloads the styles.
     */
    public static void reload(@NotNull BulletCore plugin) {
        instance = new StylesManager(plugin);
    }

    private final EnumMap<TranslatableMessage, StylesBundle> styles;

    /**
     * Initializes the {@link StylesManager} instance and loads the styles.
     *
     * @param plugin the {@link BulletCore} instance
     */
    private StylesManager(@NotNull BulletCore plugin) {
        File stylesFile = new File(plugin.getDataFolder(), "styles.yml");
        if (!stylesFile.exists())
            plugin.saveResource("styles.yml", false);
        this.styles = load(stylesFile);
    }

    /**
     * Gets the {@link StylesBundle} for the given message key.
     *
     * @param key the message key
     * @return corresponding styles bundle
     */
    public @NotNull StylesBundle getStyles(@NotNull TranslatableMessage key) {
        return styles.get(key);
    }

    /**
     * Loads style definitions for all {@link TranslatableMessage} constants from a YAML file.
     * <p>
     * Expects a format where each top-level key corresponds to a message and
     * contains a `key` style and `arg0`, `arg1`, … for each argument.<br>
     * If a message section is missing or invalid, the default styles are used.
     *
     * <pre>
     * translatable_message_key:
     *   key: "&f"
     *   arg0: "&a"
     *   arg1: "&b"
     *   ...
     *   argN: "&c"
     * </pre>
     *
     * @param file the YAML file to load styles from
     * @return an {@link EnumMap} mapping each message to its loaded or default {@link StylesBundle}
     */
    private static @NotNull EnumMap<TranslatableMessage, StylesBundle> load(@NotNull File file) {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        EnumMap<TranslatableMessage, StylesBundle> result = new EnumMap<>(TranslatableMessage.class);

        for (TranslatableMessage message : TranslatableMessage.values()) {
            String path = message.name().toLowerCase(Locale.ROOT);

            ConfigurationSection section = config.getConfigurationSection(path);
            if (section == null) {
                BulletCore.getInstance().getLogger().severe(
                    "Missing styles section for: " + path + "; this message will use default styles."
                );
                result.put(message, message.defStyles);
                continue;
            }

            Set<String> styleKeys = section.getKeys(false);

            int expectedStyleKeys = message.defStyles.argStyles().size() + 1;
            if (styleKeys.size() != expectedStyleKeys) {
                BulletCore.getInstance().getLogger().severe(
                    "Invalid number of styles for: " + path +
                        "; expected " + expectedStyleKeys + ", but got " + styleKeys.size() +
                        "; this message will use default styles."
                );
                result.put(message, message.defStyles);
                continue;
            }

            String key = section.getString("key", null);
            if (key == null) {
                BulletCore.getInstance().getLogger().severe(
                    path + " does not contain style for 'key'; this message will use default styles."
                );
                result.put(message, message.defStyles);
                continue;
            }

            Style keyStyle = ComponentUtils.deserialize(key).style();

            List<Style> argStyles = styleKeys.stream()
                .skip(1)
                .map(styleKey -> ComponentUtils.deserialize(section.getString(styleKey, "")).style())
                .toList();

            StylesBundle stylesBundle = new StylesBundle(keyStyle, argStyles);

            result.put(message, stylesBundle);
        }

        return result;
    }
}