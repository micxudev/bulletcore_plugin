package org.dredd.bulletcore.models.weapons;

import net.kyori.adventure.text.Component;
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
import org.dredd.bulletcore.config.ConfigManager;
import org.dredd.bulletcore.config.messages.ComponentMessage;
import org.dredd.bulletcore.config.particles.ParticleManager;
import org.dredd.bulletcore.config.sounds.SoundManager;
import org.dredd.bulletcore.listeners.trackers.CurrentHitTracker;
import org.dredd.bulletcore.models.CustomBase;
import org.dredd.bulletcore.models.ammo.Ammo;
import org.dredd.bulletcore.models.weapons.damage.WeaponDamage;
import org.dredd.bulletcore.models.weapons.reloading.ReloadHandler;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Predicate;

import static org.bukkit.inventory.ItemFlag.HIDE_ADDITIONAL_TOOLTIP;
import static org.bukkit.inventory.ItemFlag.HIDE_UNBREAKABLE;
import static org.bukkit.persistence.PersistentDataType.INTEGER;
import static org.dredd.bulletcore.config.messages.ComponentMessage.WEAPON_ACTIONBAR;
import static org.dredd.bulletcore.config.messages.MessageManager.of;
import static org.dredd.bulletcore.config.messages.TranslatableMessages.LORE_WEAPON_BULLETS;
import static org.dredd.bulletcore.models.weapons.damage.DamagePoint.getDamagePoint;

/**
 * Represents weapon items.
 *
 * @author dredd
 * @since 1.0.0
 */
public class Weapon extends CustomBase {

    /**
     * Identifier for bullet count on a Weapon ItemStack
     */
    private static final NamespacedKey BULLETS_KEY = new NamespacedKey("bulletcore", "bullets");

    /**
     * Weapon damage values for each body part.
     */
    public final WeaponDamage damage;

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
     * The maximum number of bullets the weapon can hold in its magazine/chamber.
     */
    public final int maxBullets;

    /**
     * The ammo used by this weapon.
     */
    public final Ammo ammo;

    /**
     * This number of milliseconds must elapse before the weapon is reloaded.
     */
    public final long reloadTime;

    /**
     * Manages weapon reloading behavior.
     */
    public final ReloadHandler reloadHandler;

    /**
     * Manages weapon sounds.
     */
    public final WeaponSounds sounds;

    /**
     * Constructs a new {@link Weapon} instance.
     * <p>
     * All parameters must be already validated.
     */
    public Weapon(BaseAttributes attrs, WeaponDamage damage, double maxDistance, long delayBetweenShots, int maxBullets, Ammo ammo, long reloadTime, ReloadHandler reloadHandler, WeaponSounds sounds) {
        super(attrs);
        this.damage = damage;
        this.maxDistance = maxDistance;
        this.delayBetweenShots = delayBetweenShots;
        this.lastShots = new HashMap<>();
        this.maxBullets = maxBullets;
        this.ammo = ammo;
        this.reloadTime = reloadTime;
        this.reloadHandler = reloadHandler;
        this.sounds = sounds;
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
        setBulletCount(stack, maxBullets);
        return stack;
    }

    @Override
    public boolean onRMB(@NotNull Player player, @NotNull ItemStack usedItem) {
        reloadHandler.tryReload(player, this, usedItem);
        return material == Material.CROSSBOW; // Condition to cancel charging the crossbow
    }

    @Override
    public boolean onLMB(@NotNull Player player, @NotNull ItemStack usedItem) {
        if (!reloadHandler.isShootingAllowed(player)) return true;

        // Check delay between shots
        long currentTime = System.currentTimeMillis();
        Long lastShot = lastShots.get(player.getUniqueId());
        if (lastShot != null && (currentTime - lastShot) < delayBetweenShots) return true;

        // Save new shot time
        lastShots.put(player.getUniqueId(), currentTime);

        // Fetch config (it is already loaded)
        ConfigManager config = ConfigManager.get();

        // Check bullet count
        int bulletCount = getBulletCount(usedItem);
        if (bulletCount <= 0) {
            sounds.play(player, sounds.empty);
            if (config.enableHotbarMessages)
                sendActionbar(player, bulletCount);
            return true;
        }

        // Update bullet count
        bulletCount--;
        setBulletCount(usedItem, bulletCount);
        if (config.enableHotbarMessages)
            sendActionbar(player, bulletCount);

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
            true,                       // ignoredMaterials will handle it
            config.raySize,             // expands ray a little bit
            entityFilter,
            canCollide
        );

