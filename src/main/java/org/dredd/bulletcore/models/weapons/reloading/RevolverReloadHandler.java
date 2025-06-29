package org.dredd.bulletcore.models.weapons.reloading;

import org.jetbrains.annotations.NotNull;

/**
 * Revolver reload handler implementation.
 * <p>This implementation refills one bullet at a time into the magazine after the specified reload time.
 *
 * @author dredd
 * @since 1.0.0
 */
public class RevolverReloadHandler implements ReloadHandler {

    @Override
    public @NotNull String getName() {
        return "revolver";
    }
}