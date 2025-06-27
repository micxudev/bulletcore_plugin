package org.dredd.bulletcore.config.messages;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A utility class for loading structured data from YAML configuration files.
 *
 * @author dredd
 * @since 1.0.0
 */
public final class YMLLoader {

    /**
     * Private constructor to prevent instantiation.
     */
    private YMLLoader() {}

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

    /**
     * Loads structured style definitions from a nested YAML file.<br>
     * Expects a YAML file where each top-level key maps to a section of subkeys and style definitions.
     *
     * <p><strong>Expected YAML format:</strong></p>
     * <pre>
     * style_key:
     *   key: "&7"
     *   arg0: "&a"
     *   arg1: "&c"
     * </pre>
     *
     * @param file the YAML file containing nested style sections
     * @return a map where each top-level key maps to an ordered map of style keys and values
     */
    public static Map<String, LinkedHashMap<String, String>> loadStyles(File file) {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        Map<String, LinkedHashMap<String, String>> result = new HashMap<>();

        for (String sectionKey : config.getKeys(false)) {
            ConfigurationSection section = config.getConfigurationSection(sectionKey);
            if (section == null) continue;

            LinkedHashMap<String, String> styles = new LinkedHashMap<>();
            for (String key : section.getKeys(false))
                styles.put(key, section.getString(key, ""));

            result.put(sectionKey, styles);
        }

        return Collections.unmodifiableMap(result);
    }
}