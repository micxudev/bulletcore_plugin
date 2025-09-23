package org.dredd.bulletcore.config.messages;

import net.kyori.adventure.text.Component;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.dredd.bulletcore.BulletCore;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static org.dredd.bulletcore.utils.ComponentUtils.MINI;

/**
 * Class for managing style definitions for translatable client side components.
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

    private final EnumMap<TranslatableMessages, List<Component>> styles;

    /**
     * Initializes the {@link StylesManager} instance and loads the styles.
     *
     * @param plugin the {@link BulletCore} instance
     */
    private StylesManager(BulletCore plugin) {
        File stylesFile = new File(plugin.getDataFolder(), "styles.yml");

        if (!stylesFile.exists())
            plugin.saveResource("styles.yml", false);

        this.styles = load(stylesFile);
    }

    /**
     * Gets the style definitions for a given key.
     *
     * @param key the style key
     * @return the style map, or null if not found
     */
    public @NotNull List<Component> getStyles(@NotNull TranslatableMessages key) {
        return styles.get(key);
    }

    /**
     * Loads structured style definitions from a nested YAML file.<br>
     * Expects a YAML file where each top-level key maps to a section of subkeys and style definitions.
     *
     * <p><strong>Expected YAML format:</strong></p>
     * <pre>
     * translatable_message_key:
     *   key: "&7"
     *   arg0: "&a"
     *   arg1: "&c"
     * </pre>
     *
     * @param file the YAML file to load styles from
     * @return an {@link EnumMap} of {@link TranslatableMessages} constants and their style definitions
     */
    private static @NotNull EnumMap<TranslatableMessages, @NotNull List<Component>> load(@NotNull File file) {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        EnumMap<TranslatableMessages, List<Component>> result = new EnumMap<>(TranslatableMessages.class);

        for (TranslatableMessages message : TranslatableMessages.values()) {
            String path = message.name().toLowerCase(Locale.ROOT);

            ConfigurationSection section = config.getConfigurationSection(path);
            if (section == null) {
                BulletCore.getInstance().getLogger().severe(
                    "Missing styles section for: " + path + "; falling back to default styles."
                );
                result.put(message, message.defStyles);
                continue;
            }

            Set<String> styleKeys = section.getKeys(false);
            if (styleKeys.size() != message.defStyles.size()) {
                BulletCore.getInstance().getLogger().severe(
                    "Invalid number of styles for: " + path +
                        "; expected " + message.defStyles.size() + ", but got " + styleKeys.size() +
                        "; falling back to default styles."
                );
                result.put(message, message.defStyles);
                continue;
            }

            List<Component> styles = styleKeys.stream()
                .map(styleKey -> MINI.deserialize(section.getString(styleKey, "")))
                .toList();

            result.put(message, styles);
        }

        return result;
    }
}