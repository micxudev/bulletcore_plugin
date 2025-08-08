package org.dredd.bulletcore.config.sounds;

/**
 * Determines how a sound is played back to listeners.
 *
 * @author dredd
 * @see ConfiguredSound
 * @since 1.0.0
 */
public enum SoundPlaybackMode {

    /**
     * Plays the sound at a location in the world.<br>
     * All players within audible range will hear it.<br>
     * Subject to their client volume settings and distance from the source.
     */
    WORLD,

    /**
     * Sends the sound packet directly to a specific player.<br>
     * Only that player will hear it, regardless of their position in the world.
     */
    PLAYER
}