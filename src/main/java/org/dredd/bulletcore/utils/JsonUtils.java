package org.dredd.bulletcore.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.dredd.bulletcore.BulletCore;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
     * A single-threaded executor to serialize save operations and preserve ordering.
     * This prevents race conditions where older data might overwrite newer data.
     *
     * @since 1.0.0
     */
    private static final ExecutorService SAVE_EXECUTOR = Executors.newSingleThreadExecutor();

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
    public static @NotNull Map<UUID, Map<String, List<String>>> loadPlayerWeaponSkins(@NotNull File file) {
        if (!file.exists()) return new HashMap<>();
        try {
            return mapper.readValue(file, new TypeReference<>() {});
        } catch (Exception e) {
            BulletCore.getInstance().getLogger().severe("Failed to load playerSkins from file: " + e.getMessage());
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
        byte[] jsonBytes;
        try {
            jsonBytes = mapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(skins);
        } catch (JsonProcessingException e) {
            BulletCore.getInstance().getLogger().severe("Failed to serialize playerSkins: " + e.getMessage());
            return;
        }

        SAVE_EXECUTOR.submit(() -> {
            try {
                attemptCreateFileWithDirs(file);
                File tempFile = new File(
                    Optional.ofNullable(file.getParentFile()).orElse(new File(".")),
                    file.getName() + ".tmp"
                );

                Files.write(tempFile.toPath(), jsonBytes);

                Files.move(tempFile.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                BulletCore.getInstance().getLogger().severe("Failed to save playerSkins to file: " + e.getMessage());
            }
        });
    }

    /**
     * Creates the file and its parent directories if they do not exist.
     *
     * @param file the file to create
     * @throws IOException if an I/O error occurs during file or directory creation
     * @since 1.0.0
     */
    private static void attemptCreateFileWithDirs(@NotNull File file) throws IOException {
        File parent = file.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs())
            throw new IOException("Failed to create parent directories for file: " + file.getPath());
        if (!file.exists() && !file.createNewFile())
            throw new IOException("Failed to create file: " + file.getPath());
    }

    /**
     * Shuts down the {@link #SAVE_EXECUTOR} and waits for it to terminate.
     */
    public static void shutdownSaveExecutor() {
        SAVE_EXECUTOR.shutdown();
    }
}