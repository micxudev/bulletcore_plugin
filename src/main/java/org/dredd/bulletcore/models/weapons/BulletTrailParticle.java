package org.dredd.bulletcore.models.weapons;

import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.configuration.file.YamlConfiguration;
import org.dredd.bulletcore.config.particles.ConfiguredParticle;
import org.dredd.bulletcore.config.particles.ParticleManager;
import org.jetbrains.annotations.NotNull;

/**
 * Holds bullet trail particle.
 *
 * @author dredd
 * @since 1.0.0
 */
public final class BulletTrailParticle {

    // ----------< Static >----------

    // -----< Defaults >-----

    /**
     * Default particle spawned along the bullet when a player fires a shot.
     */
    private static final ConfiguredParticle DEFAULT_PARTICLE = new ConfiguredParticle(
        Particle.DUST, 1, new Particle.DustOptions(Color.fromRGB(0x505050), 0.5F)
    );

    // -----< Loader >-----

    /**
     * Loads a {@link BulletTrailParticle} from a YAML config.
     *
     * @param config the configuration to load from
     * @return a new {@link BulletTrailParticle} instance
     */
    public static @NotNull BulletTrailParticle load(@NotNull YamlConfiguration config) {
        return new BulletTrailParticle(config);
    }


    // ----------< Instance >----------

    // -----< Attributes >-----

    /**
     * Distance between particle spawn points in blocks.<br>
     * Lower values make trails denser.<br>
     * Use a negative value to disable.<br>
     */
    public final double step;

    /**
     * Offset in blocks forward from player eyes to spawn bullet trails.
     */
    public final double offset;

    /**
     * Particle to spawn.
     */
    public final ConfiguredParticle particle;

    // -----< Construction >-----

    /**
     * Private constructor. Use {@link #load(YamlConfiguration)} instead.
     */
    private BulletTrailParticle(@NotNull YamlConfiguration config) {
        this.step = config.getDouble("particles.step", 1.0D);
        this.offset = Math.clamp(config.getDouble("particles.offset", 2.0D), 0.0D, 20.0D);
        this.particle = ParticleManager.loadParticle(config, "bullet_trail", DEFAULT_PARTICLE);
    }
}