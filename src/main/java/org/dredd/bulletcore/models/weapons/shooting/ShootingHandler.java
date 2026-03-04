package org.dredd.bulletcore.models.weapons.shooting;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.dredd.bulletcore.BulletCore;
import org.dredd.bulletcore.config.ConfigManager;
import org.dredd.bulletcore.config.particles.ParticleManager;
import org.dredd.bulletcore.config.sounds.ConfiguredSound;
import org.dredd.bulletcore.config.sounds.SoundManager;
import org.dredd.bulletcore.config.sounds.SoundPlaybackMode;
import org.dredd.bulletcore.custom_item_manager.registries.CustomItemsRegistry;
import org.dredd.bulletcore.listeners.trackers.CurrentHitTracker;
import org.dredd.bulletcore.listeners.trackers.PlayerActionTracker;
import org.dredd.bulletcore.models.armor.Armor;
import org.dredd.bulletcore.models.armor.ArmorHit;
import org.dredd.bulletcore.models.weapons.Weapon;
import org.dredd.bulletcore.models.weapons.damage.DamagePoint;
import org.dredd.bulletcore.models.weapons.damage.DamageThresholds;
import org.dredd.bulletcore.models.weapons.shooting.projectile.AProjectile;
import org.dredd.bulletcore.models.weapons.shooting.projectile.ProjectileFactory;
import org.dredd.bulletcore.models.weapons.shooting.projectile.ProjectileSpawner;
import org.dredd.bulletcore.models.weapons.shooting.recoil.RecoilHandler;
import org.dredd.bulletcore.models.weapons.shooting.spray.SprayHandler;
import org.jetbrains.annotations.NotNull;

import static org.dredd.bulletcore.models.weapons.damage.DamagePoint.BODY;
import static org.dredd.bulletcore.models.weapons.damage.DamagePoint.FEET;
import static org.dredd.bulletcore.models.weapons.damage.DamagePoint.HEAD;
import static org.dredd.bulletcore.models.weapons.damage.DamagePoint.LEGS;

/**
 * Handles weapon shooting (e.g., single, automatic).
 *
 * @author dredd
 * @since 1.0.0
 */
public final class ShootingHandler {

    /**
     * Private constructor to prevent instantiation.
     */
    private ShootingHandler() {}

    /**
     * Stores currently running automatic shooting tasks for each player.
     */
    private static final Map<UUID, BukkitTask> AUTO_SHOOTING_TASKS = new HashMap<>();

    // ----------< Public API >----------

    /**
     * Checks whether the specified player is currently shooting in automatic mode.
     *
     * @param player the player to check
     * @return {@code true} if the player is currently shooting in automatic mode, {@code false} otherwise
     */
    public static boolean isAutoShooting(@NotNull Player player) {
        return AUTO_SHOOTING_TASKS.containsKey(player.getUniqueId());
    }

    /**
     * Cancels the automatic shooting task for the specified player.
     *
     * @param player the player whose shooting task should be canceled
     */
    public static void cancelAutoShooting(@NotNull Player player) {
        final BukkitTask task = AUTO_SHOOTING_TASKS.remove(player.getUniqueId());
        if (task != null) task.cancel();
    }

    /**
     * Clears all shooting tasks. Called when the plugin is reloaded or disabled.
     */
    public static void cancelAllAutoShootingTasks() {
        AUTO_SHOOTING_TASKS.values().forEach(BukkitTask::cancel);
        AUTO_SHOOTING_TASKS.clear();
    }

    /**
     * Attempts to shoot in response to the shooting trigger (LMB).
     *
     * @param player the player who is trying to shoot
     * @param weapon the weapon used
     */
    public static void tryShootOnLMB(@NotNull Player player,
                                     @NotNull Weapon weapon) {
        if (!weapon.reloadHandler.isShootingAllowed(player)) return;
        if (weapon.isAutomatic && isAutoShooting(player)) return;

        final long currentTime = System.currentTimeMillis();
        final long lastShot = weapon.getLastTriggerPullTime(player);
        if (currentTime - lastShot < weapon.delayBetweenShots) return;

        if (weapon.isAutomatic && player.isSneaking()) {
            if (!shoot(player, weapon)) return;

            runAutoShootingTask(player, weapon, weapon.ticksDelayBetweenShots, weapon.ticksDelayBetweenShots);
        } else {
            if (weapon.isAutomatic) PlayerActionTracker.recordSingleShotAutomatic(player.getUniqueId());
            shoot(player, weapon);
        }
    }

    /**
     * Attempts to shoot in response to the SHIFT key being held down
     * within {@link ConfigManager#fireResumeThreshold} ms
     * after the last single shot using an automatic weapon.
     *
     * @param player the player who is trying to shoot
     * @param weapon the weapon used
     */
    public static void tryAutoShootOnToggleSneak(@NotNull Player player,
                                                 @NotNull Weapon weapon) {
        if (!weapon.isAutomatic) return;

        final long now = System.currentTimeMillis();
        final long lastSingleShot = PlayerActionTracker.getLastSingleShotAutomatic(player.getUniqueId());
        final long threshold = ConfigManager.instance().fireResumeThreshold;
        if ((now - lastSingleShot) >= threshold) return;

        if (!weapon.reloadHandler.isShootingAllowed(player)) return;
        if (isAutoShooting(player)) return;

        final long lastShot = weapon.getLastTriggerPullTime(player);
        long ticksUntilShotAvailable = Math.ceilDiv((weapon.delayBetweenShots - (now - lastShot)), 50L);

        if (ticksUntilShotAvailable <= 0L) {
            ticksUntilShotAvailable = weapon.ticksDelayBetweenShots;
            if (!shoot(player, weapon)) return;
        }

        runAutoShootingTask(player, weapon, ticksUntilShotAvailable, weapon.ticksDelayBetweenShots);
    }


