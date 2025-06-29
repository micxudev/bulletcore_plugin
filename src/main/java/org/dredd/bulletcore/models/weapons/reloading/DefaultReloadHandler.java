package org.dredd.bulletcore.models.weapons.reloading;

import org.jetbrains.annotations.NotNull;

/**
 * Default reload handler implementation.
 * <p>This implementation refills all bullets at once into the magazine after the specified reload time.
 *
 * @author dredd
 * @since 1.0.0
 */
public class DefaultReloadHandler implements ReloadHandler {

    @Override
    public @NotNull String getName() {
        return "default";
    }
}