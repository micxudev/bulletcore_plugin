package org.dredd.bulletcore.models.weapons.reloading;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Defines reload handler implementations.
 *
 * @author dredd
 * @since 1.0.0
 */
public enum ReloadType {

    DEFAULT(DefaultReloadHandler.INSTANCE),
    SINGLE(SingleReloadHandler.INSTANCE);

    private final ReloadHandler handler;

    ReloadType(@NotNull ReloadHandler handler) {
        this.handler = handler;
    }

    /**
     * Resolves a reload handler by name.
     */
    public static @Nullable ReloadHandler getHandlerOrNull(@NotNull String name) {
        for (final var type : values())
            if (type.handler.getName().equals(name))
                return type.handler;
        return null;
    }
}