    // ----------< Internal API >----------

    /**
     * Runs a new auto shooting task for the specified player.
     *
     * @param player the player to start the task for
     * @param weapon the weapon used
     * @param delay  the ticks to wait before running the task
     * @param period the ticks to wait between runs
     */
    private static void runAutoShootingTask(@NotNull Player player,
                                            @NotNull Weapon weapon,
                                            long delay,
                                            long period) {
        final BukkitTask autoShootingTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!shoot(player, weapon)) cancelAutoShooting(player);
            }
        }.runTaskTimer(BulletCore.instance(), delay, period);

        AUTO_SHOOTING_TASKS.put(player.getUniqueId(), autoShootingTask);
    }

    /**
     * Does a single fire cycle. For most of the weapons this is a single bullet shot.<br>
     * For some weapons (e.g., shotguns), this may be multiple bullet shots.
     *
     * @param player the player who is shooting
     * @param weapon the weapon used
     * @return {@code true} if the shot was successful, {@code false} otherwise.
     */
    // TODO: double check
    private static boolean shoot(@NotNull Player player,
                                 @NotNull Weapon weapon) {
        // always update the last trigger-pull time whenever this method is called,
        // otherwise certain actions may occur more frequently than allowed
        weapon.setLastTriggerPullTime(player);

        // make sure the weapon stack didn't change in the meantime
        final ItemStack weaponStack = player.getInventory().getItemInMainHand();
        if (!weapon.isThisWeapon(weaponStack)) return false;

        final ConfigManager config = ConfigManager.instance();

        // stop if the weapon is empty
        final int bulletCount = weapon.getBulletCount(weaponStack);
        if (bulletCount <= 0) {
            weapon.sounds.play(player, weapon.sounds.empty);
            if (config.enableHotbarMessages)
                weapon.sendWeaponStatus(player, bulletCount);
            return false;
        }

        // update bullet count
        final int newBulletCount = bulletCount - 1;
        weapon.setBulletCount(weaponStack, newBulletCount);
        if (config.enableHotbarMessages)
            weapon.sendWeaponStatus(player, newBulletCount);

        // play fire sound
        weapon.sounds.play(player, weapon.sounds.fire);

        // update recoil
        RecoilHandler.handleShot(player, weapon.recoil);


        // -----< Creating and Shooting Projectile(s) >-----
        final Location eyeLocation = player.getEyeLocation();
        final Vector aimDirection = eyeLocation.getDirection();

        // Create and shoot each pellet separately
        final Vector[] directions = SprayHandler.handleShot(player, weapon, aimDirection);
        for (final Vector direction : directions) {
            final AProjectile projectile = ProjectileFactory.create(eyeLocation, direction, weapon, player);
            ProjectileSpawner.spawn(projectile);
        }

        if (weapon.recoilImpulse > 0.0D) {
            final Vector recoil = aimDirection.clone().multiply(weapon.recoilImpulse);
            final Vector newVelocity = player.getVelocity().subtract(recoil);
            player.setVelocity(newVelocity);
        }

        return true;
    }


    // ----------< HitHandler >----------
    // TODO: move all below to a new HitHandler class
    /*
     * No logical changes done.
     * Only:
     * 1. Config refetch using ConfigManager.instance()
     * 2. eyeLocation for shooter is taken from the current location (shooter.getEyeLocation())
     *
     * New problems:
     * 1. Shooter / World may have changed e.g. (teleport to nether,end / left game) // TODO: test,handle
     * */
    public static void handleHit(@NotNull Player shooter,
                                 @NotNull Weapon weapon,
                                 @NotNull RayTraceResult result,
                                 @NotNull World world) {
        final Location hitLocation = result.getHitPosition().toLocation(world);
        final ConfigManager config = ConfigManager.instance();

        if (result.getHitEntity() instanceof LivingEntity victim) {
            // Entity hit
            final DamagePoint damagePoint = applyCustomDamage(victim, shooter, weapon, hitLocation);
            final ConfiguredSound sound = damagePoint == HEAD ? config.entityHitHeadSound : config.entityHitBodySound;
            final Location soundLocation = sound.mode() == SoundPlaybackMode.WORLD ? hitLocation : shooter.getEyeLocation();
            SoundManager.playSound(shooter, soundLocation, sound);
            ParticleManager.spawnParticle(world, hitLocation, config.entityHitParticle);
        } else if (result.getHitBlock() != null) {
            // Block hit
            SoundManager.playSound(shooter, hitLocation, config.blockHitSound);
            ParticleManager.spawnParticle(world, hitLocation, config.blockHitParticle);
            config.asFeatureManager.bulletHole.spawn(world, hitLocation, result.getHitBlockFace(), result.getHitBlock());
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