        sounds.play(player, sounds.fire);

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
            applyCustomDamage(victim, player, hitLocation);
            ParticleManager.spawnParticle(world, hitLocation, config.entityHitParticle);
            SoundManager.playSound(world, hitLocation, config.entityHitSound);
        } else if (result.getHitBlock() != null) {
            // Block hit
            ParticleManager.spawnParticle(world, hitLocation, config.blockHitParticle);
            SoundManager.playSound(world, hitLocation, config.blockHitSound);
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
     * @param victim   the entity receiving damage; must not be null
     * @param damager  the player who caused the damage using Weapon; must not be null
     * @param hitPoint the location where the damage occurred; must not be null
     */
    private void applyCustomDamage(@NotNull LivingEntity victim, @NotNull Player damager, @NotNull Location hitPoint) {
        // TODO:
        //  - Consider armor
        //  - Shield position
        //  - Armor/shield damage

        double finalDamage;
        if (victim instanceof Player victimPlayer) {
            finalDamage = switch (getDamagePoint(victimPlayer, hitPoint)) {
                case HEAD -> damage.head(); // TODO: apply helmet reduction
                case BODY -> damage.body(); // TODO: apply chestplate reduction
                case LEGS -> damage.legs();
                case FEET -> damage.feet();
            };
        } else {
            // For now, all non-player entities always get body damage
            finalDamage = damage.body();
        }

        // Prevents recursion for the same hit
        CurrentHitTracker.startHitProcess(damager.getUniqueId(), victim.getUniqueId());
        victim.damage(finalDamage, damager); // fires EntityDamageByEntityEvent
        CurrentHitTracker.finishHitProcess(damager.getUniqueId(), victim.getUniqueId());
    }

    @Override
    public boolean onSwapTo(@NotNull Player player, @NotNull ItemStack usedItem) {
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
        ReloadHandler.cancelReload(player, false);

        if (!player.isSneaking()) return false;
        //System.err.println("1. Player is sneaking.");

        if (usedItem.getItemMeta() instanceof CrossbowMeta meta) {
            //System.err.println("2.1. Player swapped FROM Crossbow Weapon. Discharge Crossbow.");
            meta.setChargedProjectiles(null);
            usedItem.setItemMeta(meta);
        }

        return false;
    }

    /**
     * Retrieves the current bullet count stored in the given {@link ItemStack}'s metadata.
     *
     * @param stack The {@link ItemStack} representing {@link Weapon} to retrieve the bullet count from.
     * @return The number of bullets currently stored in the weapon stack.<br>
     * Returns {@code 0} if the stack did not store bullet count metadata.
     */
    public int getBulletCount(@NotNull ItemStack stack) {
        return stack.getItemMeta().getPersistentDataContainer().getOrDefault(BULLETS_KEY, INTEGER, 0);
    }

    /**
     * Sets the bullet count for the given {@link ItemStack}, updating both persistent data and lore display.
     *
     * @param stack The weapon {@link ItemStack} to modify.
     * @param count The number of bullets to set for this weapon stack.
     */
    public void setBulletCount(@NotNull ItemStack stack, int count) {
        ItemMeta meta = stack.getItemMeta();
        meta.getPersistentDataContainer().set(BULLETS_KEY, INTEGER, count);

        List<Component> lore = meta.lore();
        lore.set(0, LORE_WEAPON_BULLETS.of(count, maxBullets));
        meta.lore(lore);
        stack.setItemMeta(meta);
    }

    /**
     * Sends the {@link ComponentMessage#WEAPON_ACTIONBAR} message to a specified player.
     *
     * @param player  the player to whom the actionbar message will be sent; must not be null
     * @param current the current bullet count in the weapon
     */
    public void sendActionbar(@NotNull Player player, int current) {
        player.sendActionBar(of(player, WEAPON_ACTIONBAR,
            Map.of(
                "displayname", displayNameString,
                "bullets", Integer.toString(current),
                "maxbullets", Integer.toString(maxBullets),
                "total", Integer.toString(ammo.getAmmoCount(player))
            )
        ));
    }
}