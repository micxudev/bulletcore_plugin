package org.dredd.bulletcore.models.weapons.damage;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.RayTraceResult;
import org.dredd.bulletcore.config.ConfigManager;
import org.dredd.bulletcore.config.particles.ParticleManager;
import org.dredd.bulletcore.config.sounds.ConfiguredSound;
import org.dredd.bulletcore.config.sounds.SoundManager;
import org.dredd.bulletcore.config.sounds.SoundPlaybackMode;
import org.dredd.bulletcore.custom_item_manager.registries.CustomItemsRegistry;
import org.dredd.bulletcore.listeners.trackers.CurrentHitTracker;
import org.dredd.bulletcore.models.armor.Armor;
import org.dredd.bulletcore.models.armor.ArmorHit;
import org.dredd.bulletcore.models.weapons.Weapon;
import org.jetbrains.annotations.NotNull;

import static org.dredd.bulletcore.models.weapons.damage.DamagePoint.BODY;
import static org.dredd.bulletcore.models.weapons.damage.DamagePoint.FEET;
import static org.dredd.bulletcore.models.weapons.damage.DamagePoint.HEAD;
import static org.dredd.bulletcore.models.weapons.damage.DamagePoint.LEGS;

/**
 * Handles {@link LivingEntity} and {@link Block} hits
 * determined by the given {@link RayTraceResult} in {@link #handleHit}.
 *
 * @author dredd
 * @since 1.0.0
 */
public final class HitHandler {

    /**
     * Private constructor to prevent instantiation.
     */
    private HitHandler() {}

    // TODO: test when the shooter goes to a different world, leaves the game, etc.

    /**
     * Processes the hit result from a ray trace which stopped the bullet/projectile.
     *
     * @param shooter the shooter
     * @param weapon  the weapon used
     * @param result  the result of the ray trace (either a living entity or a block)
     * @param world   the world in which the ray trace took place and the hit occurred
     */
    public static void handleHit(@NotNull Player shooter,
                                 @NotNull Weapon weapon,
                                 @NotNull RayTraceResult result,
                                 @NotNull World world) {
        final Location hitLocation = result.getHitPosition().toLocation(world);
        final ConfigManager config = ConfigManager.instance();

        if (result.getHitEntity() instanceof LivingEntity victim) {
            // Living Entity hit

            final DamagePoint damagePoint =
                applyCustomDamage(victim, shooter, weapon, hitLocation);

            final ConfiguredSound sound = (damagePoint == HEAD)
                ? config.entityHitHeadSound
                : config.entityHitBodySound;

            final Location soundLocation = (sound.mode() == SoundPlaybackMode.WORLD)
                ? hitLocation
                : shooter.getEyeLocation();

            SoundManager.playSound(shooter, soundLocation, sound);
            ParticleManager.spawnParticle(world, hitLocation, config.entityHitParticle);
        } else if (result.getHitBlock() != null) {
            // Block hit

            SoundManager.playSound(shooter, hitLocation, config.blockHitSound);
            ParticleManager.spawnParticle(world, hitLocation, config.blockHitParticle);
            config.asFeatureManager.bulletHole.spawn(
                world, hitLocation, result.getHitBlockFace(), result.getHitBlock()
            );
        }
    }

    /**
     * Applies custom damage to a living entity.
     *
     * @param victim   the entity receiving damage
     * @param damager  the player who caused the damage using Weapon
     * @param weapon   the weapon used
     * @param hitPoint the location where the damage occurred
     * @return the damage point of the hit
     */
    private static @NotNull DamagePoint applyCustomDamage(@NotNull LivingEntity victim,
                                                          @NotNull Player damager,
                                                          @NotNull Weapon weapon,
                                                          @NotNull Location hitPoint) {
        DamagePoint damagePoint = DamagePoint.BODY; // non-player entities will default to BODY
        double finalDamage = weapon.damage.body();

        // START: PLAYER ONLY
        final Player victimPlayer = victim instanceof Player p ? p : null;

        AttributeInstance victimKnockbackResistance = null;
        double originalKnockbackValue = 0.0;

        if (victimPlayer != null) {
            damagePoint = getDamagePoint(victimPlayer, hitPoint);
            finalDamage = getFinalHPDamage(victimPlayer, damagePoint, weapon);

            victimKnockbackResistance = victimPlayer.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE);
            if (victimKnockbackResistance != null) {
                originalKnockbackValue = victimKnockbackResistance.getBaseValue();
                victimKnockbackResistance.setBaseValue(weapon.victimKnockbackResistance);
            }
        }
        // END: PLAYER ONLY

        try {
            CurrentHitTracker.startHitProcess(damager.getUniqueId(), victim.getUniqueId(), weapon);
            victim.damage(finalDamage, damager); // fires EntityDamageByEntityEvent
            victim.setNoDamageTicks(0); // allows constant hits
        } finally {
            CurrentHitTracker.finishHitProcess(damager.getUniqueId(), victim.getUniqueId());

            // PLAYER ONLY
            if (victimPlayer != null) {
                if (victimKnockbackResistance != null)
                    victimKnockbackResistance.setBaseValue(originalKnockbackValue);
                CurrentHitTracker.removeArmorHit(victim.getUniqueId());
            }
        }

        return damagePoint;
    }

    /**
     * Determines the {@link DamagePoint} corresponding to the vertical hit position on the victim.
     * <p>
     * This method uses the Y-coordinate of the hit relative to the victim's bounding box
     * to categorize the hit location. If the player is sleeping, all hits are treated as {@link DamagePoint#HEAD},
     * as the hitbox is tiny and localized to the head region.
     *
     * @param victim   the player who was hit
     * @param hitPoint the location of the hit (typically from ray tracing)
     * @return the body part that was hit, as a {@link DamagePoint}
     */
    private static @NotNull DamagePoint getDamagePoint(@NotNull Player victim,
                                                       @NotNull Location hitPoint) {
        // Player hitbox size (height, width):
        // sleeping: h=0.2, w=0.2
        // standing: h=1.8, w=0.6
        // sneaking: h=1.5, w=0.6
        // lying:    h=0.6, w=0.6

        if (victim.isSleeping()) return HEAD; // while sleeping hitbox is only in the head

        final BoundingBox bb = victim.getBoundingBox();
        final double normalizedY = (hitPoint.getY() - bb.getMinY()) / bb.getHeight();
        final DamageThresholds thr = ConfigManager.instance().damageThresholds;

        if (normalizedY > thr.head()) return HEAD;
        if (normalizedY > thr.body()) return BODY;
        if (normalizedY > thr.legs()) return LEGS;
        return FEET;
    }

    /**
     * Calculates the final Health Points damage to be applied to the victim
     * taking the worn {@link Armor} into account.
     *
     * @param victim      the victim player receiving the damage
     * @param damagePoint the damage point of the hit
     * @param weapon      the weapon used to cause the damage
     * @return the final damage to be applied to the victim
     */
    private static double getFinalHPDamage(@NotNull Player victim,
                                           @NotNull DamagePoint damagePoint,
                                           @NotNull Weapon weapon) {
        final double initialDamage = damagePoint.getDamage(weapon.damage);
        final ItemStack armorStack = damagePoint.getArmor(victim.getInventory());

        final Armor armor = CustomItemsRegistry.getArmorOrNull(armorStack);
        if (armor == null) return initialDamage;

        final ArmorHit armorHit = new ArmorHit(armor, initialDamage, damagePoint, victim);
        CurrentHitTracker.addArmorHit(victim.getUniqueId(), armorHit);

        return initialDamage * (1 - armor.damageReduction);
    }
}