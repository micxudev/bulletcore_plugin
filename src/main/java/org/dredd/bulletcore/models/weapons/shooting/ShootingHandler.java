package org.dredd.bulletcore.models.weapons.shooting;

import org.bukkit.FluidCollisionMode;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
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
import org.dredd.bulletcore.models.weapons.Weapon;
import org.dredd.bulletcore.models.weapons.damage.DamagePoint;
import org.dredd.bulletcore.models.weapons.shooting.recoil.RecoilHandler;
import org.dredd.bulletcore.models.weapons.shooting.spray.SprayHandler;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;

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
    private static final Map<UUID, BukkitTask> activeShooters = new HashMap<>();

    /**
     * Checks whether the specified player is currently shooting in automatic mode.
     *
     * @param player the player to check; must not be {@code null}
     * @return {@code true} if the player is currently shooting in automatic mode, {@code false} otherwise
     */
    public static boolean isAutoShooting(@NotNull Player player) {
        return activeShooters.containsKey(player.getUniqueId());
    }

    /**
     * Clears all shooting tasks. Called when the plugin is reloaded or disabled.
     */
    public static void clearAllAutoShootingTasks() {
        activeShooters.values().forEach(BukkitTask::cancel);
        activeShooters.clear();
    }

    /**
     * Cancels the automatic shooting task for the specified player.
     *
     * @param player the player whose shooting task should be canceled; must not be {@code null}
     */
    public static void cancelAutoShooting(@NotNull Player player) {
        BukkitTask task = activeShooters.remove(player.getUniqueId());
        if (task == null) return;
        task.cancel();
    }

    /**
     * Attempts to shoot in response to the shooting trigger (e.g., LMB).
     *
     * @param player the player who is trying to shoot
     * @param weapon the weapon used
     */
    public static void tryShoot(@NotNull Player player, @NotNull Weapon weapon) {
        if (!weapon.reloadHandler.isShootingAllowed(player)) return;
        if (weapon.isAutomatic && isAutoShooting(player)) return;

        // Check delay between shots
        long currentTime = System.currentTimeMillis();
        Long lastShot = weapon.getLastShotTime(player);
        if (lastShot != null && (currentTime - lastShot) < weapon.delayBetweenShots) return;

        // Save new shot time
        weapon.setLastShotTime(player, currentTime);

        if (weapon.isAutomatic && player.isSneaking()) {
            // Fire the first shot immediately to omit waiting next tick
            boolean shotSuccessful = shoot(player, weapon);

            // Start an automatic shooting task if the shot was successful
            if (!shotSuccessful) return;

            long delayBetweenShotsInTicks = weapon.delayBetweenShots / 50;
            BukkitTask shootingTask = new BukkitRunnable() {
                @Override
                public void run() {
                    if (!shoot(player, weapon)) cancelAutoShooting(player);
                }
            }.runTaskTimer(BulletCore.getInstance(), delayBetweenShotsInTicks, delayBetweenShotsInTicks);
            activeShooters.put(player.getUniqueId(), shootingTask);
        } else {
            // Do a single shot
            shoot(player, weapon);
        }
    }

    /**
     * Does a single fire cycle. For most of the weapons this is a single bullet shot.<br>
     * For some weapons (e.g., shotguns), this may be multiple bullet shots.
     *
     * @param player the player who is shooting
     * @param weapon the weapon used
     * @return {@code true} if the shot was successful, {@code false} otherwise.
     */
    private static boolean shoot(@NotNull Player player, @NotNull Weapon weapon) {
        ConfigManager config = ConfigManager.get();

        // Always get actual reference from the hand and make sure it didn't change in the meantime
        ItemStack weaponStack = player.getInventory().getItemInMainHand();
        if (CustomItemsRegistry.getWeaponOrNull(weaponStack) != weapon) return false;

        // Check bullet count
        int bulletCount = weapon.getBulletCount(weaponStack);
        if (bulletCount <= 0) {
            weapon.sounds.play(player, weapon.sounds.empty);
            if (config.enableHotbarMessages)
                weapon.sendActionbar(player, bulletCount);
            return false;
        }

        // Update bullet count
        bulletCount--;
        weapon.setBulletCount(weaponStack, bulletCount);
        if (config.enableHotbarMessages)
            weapon.sendActionbar(player, bulletCount);

        // Set rayTrace settings
        Predicate<Entity> entityFilter = entity ->
            entity instanceof LivingEntity victim && !entity.equals(player) && !skipHit(victim);

        Predicate<Block> canCollide = block -> !config.ignoredMaterials.contains(block.getType());

        World world = player.getWorld();
        Location eyeLocation = player.getEyeLocation();
        Vector direction = SprayHandler.handleShot(player, weapon.spray, eyeLocation.getDirection().normalize());

        RayTraceResult result = world.rayTrace(
            eyeLocation,
            direction,
            weapon.maxDistance,
            FluidCollisionMode.NEVER,   // skips water/lava
            true,                       // ignoredMaterials will handle it
            config.raySize,             // expands ray a little bit
            entityFilter,
            canCollide
        );

        weapon.sounds.play(player, weapon.sounds.fire);
        RecoilHandler.handleShot(player, weapon.recoil);

        // Spawn particles
        double bulletTrailStep = config.bulletTrailStep;
        if (bulletTrailStep > 0) {
            Location start = player.getLocation()
                .add(direction.getX() * 2, player.getHeight() / 2, direction.getZ() * 2);
            Vector rayTo = direction.clone().multiply(
                (result == null) ? weapon.maxDistance : result.getHitPosition().distance(start.toVector())
            );
            Location end = start.clone().add(rayTo);

            double distance = start.distance(end);
            Vector step = direction.clone().multiply(bulletTrailStep);

            Location particleLoc = start.clone();
            for (double d = 0; d <= distance; d += bulletTrailStep) {
                ParticleManager.spawnParticle(world, particleLoc, config.bulletTrailParticle);
                particleLoc.add(step);
            }
        }
        if (config.enableMuzzleFlashes) {
            ParticleManager.spawnParticle(world, player.getLocation()
                    .add(direction.getX() * 2, player.getHeight() / 2, direction.getZ() * 2),
                config.muzzleFlashParticle
            );
        }

        // Handle result
        if (result == null) return true;

        final Location hitLocation = result.getHitPosition().toLocation(world);

        if (result.getHitEntity() instanceof LivingEntity victim) {
            // Entity hit
            DamagePoint damagePoint = applyCustomDamage(victim, player, weapon, hitLocation);
            ParticleManager.spawnParticle(world, hitLocation, config.entityHitParticle);
            ConfiguredSound sound = damagePoint == DamagePoint.HEAD ? config.entityHitHeadSound : config.entityHitBodySound;
            Location soundLocation = sound.mode() == SoundPlaybackMode.WORLD ? hitLocation : eyeLocation;
            SoundManager.playSound(player, soundLocation, sound);
        } else if (result.getHitBlock() != null) {
            // Block hit
            ParticleManager.spawnParticle(world, hitLocation, config.blockHitParticle);
            SoundManager.playSound(player, hitLocation, config.blockHitSound);
            config.asFeatureManager.bulletHole.spawn(world, hitLocation, result.getHitBlockFace());
        }

        return true;
    }

    /**
     * Evaluates whether the bullet should skip the specified entity and go beyond it.
     *
     * @param victim the entity being evaluated for skipping; must not be null
     * @return true if the entity should be skipped; false otherwise
     */
    private static boolean skipHit(@NotNull LivingEntity victim) {
        if (victim.isInvulnerable() || victim.isDead()) return true;

        if (victim instanceof ArmorStand) return true;

        if (victim instanceof Player p) {
            GameMode gamemode = p.getGameMode();
            return gamemode == GameMode.CREATIVE || gamemode == GameMode.SPECTATOR;
        }

        return false;
    }

    /**
     * Applies custom damage to a living entity.
     *
     * @param victim   the entity receiving damage; must not be null
     * @param damager  the player who caused the damage using Weapon; must not be null
     * @param weapon   the weapon used; must not be null
     * @param hitPoint the location where the damage occurred; must not be null
     * @return the damage point of the hit
     */
    private static DamagePoint applyCustomDamage(@NotNull LivingEntity victim, @NotNull Player damager,
                                                 @NotNull Weapon weapon, @NotNull Location hitPoint) {
        DamagePoint damagePoint;
        double finalDamage;

        AttributeInstance victimKnockbackResistance = null;
        double originalKnockbackValue = 0.0;

        if (victim instanceof Player victimPlayer) {
            victimKnockbackResistance = victimPlayer.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE);
            if (victimKnockbackResistance != null) {
                originalKnockbackValue = victimKnockbackResistance.getBaseValue();
                victimKnockbackResistance.setBaseValue(weapon.victimKnockbackResistance);
            }

            damagePoint = DamagePoint.getDamagePoint(victimPlayer, hitPoint);
            finalDamage = switch (damagePoint) {
                case HEAD -> weapon.damage.head();
                case BODY -> weapon.damage.body();
                case LEGS -> weapon.damage.legs();
                case FEET -> weapon.damage.feet();
            };
        } else {
            damagePoint = DamagePoint.BODY; // Non-player entities default to BODY
            finalDamage = weapon.damage.body();
        }

        // Prevents recursion for the same hit
        CurrentHitTracker.startHitProcess(damager.getUniqueId(), victim.getUniqueId());
        try {
            victim.damage(finalDamage, damager); // fires EntityDamageByEntityEvent
            victim.setNoDamageTicks(0); // allow constant hits
        } finally {
            if (victimKnockbackResistance != null) victimKnockbackResistance.setBaseValue(originalKnockbackValue);
            CurrentHitTracker.finishHitProcess(damager.getUniqueId(), victim.getUniqueId());
        }

        return damagePoint;
    }
}