package org.dredd.bulletcore.compatibility.entity;

/**
 * Defines meta flags for entities.
 *
 * @author dredd
 * @since 1.0.0
 */
public enum EntityMetaFlag {

    FIRE(0),
    SNEAKING(1),
    SPRINTING(3),
    SWIMMING(4),
    INVISIBLE(5),
    GLOWING(6),
    GLIDING(7);

    public final int index;

    EntityMetaFlag(int index) {
        this.index = index;
    }
}