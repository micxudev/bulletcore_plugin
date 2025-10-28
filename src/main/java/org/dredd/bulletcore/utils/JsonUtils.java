package org.dredd.bulletcore.utils;

import org.dredd.bulletcore.BulletCore;
import org.jetbrains.annotations.NotNull;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.ObjectWriter;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Utility class for JSON serialization.
 *
 * @since 1.0.0
 */
public final class JsonUtils {

    /**
     * Private constructor to prevent instantiation.
     */
    private JsonUtils() {}

    // ----------< Jackson >----------

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final ObjectWriter WRITER = MAPPER.writer();
    private static final ObjectWriter PRETTY_WRITER = MAPPER.writerWithDefaultPrettyPrinter();

    // ----------< Executor >----------

    private static final ExecutorService SAVE_EXECUTOR = Executors.newSingleThreadExecutor();

    public static void shutdownSaveExecutor() {
        SAVE_EXECUTOR.shutdown();
    }

    // ----------< Public API >----------

    /**
     * Reads a JSON file and deserializes it into an instance of the specified type.
     * <p>
     * If the file does not exist or cannot be parsed, the provided {@code defaultValue}
     * is returned. This method supports generic types such as {@code Map}, {@code List},
     * or custom objects via Jackson's {@link TypeReference}.
     *
     * @param file         the JSON file to read from
     * @param typeRef      the Jackson type reference defining the target type
     * @param defaultValue the value to return if loading fails
     * @param <T>          the target type to deserialize into
     * @return the deserialized object, or {@code defaultValue} if reading fails
     */
    public static <T> @NotNull T load(@NotNull File file,
                                      @NotNull TypeReference<T> typeRef,
                                      @NotNull T defaultValue) {
        if (!file.exists())
            return defaultValue;

        try {
            return MAPPER.readValue(file, typeRef);
        } catch (Exception e) {
            BulletCore.logError("Failed to load JSON from " + file.getPath() + ": " + e.getMessage());
            return defaultValue;
        }
    }

    /**
     * Serializes an object to JSON and saves it to a file asynchronously.
     * <p>
     * The operation is queued on a single-threaded executor to ensure
     * non-blocking behavior and prevent concurrent write conflicts.
     * The file is written atomically using a temporary file to guarantee
     * data integrity. Optionally supports pretty-printed output.
     *
     * @param value  the object to serialize
     * @param file   the target file to write to
     * @param pretty whether to pretty print the JSON output
     */
    public static void saveAsync(@NotNull Object value,
                                 @NotNull File file,
                                 boolean pretty) {
        SAVE_EXECUTOR.submit(() -> {
            try {
                final byte[] bytes = (pretty ? PRETTY_WRITER : WRITER).writeValueAsBytes(value);
                writeBytesToFile(file, bytes);
            } catch (Exception e) {
                BulletCore.logError("Failed to save JSON to " + file.getPath() + ": " + e.getMessage());
            }
        });
    }

    // ----------< Utilities >----------

    /**
     * Writes the given byte array to the specified file atomically.
     * <p>
     * The data is first written to a temporary file in the same directory,
     * then atomically moved to the target location. This ensures that the
     * file is never left in a partially written state, even if a failure occurs.
     * <p>
     * If the file’s parent directories do not exist, they will be created automatically.
     *
     * @param file  the target file to write to
     * @param bytes the data to write
     * @throws Exception if an I/O error occurs
     */
    private static void writeBytesToFile(@NotNull File file,
                                         byte[] bytes) throws Exception {
        final Path path = file.toPath();
        final Path parent = path.getParent();

        if (parent != null) Files.createDirectories(parent);

        final Path tempPath = (parent != null)
            ? Files.createTempFile(parent, path.getFileName().toString(), null)
            : Files.createTempFile(path.getFileName().toString(), null);

        Files.write(tempPath, bytes);
        Files.move(tempPath, path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
    }
}