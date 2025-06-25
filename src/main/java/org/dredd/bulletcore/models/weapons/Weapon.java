package org.dredd.bulletcore.models.weapons;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CrossbowMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.dredd.bulletcore.BulletCore;
import org.dredd.bulletcore.config.ConfigManager;
import org.dredd.bulletcore.listeners.trackers.CurrentHitTracker;
import org.dredd.bulletcore.models.CustomBase;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;

import static org.bukkit.inventory.ItemFlag.HIDE_ADDITIONAL_TOOLTIP;
import static org.bukkit.inventory.ItemFlag.HIDE_UNBREAKABLE;

/**
 * Represents weapon items.
 *
 * @author dredd
 * @since 1.0.0
 */
public class Weapon extends CustomBase {

    /**
     * Base weapon damage value.
     */
    public final double damage;

    /**
     * Maximum distance a bullet can travel before it is discarded.
     */
    public final double maxDistance;

    /**
     * This number of milliseconds must elapse before the next shot is available.
     */
    public final long delayBetweenShots;

    /**
     * Stores per-weapon last shot timestamps for each player.<br>
     * Used with {@link #delayBetweenShots} to prevent multiple shots in a short period of time.
     */
    private final Map<UUID, Long> lastShots;


    /**
     * Constructs a new {@link Weapon} instance.
     * <p>
     * All parameters must be already validated.
     */
    public Weapon(BaseAttributes attrs, double damage, double maxDistance, long delayBetweenShots) {
        super(attrs);
        this.damage = damage;
        this.maxDistance = maxDistance;
        this.delayBetweenShots = delayBetweenShots;
        this.lastShots = new HashMap<>();
    }


    /**
     * Triggered when a player attempts to drop a weapon.
     * This method is invoked specifically when the drop action is initiated manually with the drop key (Q).
     *
     * @param player   the player who attempts to drop the weapon
     * @param usedItem the item being dropped, which is this weapon object
     */
    public void onDrop(@NotNull Player player, @NotNull ItemStack usedItem) {
        //System.out.println("Weapon drop attempt on key (Q)");
    }

    @Override
    public @NotNull ItemStack createItemStack() {
        ItemStack stack = createBaseItemStack();
        ItemMeta meta = stack.getItemMeta();

        // Add only weapon-specific attributes
        meta.setUnbreakable(true);
        meta.addItemFlags(HIDE_UNBREAKABLE, HIDE_ADDITIONAL_TOOLTIP);

        stack.setItemMeta(meta);
        return stack;
    }

    @Override
    public boolean onRMB(@NotNull Player player, @NotNull ItemStack usedItem) {
        //System.out.println("Right-click with Weapon");
        return material == Material.CROSSBOW; // Cancel charging the crossbow
    }

    @Override
    public boolean onLMB(@NotNull Player player, @NotNull ItemStack usedItem) {

        // Check delay between shots
        long currentTime = System.currentTimeMillis();
        Long lastShot = lastShots.get(player.getUniqueId());
        if (lastShot != null && (currentTime - lastShot) < delayBetweenShots) return true;

        // Save new shot time
        lastShots.put(player.getUniqueId(), currentTime);

        // Set Item cooldown (only visual for a player)
        int ticksDelay = (int) (delayBetweenShots / 50);
        player.setCooldown(usedItem.getType(), ticksDelay);

        // Fetch config (it is already loaded)
        ConfigManager config = ConfigManager.getOrLoad(BulletCore.getInstance());

        // Set rayTrace settings
        Predicate<Entity> entityFilter = entity ->
            entity instanceof LivingEntity victim && !entity.equals(player) && !skipHit(victim);

        Predicate<Block> canCollide = block -> !config.ignoredMaterials.contains(block.getType());

        World world = player.getWorld();
        Location eyeLocation = player.getEyeLocation();
        Vector direction = eyeLocation.getDirection().normalize();

        RayTraceResult result = world.rayTrace(
            eyeLocation,
            direction,
            maxDistance,
            FluidCollisionMode.NEVER,   // skips water/lava
            false,                      // ignoredMaterials will handle it
            0.01,                       // expands ray a little bit
            entityFilter,
            canCollide
        );

        // Play shot sound
        world.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE /* weapon sound */, 0.2f, 2f);

