package org.dredd.bulletcore.config.messages;

import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.dredd.bulletcore.BulletCore;
import org.dredd.bulletcore.config.ConfigManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.security.CodeSource;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.stream.Collectors;

import static org.dredd.bulletcore.utils.ComponentUtils.MINI;

/**
 * Manages localization and message resolution.
 * <p>
 * This class is responsible for loading language files from the {@code /lang} directory,
 * resolving locale-specific messages, applying placeholders, and converting them
 * into {@link net.kyori.adventure.text.Component} using MiniMessage.
 * </p>
 *
 * <p>
 * Language files should be YAML files named with IETF BCP 47 locale tags {@link Locale#forLanguageTag(String)}.
 * When the plugin is first run, default language files from the JAR under {@code /lang} are extracted to the data folder if missing.
 * </p>
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
    private static MessageManager get() {
        return instance;
    }

    /**
     * Initializes or reloads all the locale messages.
     *
     * @param plugin the plugin instance
     */
    public static void reload(@NotNull BulletCore plugin) {
        instance = new MessageManager(plugin);
        instance.loadLocales();
    }

    private final BulletCore plugin;
    private final File langFolder;
    private final Map<Locale, Map<String, String>> messages;

    /**
     * Constructs a new {@code MessageManager} and initializes required fields.
     * Language files are not loaded here; use {@link #reload(BulletCore)} to initialize.
     *
     * @param plugin the main plugin instance
     */
    private MessageManager(@NotNull BulletCore plugin) {
        this.plugin = plugin;
        this.langFolder = new File(plugin.getDataFolder(), "lang");
        this.messages = new HashMap<>();
    }

    /**
     * Loads all locale message files from the {@code /lang} directory and parses them into memory.<br>
     * If the directory does not exist, it is created and default language files are extracted from the plugin JAR.
     */
    private void loadLocales() {
        if (!langFolder.exists()) {
            if (!langFolder.mkdirs())
                throw new RuntimeException("Failed to create lang folder: " + langFolder.getPath());

            copyDefaultLangFiles();
        }

        File[] files = langFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null) return;

        for (File file : files) {
            String localeKey = file.getName().replace(".yml", "");
            Locale locale = Locale.forLanguageTag(localeKey);
            try {
                messages.put(locale, loadMessages(file));
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to load language file: " + file.getName() + ": " + e.getMessage());
            }
        }

        plugin.getLogger().info("-Loaded " + messages.size() + " language files: " + messages.keySet());
    }

    /**
     * Loads all non-section key-value string pairs from a YAML file.
     *
     * @param file the YAML file to load messages from
     * @return a {@code Map<String, String>} containing key-value pairs from the file
     */
    private static @NotNull Map<String, String> loadMessages(@NotNull File file) {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        return config.getKeys(true).stream()
            .filter(key -> !config.isConfigurationSection(key))
            .map(key -> Map.entry(key, config.getString(key, "")))
            .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));
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

    /**
     * Gets the {@link Locale} of the given {@link CommandSender}, if they are a {@link Player}.
     *
     * @param sender the command sender
     * @return the locale of the player, or {@code null} if not a player
     */
    private @Nullable Locale getLocale(@NotNull CommandSender sender) {
        return sender instanceof Player player ? player.locale() : null;
    }

    /**
     * Resolves the message string for the given component key and sender's locale.<br>
     * Falls back to the component's default string if not found.
     *
     * @param sender  the command sender
     * @param message the component message key and default
     * @return the resolved message string
     */
    private @NotNull String resolveMessage(@NotNull CommandSender sender,
                                           @NotNull ComponentMessage message) {
        @Nullable Locale senderLocale = getLocale(sender);
        @NotNull Locale defaultLocale = ConfigManager.get().locale;
        @NotNull Locale locale1 = senderLocale == null ? defaultLocale : senderLocale;

        var locale1Messages = messages.get(locale1);
        var localizedMessages = locale1Messages == null ? messages.get(defaultLocale) : locale1Messages;

        return localizedMessages == null ? message.def : localizedMessages.getOrDefault(message.key, message.def);
    }

    /**
     * Replaces placeholder tokens in the message string with values from the provided map.
     *
     * @param message      the message string with placeholders (e.g., {@code %player%})
     * @param placeholders a map of placeholder keys to values
     * @return the resolved string with placeholders replaced
     */
    private @NotNull String resolvePlaceholders(@NotNull String message,
                                                @NotNull Map<String, String> placeholders) {
        for (Map.Entry<String, String> entry : placeholders.entrySet())
            message = message.replace("%" + entry.getKey() + "%", entry.getValue());
        return message;
    }

    /**
     * Converts a resolved message into {@link Component}, after placeholder replacement and MiniMessage deserialization.
     *
     * @param sender       the command sender (for locale detection)
     * @param message      the message key and default fallback
     * @param placeholders placeholder values to substitute
     * @return the deserialized MiniMessage component
     */
    private @NotNull Component component(@NotNull CommandSender sender,
                                         @NotNull ComponentMessage message,
                                         @Nullable Map<String, String> placeholders) {
        String resolved = resolveMessage(sender, message);
        String formatted = placeholders != null ? resolvePlaceholders(resolved, placeholders) : resolved;
        return MINI.deserialize(formatted);
    }

    /**
     * Resolves and returns a localized {@link Component} for the given sender and message key,
     * applying any placeholder substitutions.
     *
     * @param sender           the command sender (used to determine locale)
     * @param componentMessage the message key and default fallback string
     * @param placeholders     optional placeholders to substitute (e.g., {@code %player%})
     * @return the resolved and formatted {@link Component}
     */
    public static @NotNull Component of(@NotNull CommandSender sender,
                                        @NotNull ComponentMessage componentMessage,
                                        @Nullable Map<String, String> placeholders) {
        return get().component(sender, componentMessage, placeholders);
    }
}