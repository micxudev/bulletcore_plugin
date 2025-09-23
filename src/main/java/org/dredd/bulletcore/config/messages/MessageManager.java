package org.dredd.bulletcore.config.messages;

import org.bukkit.configuration.file.YamlConfiguration;
import org.dredd.bulletcore.BulletCore;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.security.CodeSource;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

/**
 * Handles managing and loading YAML language files from the {@code /lang} directory.
 * <p>
 * Language files must be named with IETF BCP 47 locale tags {@link Locale#forLanguageTag(String)}.
 *
 * @author dredd
 * @since 1.0.0
 */
public class MessageManager {

    /**
     * The singleton instance of the {@link MessageManager}
     */
    private static MessageManager instance;

    /**
     * Gets the singleton instance of the {@link MessageManager}
     *
     * @return the singleton instance, or {@code null} if called before {@link #reload(BulletCore)}
     */
    public static MessageManager get() {
        return instance;
    }

    /**
     * Initializes or reloads all the locale messages.
     *
     * @param plugin the plugin instance
     */
    public static void reload(@NotNull BulletCore plugin) {
        instance = new MessageManager(plugin);
    }

    private final BulletCore plugin;
    private final Map<Locale, EnumMap<ComponentMessage, String>> messages;

    /**
     * Constructs a new {@code MessageManager} and initializes required fields.
     * Language files are not loaded here; use {@link #reload(BulletCore)} to initialize.
     *
     * @param plugin the main plugin instance
     */
    private MessageManager(@NotNull BulletCore plugin) {
        this.plugin = plugin;
        this.messages = loadLocales(new File(plugin.getDataFolder(), "lang"));
    }

    /**
     * Tries to get the locale-specific message for any of the given locales.
     *
     * @param locale1 first locale to check
     * @param locale2 second locale to check
     * @param key     the message key to resolve
     * @return the locale-specific message, or {@code null} if not found for either locale
     */
    public @Nullable String getMessageForOr(@NotNull Locale locale1,
                                            @NotNull Locale locale2,
                                            @NotNull ComponentMessage key) {
        var forLocale1 = messages.get(locale1);
        var localized = forLocale1 != null ? forLocale1 : messages.get(locale2);
        return localized != null ? localized.get(key) : null;
    }

    /**
     * Loads all locale message files from the {@code /lang} directory and parses them into memory.<br>
     * If the directory does not exist, it is created and default language files are extracted from the plugin JAR.
     */
    private @NotNull Map<Locale, EnumMap<ComponentMessage, String>> loadLocales(File langFolder) {
        if (!langFolder.exists()) {
            if (!langFolder.mkdirs())
                throw new RuntimeException("Failed to create lang folder: " + langFolder.getPath());

            copyDefaultLangFiles();
        }

        Map<Locale, EnumMap<ComponentMessage, String>> result = new HashMap<>();

        File[] files = langFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null) return result;

        for (File file : files) {
            String localeKey = file.getName().replace(".yml", "");
            Locale locale = Locale.forLanguageTag(localeKey);
            result.put(locale, load(file));
        }

        plugin.getLogger().info("-Loaded " + result.size() + " language files: " + result.keySet());
        return result;
    }

    /**
     * Loads all non-section key-value string pairs from a YAML file.
     *
     * @param file the YAML file to load messages from
     * @return a {@code Map<String, String>} containing key-value pairs from the file
     */
    private static @NotNull EnumMap<ComponentMessage, String> load(@NotNull File file) {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        EnumMap<ComponentMessage, String> result = new EnumMap<>(ComponentMessage.class);

        for (ComponentMessage message : ComponentMessage.values()) {
            String path = message.name().toLowerCase(Locale.ROOT);

            String content = config.getString(path, null);
            if (content == null) {
                BulletCore.getInstance().getLogger().severe(
                    "Lang file: " + file.getName() +
                        "; has missing message: " + path +
                        "; falling back to default message."
                );
                result.put(message, message.def);
                continue;
            }

            result.put(message, content);
        }

        return result;
    }

    /**
     * Copies bundled default language files from the plugin JAR into the plugin's data folder
     * if they are not already present.
     */
    private void copyDefaultLangFiles() {
        try {
            CodeSource src = plugin.getClass().getProtectionDomain().getCodeSource();
            if (src == null) return;
            try (JarInputStream jarStream = new JarInputStream(src.getLocation().openStream())) {
                JarEntry entry;
                while ((entry = jarStream.getNextJarEntry()) != null) {
                    String name = entry.getName();
                    if (name.startsWith("lang/") && !entry.isDirectory()) {
                        File outFile = new File(plugin.getDataFolder(), name);
                        if (!outFile.exists())
                            plugin.saveResource(name, false);
                    }
                }
            }
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to copy default lang files: " + e.getMessage());
        }
    }
}