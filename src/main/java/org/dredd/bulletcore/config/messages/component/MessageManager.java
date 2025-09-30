package org.dredd.bulletcore.config.messages.component;

import org.bukkit.configuration.file.YamlConfiguration;
import org.dredd.bulletcore.BulletCore;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Manages localization messages for {@link ComponentMessage}.
 * <p>
 * Loads messages from the language files or creates a default file if missing.
 *
 * @author dredd
 * @since 1.0.0
 */
public final class MessageManager {

    // ----------< Static >----------
    private static final String LANG_FOLDER_NAME = "lang";
    private static final String DEFAULT_LANG_FILE_NAME = "en-US.yml";
    private static final List<String> LANG_HEADER = List.of("Wiki: <link>");

    private static MessageManager instance;

    static MessageManager instance() {
        return instance;
    }

    public static void load(@NotNull BulletCore plugin) {
        instance = new MessageManager(plugin);
    }

    // ----------< Instance >----------
    private final BulletCore plugin;
    private final Map<Locale, EnumMap<ComponentMessage, String>> messages;

    private MessageManager(@NotNull BulletCore plugin) {
        this.plugin = plugin;

        File langFolder = new File(plugin.getDataFolder(), LANG_FOLDER_NAME);
        boolean isFirstLoading = !(langFolder.exists() && langFolder.isDirectory());

        this.messages = isFirstLoading
            ? initializeDefaults(new File(langFolder, DEFAULT_LANG_FILE_NAME))
            : loadLanguagesFromFolder(langFolder);
    }

    /**
     * Resolves the given message for the first available locale.
     *
     * @param primaryLocale  the preferred locale to check first
     * @param fallbackLocale the locale to use if the primary is missing
     * @param message        the message key
     * @return the localized message, or {@code null} if not found
     */
    @Nullable String resolveMessage(@NotNull Locale primaryLocale,
                                    @NotNull Locale fallbackLocale,
                                    @NotNull ComponentMessage message) {
        var primary = messages.get(primaryLocale);
        var source = primary != null ? primary : messages.get(fallbackLocale);
        return source != null ? source.get(message) : null;
    }

    // -----< First Loading >-----

    /**
     * Creates the default language file on the first startup.
     *
     * @param defaultLangFile file to write
     * @return an empty map, since no messages are loaded yet
     */
    private Map<Locale, EnumMap<ComponentMessage, String>> initializeDefaults(@NotNull File defaultLangFile) {
        try {
            writeDefaultMessages(defaultLangFile);
            plugin.getLogger().info("Created default language file: " + defaultLangFile.getName());
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to create default language file '"
                + defaultLangFile.getName() + "': " + e.getMessage());
        }
        return new HashMap<>();
    }

    /**
     * Writes the default messages to the given file.
     */
    private void writeDefaultMessages(@NotNull File file) throws Exception {
        var config = new YamlConfiguration();
        config.options().setHeader(LANG_HEADER).width(Integer.MAX_VALUE);

        for (var msg : ComponentMessage.values())
            config.set(msg.configKey, msg.defaultMessage);

        config.save(file);
    }

    // -----< Regular Loading >-----

    /**
     * Loads all language files from the given folder.
     */
    private @NotNull Map<Locale, EnumMap<ComponentMessage, String>> loadLanguagesFromFolder(@NotNull File langFolder) {
        Map<Locale, EnumMap<ComponentMessage, String>> result = new HashMap<>();

        File[] files;
        try {
            files = langFolder.listFiles((dir, name) -> name.endsWith(".yml"));
            if (files == null) {
                plugin.getLogger().severe("Failed to list language files in folder: " + langFolder);
                return result;
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Error listing files in " + langFolder + ": " + e.getMessage());
            return result;
        }

        for (File file : files) {
            try {
                YamlConfiguration config = new YamlConfiguration();
                config.load(file);

                String localeKey = file.getName().replace(".yml", "");
                Locale locale = Locale.forLanguageTag(localeKey);
                result.put(locale, loadMessages(config, file.getPath()));
            } catch (Exception e) {
                plugin.getLogger().severe("Skipping invalid language file " + file + ":\n" + e.getMessage());
            }
        }

        plugin.getLogger().info("-Loaded " + result.size() + " language file(s): " + result.keySet());
        return result;
    }

    /**
     * Loads messages from the configuration. Logs missing keys.
     */
    private @NotNull EnumMap<ComponentMessage, String> loadMessages(@NotNull YamlConfiguration config,
                                                                    @NotNull String filePath) {
        EnumMap<ComponentMessage, String> result = new EnumMap<>(ComponentMessage.class);

        for (var msg : ComponentMessage.values()) {
            String value = config.getString(msg.configKey, null);
            if (value == null)
                plugin.getLogger().severe(filePath + " missing message for key '"
                    + msg.configKey + "'; using default.");
            else
                result.put(msg, value);
        }
        return result;
    }
}