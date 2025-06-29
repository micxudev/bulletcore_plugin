package org.dredd.bulletcore.models.weapons.reloading;

import org.jetbrains.annotations.NotNull;

/**
 * Defines a weapon reload handler interface used to refill ammo/bullets into weapons.
 *
 * @author dredd
 * @since 1.0.0
 */
public interface ReloadHandler {

    /**
     * The name used to identify this reload handler.
     */
    @NotNull String getName();
}