        // Spawn particles
        double bulletTrailStep = config.bulletTrailStep;
        if (bulletTrailStep > 0) {
            Location start = player.getLocation()
                .add(direction.getX() * 2, player.getHeight() / 2, direction.getZ() * 2);
            Vector rayTo = direction.clone().multiply(
                (result == null) ? maxDistance : result.getHitPosition().distance(start.toVector())
            );
            Location end = start.clone().add(rayTo);

            double distance = start.distance(end);
            Vector step = direction.clone().multiply(bulletTrailStep);

            Location particleLoc = start.clone();
            for (double d = 0; d <= distance; d += bulletTrailStep) {
                world.spawnParticle(Particle.ASH, particleLoc, 1);
                particleLoc.add(step);
            }
        }
        if (config.enableMuzzleFlashes) {
            world.spawnParticle(Particle.LAVA, player.getLocation()
                .add(direction.getX() * 2, player.getHeight() / 2, direction.getZ() * 2), 1);
        }

        // Handle result
        if (result == null) return true;

        final Location hitLocation = result.getHitPosition().toLocation(world);

        if (result.getHitEntity() instanceof LivingEntity victim) {
            // Entity hit
            applyCustomDamage(victim, player, damage, hitLocation);
            world.spawnParticle(Particle.DAMAGE_INDICATOR, hitLocation, 4);
            world.playSound(hitLocation, Sound.ENTITY_ARROW_HIT_PLAYER, 0.5f, 1f);
        } else if (result.getHitBlock() != null) {
            // Block hit
            world.spawnParticle(Particle.CRIT, hitLocation, 4);
            world.playSound(hitLocation, Sound.BLOCK_METAL_HIT, 1f, 1f);
        }

        return true;
    }

    /**
     * Evaluates whether the bullet should skip the specified entity and go beyond it.
     *
     * @param victim the entity being evaluated for skipping; must not be null
     * @return true if the entity should be skipped; false otherwise
     */
    private boolean skipHit(@NotNull LivingEntity victim) {
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
     * @param victim     the entity receiving damage; must not be null
     * @param damager    the player who caused the damage using Weapon; must not be null
     * @param baseDamage initial Weapon damage value; must be greater than 0
     * @param hitPoint   the location where the damage occurred; must not be null
     */
    private void applyCustomDamage(@NotNull LivingEntity victim, @NotNull Player damager, double baseDamage, @NotNull Location hitPoint) {
        // TODO:
        //  - Handle body parts (e.g. headshot multiplier)
        //  - Consider armor
        //  - Shield position
        //  - Armor/shield damage

        // Prevents recursion for the same hit
        CurrentHitTracker.startHitProcess(damager.getUniqueId(), victim.getUniqueId());
        victim.damage(baseDamage, damager); // fires EntityDamageByEntityEvent
        CurrentHitTracker.finishHitProcess(damager.getUniqueId(), victim.getUniqueId());
    }

    @Override
    public boolean onSwapTo(@NotNull Player player, @NotNull ItemStack usedItem) {
        //System.out.println("Swapped to Weapon");

        if (!player.isSneaking()) return false;
        //System.err.println("1. Player is sneaking.");

        if (usedItem.getItemMeta() instanceof CrossbowMeta meta) {
            //System.err.println("2.2. Player swapped TO Crossbow Weapon. Charge Crossbow.");
            meta.setChargedProjectiles(Collections.singletonList(new ItemStack(Material.ARROW)));
            usedItem.setItemMeta(meta);
        }

        return false;
    }

    @Override
    public boolean onSwapAway(@NotNull Player player, @NotNull ItemStack usedItem) {
        //System.out.println("Swapped away from Weapon");

        if (!player.isSneaking()) return false;
        //System.err.println("1. Player is sneaking.");

        if (usedItem.getItemMeta() instanceof CrossbowMeta meta) {
            //System.err.println("2.1. Player swapped FROM Crossbow Weapon. Discharge Crossbow.");
            meta.setChargedProjectiles(null);
            usedItem.setItemMeta(meta);
        }

        return false;
    }
}