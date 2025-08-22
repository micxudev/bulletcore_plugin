package org.dredd.bulletcore.models.weapons;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.dredd.bulletcore.config.particles.ConfiguredParticle;
import org.dredd.bulletcore.config.particles.ParticleManager;
import org.dredd.bulletcore.utils.MathUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Holds bullet trail particle.
 *
 * @author dredd
 * @since 1.0.0
 */
public class BulletTrailParticle {

    /**
     * Default particle spawned along the bullet when a player fires a shot.
     */
    private static final ConfiguredParticle DEFAULT_PARTICLE =
        new ConfiguredParticle(Particle.DUST, 1, new Particle.DustOptions(Color.fromRGB(0x505050), 0.5F));

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

    private BulletTrailParticle(double step, double offset, ConfiguredParticle particle) {
        this.step = step;
        this.offset = offset;
        this.particle = particle;
    }

    /**
     * Loads a {@link BulletTrailParticle} from a {@link FileConfiguration}.
     *
     * @param cfg the configuration to load from
     * @return a new {@link BulletTrailParticle} instance
     */
    public static @NotNull BulletTrailParticle load(@NotNull FileConfiguration cfg) {
        String s = "particles.";
        return new BulletTrailParticle(
            cfg.getDouble(s + "step", 1.0),
            Math.clamp(cfg.getDouble(s + "offset", 2.0), 0.0, 50.0),
            ParticleManager.loadParticle(cfg, "bullet_trail", DEFAULT_PARTICLE)
        );
    }

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

        if (MathUtils.approximatelyZero(step, 0.01D)) return;

        double travelDistance = (result == null)
            ? weapon.maxDistance - offset
            : eyeLocation.toVector().distance(result.getHitPosition()) - offset;

        if (travelDistance <= 0) return;

        Location particleLoc = eyeLocation.clone().add(direction.clone().multiply(offset));
        Vector step = direction.clone().multiply(this.step);

        for (double traveled = 0; traveled < travelDistance; traveled += this.step) {
            ParticleManager.spawnParticle(world, particleLoc, particle);
            particleLoc.add(step);
        }
    }
}