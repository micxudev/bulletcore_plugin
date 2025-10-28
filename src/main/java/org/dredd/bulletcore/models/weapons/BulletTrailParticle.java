package org.dredd.bulletcore.models.weapons;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.dredd.bulletcore.config.particles.ConfiguredParticle;
import org.dredd.bulletcore.config.particles.ParticleManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

    // -----< Public API >-----

    /**
     * Spawns a particle trail along the path of a fired projectile.
     * <p>
     * The trail starts at the shooter's eye position plus a configurable offset<br>
     * (so that the player can see better) and continues in the given direction.<br>
     * Particles are spawned every {@code bulletTrailStep} units until one of the conditions is met:
     * <ul>
     *   <li>the projectile collides with a block or entity (from {@code result}), or</li>
     *   <li>the weapon's maximum firing distance</li>
     * </ul>
     *
     * @param eyeLocation the starting eye position of the shooter
     * @param direction   the normalized firing direction
     * @param result      the ray-trace result of the shot, or {@code null} if no collision occurred
     * @param weapon      the weapon, used to determine maximum firing range
     * @param world       the world in which to spawn particles
     */
    public void spawn(@NotNull Location eyeLocation,
                      @NotNull Vector direction,
                      @Nullable RayTraceResult result,
                      @NotNull Weapon weapon,
                      @NotNull World world) {

        if (step < 0.01D) return;

        final double travelDistance = (result == null)
            ? weapon.maxDistance - offset
            : eyeLocation.toVector().distance(result.getHitPosition()) - offset;

        if (travelDistance <= 0.0D) return;

        final Location particleLoc = eyeLocation.clone().add(direction.clone().multiply(offset));
        final Vector stepVec = direction.clone().multiply(step);

        for (double traveled = 0.0D; traveled < travelDistance; traveled += step) {
            ParticleManager.spawnParticle(world, particleLoc, particle);
            particleLoc.add(stepVec);
        }
    }
}