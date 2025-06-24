package org.dredd.bulletcore.models.weapons;

import org.bukkit.*;
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

import java.util.*;

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
     * Stores last shot timestamps for each player.
     */
    private static final Map<UUID, Long> lastShots = new HashMap<>();

    public Weapon(BaseAttributes attrs) {
        super(attrs);
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
        // Delay between shots
        long millisBetweenShots = 500L;
        long currentTimeMillis = System.currentTimeMillis();

        Long lastPlayerShot = lastShots.get(player.getUniqueId());
        if (lastPlayerShot != null && (currentTimeMillis - lastPlayerShot) < millisBetweenShots) return true;
        lastShots.put(player.getUniqueId(), currentTimeMillis);

        // Item cooldown (this is only visual for a player)
        int ticksDelay = (int) (millisBetweenShots / 50);
        player.setCooldown(usedItem.getType(), ticksDelay);

        // Start the shot process
        final World world = player.getWorld();
        final Location eyeLocation = player.getEyeLocation();
        final Vector direction = eyeLocation.getDirection().normalize();

        // Shot sound
        world.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 0.2f, 2f);

        // Params
        ConfigManager config = ConfigManager.getOrLoad(BulletCore.getInstance());

        double maxDistanceBlocks = 64;
        double step = config.bulletDetectionStep;
        final Set<Material> ignoredMaterials = config.ignoredMaterials;

        for (double d = 0; d <= maxDistanceBlocks; d += step) {
            final Location point = eyeLocation.clone().add(direction.clone().multiply(d));

            // Block collision check
            if (!ignoredMaterials.contains(point.getBlock().getType())) {
                RayTraceResult blockHit = point.getBlock().rayTrace(point, direction, step, FluidCollisionMode.NEVER);
                if (blockHit != null) {
                    world.spawnParticle(Particle.CRIT, blockHit.getHitPosition().toLocation(world), 1, 0.1, 0.1, 0.1, 1);
                    world.playSound(blockHit.getHitPosition().toLocation(world), Sound.BLOCK_METAL_HIT, 1f, 1f);
                    return true;
                }
            }

            // Entity hit check
            double boxHalfSize = 0.025; // A bullet is like a small box
            Collection<Entity> nearbyEntities = world.getNearbyEntities(point, boxHalfSize, boxHalfSize, boxHalfSize);
            for (Entity nearby : nearbyEntities) {
                if (nearby.equals(player)) continue; // Do not hit the shooter himself

                if (nearby instanceof LivingEntity victim && !skipHit(victim)) {

                    applyCustomDamage(victim, player, 10 /* gun base damage */, point);

                    // Entity hit particle, sound
                    world.spawnParticle(Particle.DAMAGE_INDICATOR, victim.getLocation().add(0, 1, 0), 5, 0.2, 0.2, 0.2, 0.1);
                    world.playSound(point, Sound.ENTITY_ARROW_HIT_PLAYER, 0.5f, 1f);

                    return true; // The victim consumed the shot
                }
            }

            // Spawn particles a bit from the player's eye location (so that they can see better)
            if (d > 2) {
                Location particleLoc = point.clone();
                particleLoc.setY(particleLoc.getY() - 0.1);
                world.spawnParticle(Particle.CRIT, particleLoc, 1, 0, 0, 0, 0);
            }
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