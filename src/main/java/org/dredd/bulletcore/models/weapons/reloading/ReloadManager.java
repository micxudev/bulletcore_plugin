package org.dredd.bulletcore.models.weapons.reloading;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages reload handler implementations.
 *
 * @author dredd
 * @since 1.0.0
 */
public final class ReloadManager {

    /**
     * Private constructor to prevent instantiation.
     */
    private ReloadManager() {}

    /**
     * A map of registered reload handlers by name.
     */
    private static final Map<String, ReloadHandler> handlers = new HashMap<>();

    /**
     * A list of reload handler implementations.
     */
    private static final List<ReloadHandler> RELOAD_HANDLERS = List.of(
        DefaultReloadHandler.INSTANCE,
        SingleReloadHandler.INSTANCE
    );

    /**
     * Initializes all reload handler implementations.
     */
    public static void init() {
        RELOAD_HANDLERS.forEach(ReloadManager::register);
    }

    /**
     * Registers a reload handler implementation.
     *
     * @param handler the reload handler implementation to register
     */
    private static void register(@NotNull ReloadHandler handler) {
        handlers.put(handler.getName(), handler);
    }

    /**
     * Gets the reload handler implementation by name.
     *
     * @param name the name of the reload handler implementation to retrieve
     * @return the reload handler implementation with the given name, or {@code null} if not found
     */
    public static @Nullable ReloadHandler getHandlerOrNull(@NotNull String name) {
        return handlers.get(name);
    }
}