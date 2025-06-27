package org.dredd.bulletcore.config.messages;

import org.dredd.bulletcore.BulletCore;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Class for managing style definitions for translatable components.
 *
 * @author dredd
 * @since 1.0.0
 */
public class StylesManager {

    /**
     * The singleton instance of the style manager.
     */
    private static StylesManager instance;

    /**
     * The style definitions.<br>
     * LinkedHashMap preserves insertion order, which is crucial in this case.
     */
    private final Map<String, LinkedHashMap<String, String>> styles;

    /**
     * Constructs a new {@link StylesManager} instance.
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
     * Initializes or reloads the styles.
     */
    public static void reload(BulletCore plugin) {
        instance = new StylesManager(plugin);
    }

    /**
     * Gets the singleton instance of the style manager.
     *
     * @return the singleton instance of the style manager, or {@code null} if called before {@link #reload(BulletCore)}}
     */
    public static StylesManager get() {
        return instance;
    }

    /**
     * Gets the style definitions for a given key.
     *
     * @param key the style key
     * @return the style map, or null if not found
     */
    public @Nullable LinkedHashMap<String, String> getStyles(@NotNull String key) {
        return styles.get(key);
    }
}