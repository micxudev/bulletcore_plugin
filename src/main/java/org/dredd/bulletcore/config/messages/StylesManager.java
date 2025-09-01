package org.dredd.bulletcore.config.messages;

import org.dredd.bulletcore.BulletCore;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.io.File;
import java.util.Map;

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
    public static void reload(BulletCore plugin) {
        instance = new StylesManager(plugin);
    }

    /**
     * The style definitions. Inner maps preserve insertion order and are unmodifiable.
     */
    private final Map<String, Map<String, String>> styles;

    /**
     * Initializes the {@link StylesManager} instance and loads the styles.
     *
     * @param plugin the {@link BulletCore} instance
     */
    private StylesManager(BulletCore plugin) {
        File stylesFile = new File(plugin.getDataFolder(), "styles.yml");

        if (!stylesFile.exists())
            plugin.saveResource("styles.yml", false);

        this.styles = YMLLoader.loadStyles(stylesFile);
    }

    /**
     * Gets the style definitions for a given key.
     *
     * @param key the style key
     * @return the style map, or null if not found
     */
    public @Nullable @Unmodifiable Map<String, String> getStyles(@NotNull String key) {
        return styles.get(key);
    }
}