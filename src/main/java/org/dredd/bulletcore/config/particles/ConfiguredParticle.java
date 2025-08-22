package org.dredd.bulletcore.config.particles;

import org.bukkit.Particle;
import org.jetbrains.annotations.Nullable;

/**
 * Immutable representation of a configured particle effect, parsed from a YAML file.
 * <p>
 * Supports any built-in Minecraft particle as listed in the official registry.<br>
 * Instances are loaded and validated at config load time.
 * <p>
 * <b>Field details:</b>
 * <ul>
 *   <li>{@code particle} – the particle type to spawn (e.g., {@code Particle.CRIT}, {@code Particle.LAVA})</li>
 *   <li>{@code count} – number of particles to spawn per emission (0 disables; higher values may impact performance)</li>
 *   <li>{@code data} – particle-specific data (see {@link Particle#getDataType()})
 * </ul>
 *
 * @param particle the {@link Particle} type to spawn
 * @param count    the number of particles (≥ 0)
 * @param data     the data to apply to the particle
 * @author dredd
 * @since 1.0.0
 */
public record ConfiguredParticle(
    Particle particle,
    int count,
    @Nullable Object data
) {}