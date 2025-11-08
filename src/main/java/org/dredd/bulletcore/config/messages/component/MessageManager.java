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

    // -----< Attributes >-----

    private final BulletCore plugin;
    private final Map<Locale, EnumMap<ComponentMessage, String>> messages;

    // -----< Construction >-----

    private MessageManager(@NotNull BulletCore plugin) {
        this.plugin = plugin;

        final File langFolder = new File(plugin.getDataFolder(), LANG_FOLDER_NAME);
        final boolean isFirstLoading = !(langFolder.exists() && langFolder.isDirectory());

        this.messages = isFirstLoading
            ? initializeDefaults(new File(langFolder, DEFAULT_LANG_FILE_NAME))
            : loadLanguagesFromFolder(langFolder);
    }

    // -----< API >-----

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
        final var primary = messages.get(primaryLocale);
        final var source = primary != null ? primary : messages.get(fallbackLocale);
        return source != null ? source.get(message) : null;
    }

    // -----< First Loading >-----

    /**
     * Creates the default language file on the first startup.
     *
     * @param defaultLangFile file to write
     * @return an empty map, since no messages are loaded yet
     */
    private @NotNull Map<Locale, EnumMap<ComponentMessage, String>> initializeDefaults(@NotNull File defaultLangFile) {
        try {
            writeDefaultMessages(defaultLangFile);
            plugin.logInfo("Created default language file \"" + defaultLangFile + "\"");
        } catch (Exception e) {
            plugin.logError("Failed to create default language file \"" + defaultLangFile + "\": " + e.getMessage());
        }
        return new HashMap<>();
    }

    /**
     * Writes the default messages to the given file.
     */
    private void writeDefaultMessages(@NotNull File file) throws Exception {
        final var config = new YamlConfiguration();
        config.options().setHeader(LANG_HEADER).width(Integer.MAX_VALUE);

        for (final var msg : ComponentMessage.values())
            config.set(msg.configKey, msg.defaultMessage);

        config.save(file);
    }

    // -----< Regular Loading >-----

    /**
     * Loads all language files from the given folder.
     */
    private @NotNull Map<Locale, EnumMap<ComponentMessage, String>> loadLanguagesFromFolder(@NotNull File langFolder) {
        final Map<Locale, EnumMap<ComponentMessage, String>> result = new HashMap<>();

        final File[] files = langFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null) {
            plugin.logError("Failed to list language files in folder \"" + langFolder + "\"");
            return result;
        }

        for (final File file : files) {
            try {
                final var config = new YamlConfiguration();
                config.load(file);

                final String localeKey = file.getName().replace(".yml", "");
                final Locale locale = Locale.forLanguageTag(localeKey);
                result.put(locale, loadMessages(config, file.toString()));
            } catch (Exception e) {
                plugin.logError("Skipping invalid language file \"" + file + "\":\n" + e.getMessage());
            }
        }

        plugin.logInfo("-Loaded " + result.size() + " language file(s): " + result.keySet());
        return result;
    }

    /**
     * Loads messages from the configuration. Logs missing keys.
     */
    private @NotNull EnumMap<ComponentMessage, String> loadMessages(@NotNull YamlConfiguration config,
                                                                    @NotNull String filePath) {
        final EnumMap<ComponentMessage, String> result = new EnumMap<>(ComponentMessage.class);

        for (final var msg : ComponentMessage.values()) {
            final String value = config.getString(msg.configKey, null);
            if (value == null)
                plugin.logError("Messages file \"" + filePath + "\" is missing message for key \"" + msg.configKey + "\"; using default.");
            else
                result.put(msg, value);
        }
        return result;
    }
}