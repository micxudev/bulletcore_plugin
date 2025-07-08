package org.dredd.bulletcore.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Utility class for JSON serialization helper methods.
 *
 * @since 1.0.0
 */
public final class JsonUtils {

    /**
     * Private constructor to prevent instantiation.
     */
    private JsonUtils() {}

    /**
     * The Jackson {@link ObjectMapper} used for JSON serialization.
     */
    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * Loads player weapon skins from a JSON file.
     * <p>
     * The file is expected to contain a JSON object where each key is a player UUID,
     * and the value is a map of weapon names to lists of skin identifiers.
     * If the file does not exist or an error occurs while reading, an empty map is returned.
     *
     * @param file the file to read from (must not be {@code null})
     * @return a map of player UUIDs to weapon skin mappings
     * @since 1.0.0
     */
    public static Map<UUID, Map<String, List<String>>> loadPlayerWeaponSkins(@NotNull File file) {
        if (!file.exists()) return new HashMap<>();
        try {
            return mapper.readValue(file, new TypeReference<>() {});
        } catch (IOException e) {
            Bukkit.getLogger().severe("Failed to load playerSkins from file: " + e.getMessage());
            return new HashMap<>();
        }
    }

    /**
     * Saves player weapon skins to a JSON file.
     * <p>
     * The data is serialized in a pretty-printed JSON format. If the target file or its
     * parent directories do not exist, they are created automatically.
     * If an error occurs during writing, it is logged to the server log.
     *
     * @param skins the map of player UUIDs to weapon skin mappings; must not be {@code null}
     * @param file  the file to write to; must not be {@code null}
     * @since 1.0.0
     */
    public static void savePlayerWeaponSkins(@NotNull Map<UUID, Map<String, List<String>>> skins, @NotNull File file) {
        try {
            attemptCreateFileWithDirs(file);
            mapper.writerWithDefaultPrettyPrinter().writeValue(file, skins);
        } catch (IOException e) {
            Bukkit.getLogger().severe("Failed to save playerSkins to file: " + e.getMessage());
        }
    }

    /**
     * Creates the file and its parent directories if they do not exist.
     *
     * @param file the file to create
     * @throws IOException if an I/O error occurs during file or directory creation
     * @since 1.0.0
     */
    private static void attemptCreateFileWithDirs(File file) throws IOException {
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) parent.mkdirs();
        if (!file.exists()) file.createNewFile();
    }
}