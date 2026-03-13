package org.dredd.bulletcore.models.weapons.shooting.projectile;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.dredd.bulletcore.compatibility.entity.FakeEntity;
import org.dredd.bulletcore.compatibility.entity.FakeEntityFactory;
import org.dredd.bulletcore.models.weapons.Weapon;
import org.jetbrains.annotations.NotNull;

/**
 * Factory for creating projectiles (base class {@link AProjectile}).
 *
 * @author dredd
 * @since 1.0.0
 */
public final class ProjectileFactory {

    /**
     * Private constructor to prevent instantiation.
     */
    private ProjectileFactory() {}

    /**
     * Creates and initializes a new {@link WeaponProjectile} instance.
     * <p>
     * The provided {@code startLocation} and {@code normalizedDirection} are <b>not</b> used directly.<br>
     * Instead, defensive copies are created to ensure the original objects are not modified.
     * <p>
     * The direction vector is expected to be normalized. It is multiplied by the
     * weapon's configured muzzle velocity to compute the projectile's initial velocity.
     * <p>
     * If the weapon defines a disguise type in its {@link ProjectileSettings},
     * a corresponding {@link FakeEntity} is created and attached to the projectile.
     *
     * @param startLocation       the initial world position from which the projectile should be spawned
     * @param normalizedDirection the normalized direction indicating the projectile's travel direction
     * @param weapon              the weapon used for firing the projectile
     * @param shooter             the player who fired the projectile
     *
     * @return a newly constructed and initialized {@link WeaponProjectile} instance
     * that has not yet been ticked or registered
     */
    public static @NotNull AProjectile create(@NotNull Location startLocation,
                                              @NotNull Vector normalizedDirection,
                                              @NotNull Weapon weapon,
                                              @NotNull Player shooter) {
        final ProjectileSettings settings = weapon.projectileSettings;

        final Vector velocity = normalizedDirection.clone().multiply(settings.muzzleVelocity);

        final FakeEntity disguise = FakeEntityFactory.create(
            settings.disguiseType, startLocation, settings.disguiseData
        );

        return new WeaponProjectile(weapon, shooter, startLocation, velocity, disguise);
    }
}