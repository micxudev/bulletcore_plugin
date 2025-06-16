package org.dredd.bulletcore.config.messages;

import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A utility class responsible for loading flat key-value string messages
 * from a YAML file into a {@link java.util.Map}.
 * <p> Used for loading language or config files.
 * <p>The loader will flatten and load all non-section entries as key-value pairs.
 *
 * @author dredd
 * @since 1.0.0
 */
public final class MessageLoader {

    /**
     * Private constructor to prevent instantiation.
     */
    private MessageLoader() {}

    /**
     * Loads all non-section key-value string pairs from a YAML file.
     *
     * @param file the YAML file to load messages from
     * @return a {@code Map<String, String>} containing key-value pairs from the file
     */
    public static Map<String, String> load(File file) {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        return config.getKeys(true).stream()
            .filter(key -> !config.isConfigurationSection(key))
            .map(key -> Map.entry(key, config.getString(key, "")))
            .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}