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
public final class MessageManager {

    // ----------< Static >----------
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
        this.messages = loadLocales(new File(plugin.getDataFolder(), "lang"));
    }

    /**
     * Resolves the given message for the first available locale.
     *
     * @param primaryLocale  the preferred locale to check first
     * @param fallbackLocale the secondary locale to check if the primary has no entry
     * @param message        the message key to resolve
     * @return the localized message for one of the provided locales, or {@code null} if not found
     */
    @Nullable String resolveMessage(@NotNull Locale primaryLocale,
                                    @NotNull Locale fallbackLocale,
                                    @NotNull ComponentMessage message) {
        var primaryMessages = messages.get(primaryLocale);
        var localizedMessages = primaryMessages != null ? primaryMessages : messages.get(fallbackLocale);
        return localizedMessages != null ? localizedMessages.get(message) : null;
    }

    /**
     * Loads all locale message files from the {@code /lang} directory and parses them into memory.<br>
     * If the directory does not exist, it is created and default language files are extracted from the plugin JAR.
     */
    private @NotNull Map<Locale, EnumMap<ComponentMessage, String>> loadLocales(@NotNull File langFolder) {
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
    private @NotNull EnumMap<ComponentMessage, String> load(@NotNull File file) {
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
                result.put(message, message.defaultMessage);
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