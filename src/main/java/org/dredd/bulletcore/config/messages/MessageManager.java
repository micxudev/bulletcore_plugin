package org.dredd.bulletcore.config.messages;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.dredd.bulletcore.BulletCore;
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
     * The shared instance of the message manager.
     */
    private static MessageManager messageManager;

    private final BulletCore plugin;
    private final File langFolder;
    private final Map<Locale, Map<String, String>> messages;
    private final MiniMessage miniMessage;

    /**
     * Constructs a new {@code MessageManager} and initializes required fields.
     * Language files are not loaded here; use {@link #reload(BulletCore)} to initialize.
     *
     * @param plugin the main plugin instance
     */
    private MessageManager(BulletCore plugin) {
        this.plugin = plugin;
        this.langFolder = new File(plugin.getDataFolder(), "lang");
        this.messages = new HashMap<>();
        this.miniMessage = MiniMessage.miniMessage();
    }

    /**
     * Loads all locale message files from the {@code /lang} directory and parses them into memory.<br>
     * If the directory does not exist, it is created and default language files are extracted from the plugin JAR.
     */
    private void loadLocales() {
        if (!langFolder.exists()) {
            langFolder.mkdirs();
            copyDefaultLangFiles();
        }

        File[] files = langFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null) return;

        for (File file : files) {
            String localeKey = file.getName().replace(".yml", "");
            Locale locale = Locale.forLanguageTag(localeKey);
            try {
                messages.put(locale, YMLLoader.load(file));
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to load language file: " + file.getName() + ": " + e.getMessage());
            }
        }

        plugin.getLogger().info("-Loaded " + messages.size() + " language files: " + messages.keySet());
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
     * Returns the singleton instance of the {@code MessageManager}, initializing it if necessary.
     *
     * @return the shared message manager instance
     */
    private static @NotNull MessageManager get() {
        if (messageManager == null)
            reload(BulletCore.getInstance());
        return messageManager;
    }

    /**
     * Reloads the message manager with the given plugin instance and re-loads all locale messages.
     *
     * @param plugin the plugin instance
     */
    public static void reload(BulletCore plugin) {
        messageManager = new MessageManager(plugin);
        messageManager.loadLocales();
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
    private @NotNull String resolveMessage(@NotNull CommandSender sender, @NotNull ComponentMessage message) {
        Locale locale = getLocale(sender);
        if (locale == null)
            return message.def;

        Map<String, String> localizedMessages = messages.get(locale);
        if (localizedMessages == null)
            return message.def;

        return localizedMessages.getOrDefault(message.key, message.def);
    }

    /**
     * Replaces placeholder tokens in the message string with values from the provided map.
     *
     * @param message      the message string with placeholders (e.g., {@code %player%})
     * @param placeholders a map of placeholder keys to values
     * @return the resolved string with placeholders replaced
     */
    private @NotNull String resolvePlaceholders(String message, @Nullable Map<String, String> placeholders) {
        if (placeholders != null && !placeholders.isEmpty())
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
        String formatted = resolvePlaceholders(resolved, placeholders);
        return miniMessage.deserialize(formatted);
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