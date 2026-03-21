package org.dredd.bulletcore.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.dredd.bulletcore.BulletCore;
import org.jetbrains.annotations.NotNull;

/**
 * Utility class for logging.
 *
 * @since 1.0.0
 */
public final class LogUtils {

    /**
     * Private constructor to prevent instantiation.
     */
    private LogUtils() {}

    // ----------< Utils >----------

    /**
     * Specifies the format for log timestamps. Format: <code>2026-03-21 15:30:45</code>.
     */
    private static final DateTimeFormatter LOG_TIMESTAMP_FORMAT =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Specifies the options for opening a file.
     * <p>
     * Creates a file if it does not exist, and writes to the end of that file.
     */
    private static final OpenOption[] OPEN_OPTIONS = {
        StandardOpenOption.CREATE,
        StandardOpenOption.WRITE,
        StandardOpenOption.APPEND
    };

    // ----------< Executor >----------
    private static final ExecutorService LOG_EXECUTOR = Executors.newSingleThreadExecutor();

    public static void shutdownLogExecutor() {
        LOG_EXECUTOR.shutdown();
    }

    // ----------< Public API >----------

    /**
     * Appends the given message to the given file asynchronously.
     *
     * @param message the message to append
     * @param file the file to append to
     */
    public static void appendLogAsync(@NotNull String message,
                                      @NotNull File file) {
        LOG_EXECUTOR.submit(() -> {
            try {
                final Path path = file.toPath();

                final Path parent = path.getParent();
                if (parent != null) {
                    Files.createDirectories(parent);
                }

                final String timestamp = LOG_TIMESTAMP_FORMAT.format(LocalDateTime.now());

                final String logEntry = "[" + timestamp + "] " + message + System.lineSeparator();

                try (final BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8, OPEN_OPTIONS)
                ) {
                    writer.write(logEntry);
                }
            } catch (Exception e) {
                BulletCore.logError("Failed to append log into file \"" + file + "\": " + e.getMessage());
            }
        });
    }
}