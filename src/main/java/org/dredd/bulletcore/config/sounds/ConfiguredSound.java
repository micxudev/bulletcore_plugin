package org.dredd.bulletcore.config.sounds;

import org.bukkit.SoundCategory;

/**
 * Immutable representation of a configured sound, parsed from a YAML file.
 * <p>
 * Supports built-in Minecraft or custom sounds from a resource pack.<br>
 * Instances are loaded via {@link SoundManager} and validated at config load time.
 * <p>
 * <b>Field details:</b>
 * <ul>
 *   <li>{@code sound} – the sound key (e.g., {@code "entity.arrow.hit_player"} or {@code "custom.ui.click"})</li>
 *   <li>{@code category} – the {@link SoundCategory} that controls which client volume slider applies</li>
 *   <li>{@code volume} – the loudness and audible range (min 0.0, no hard max; 1.0 = normal)</li>
 *   <li>{@code pitch} – perceived pitch of the sound (clamped between 0.5 and 2.0; 1.0 = normal)</li>
 *   <li>{@code seed} – determines which variation plays for sounds with multiple variants;
 *   using the same seed ensures the same variant is selected. To use a random seed, use -1.</li>
 *   <li>{@code mode} – specifies how the sound is played back.</li>
 * </ul>
 *
 * @param sound    the sound identifier (vanilla or resource-pack-defined)
 * @param category the sound category
 * @param volume   the volume (≥ 0.0)
 * @param pitch    the pitch (0.5–2.0)
 * @param seed     controls variation selection for sounds with multiple internal variants or -1 for random
 * @param mode     determines the way the sound is played back
 * @author dredd
 * @see SoundManager
 * @since 1.0.0
 */
public record ConfiguredSound(
    String sound,
    SoundCategory category,
    float volume,
    float pitch,
    long seed,
    SoundPlaybackMode mode
) {}