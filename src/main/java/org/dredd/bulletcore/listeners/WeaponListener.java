package org.dredd.bulletcore.listeners;

import com.destroystokyo.paper.event.server.ServerTickEndEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.dredd.bulletcore.BulletCore;
import org.dredd.bulletcore.listeners.trackers.CurrentHitTracker;
import org.dredd.bulletcore.models.armor.ArmorHit;
import org.dredd.bulletcore.models.weapons.Weapon;
import org.dredd.bulletcore.models.weapons.shooting.CurrentHit;
import org.dredd.bulletcore.models.weapons.shooting.ShootingHandler;
import org.dredd.bulletcore.models.weapons.shooting.spray.SprayHandler;
import org.dredd.bulletcore.utils.ServerUtils;

import static org.dredd.bulletcore.custom_item_manager.registries.CustomItemsRegistry.getWeaponOrNull;
import static org.dredd.bulletcore.custom_item_manager.registries.CustomItemsRegistry.isWeapon;

/**
 * Listens for the specific events related to items representing {@link Weapon}.
 *
 * @author dredd
 * @since 1.0.0
 */
public enum WeaponListener implements Listener {

    INSTANCE;

    // ----------< Constants >----------

    /**
     * Raw off-hand slot.
     */
    private static final int RAW_OFFHAND_SLOT = 45;

    /**
     * Converted off-hand slot.
     *
     * @see InventoryView#convertSlot(int)
     */
    private static final int CONVERTED_OFFHAND_SLOT = 40;

    // ----------< Events >----------

    /**
     * Prevent placing Weapon into the off-hand slot {@code on inventory click}.
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void preventOffhandWeaponPlacementOnInventoryClick(InventoryClickEvent event) {
        //System.err.println("===============================");
        //System.err.println("0. InventoryClickEvent.");

        //System.err.println("1. Event is not canceled.");

        if (!(event.getClickedInventory() instanceof PlayerInventory playerInventory)) return;
        //System.err.println("2. Click is inside player inventory.");

        if (event.getClick() == ClickType.SWAP_OFFHAND) {
            //System.err.println("3.1. Current item during Swap: " + (event.getCurrentItem() != null ? event.getCurrentItem().getType() : "null"));
            if (isWeapon(event.getCurrentItem())) {
                //System.err.println("4. Current item during Swap is a Weapon. Canceled event.");
                event.setCancelled(true);
                return;
            }
        }

        //System.err.println("3.2. Clicked (converted) slot: " + event.getSlot());
        if (event.getSlot() != CONVERTED_OFFHAND_SLOT) return;
        //System.err.println("4. Slot is off-hand: " + event.getSlot());

        //System.err.println("5. Cursor item: " + event.getCursor().getType());
        if (isWeapon(event.getCursor())) {
            //System.err.println("6. Cursor item is a Weapon. Canceled event.");
            event.setCancelled(true);
            event.getView().setCursor(event.getCursor()); // cursor item disappear glitch fix
            return;
        }

        if (event.getClick() == ClickType.NUMBER_KEY) {
            final ItemStack hotbarItem = playerInventory.getItem(event.getHotbarButton());
            //System.err.println("6. Hotbar item: " + (hotbarItem != null ? hotbarItem.getType() : "null"));
            if (isWeapon(hotbarItem)) {
                //System.err.println("7. Hotbar item is a Weapon. Canceled event.");
                event.setCancelled(true);
                return;
            }
        }

        // The code below is only needed if we want to prevent removing a weapon from the off-hand slot.
        // As for now, we never put a weapon to the off-hand slot.
        // But if we ever do (e.g., we might: place scope, fake weapon), we uncomment it.
        //System.err.println("7. Current item: " + (event.getCurrentItem() != null ? event.getCurrentItem().getType() : "null"));
        /*if (isWeapon(event.getCurrentItem())) {
            //System.err.println("8. Current item is a Weapon. Canceled event.");
            event.setCancelled(true);
        }*/
    }

    /**
     * Prevent placing Weapon into the off-hand slot {@code on inventory drag}.
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void preventOffhandWeaponPlacementOnInventoryDrag(InventoryDragEvent event) {
        //System.err.println("===============================");
        //System.err.println("0. InventoryDragEvent.");

        if (event.getView().getTopInventory().getType() != InventoryType.CRAFTING) return;
        //System.err.println("1. Top inventory is player inventory (no external container open).");

        final ItemStack draggedItem = event.getNewItems().get(RAW_OFFHAND_SLOT);
        //System.err.println("2. Dragged item into off-hand slot: " + (draggedItem != null ? draggedItem.getType() : "null"));
        if (isWeapon(draggedItem)) {
            //System.err.println("3. Off-hand slot item is a Weapon. Canceled event.");
            event.setCancelled(true);
        }
    }

    /**
     * Charge or discharge weapon {@code on inventory click} when sneaking.
     * <p>
     * Taking {@code out of} the main-hand -> discharge
     * <br>
     * Placing {@code into} the main-hand -> charge
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void handleWeaponChargeDischargeOnInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player) || !player.isSneaking()) return;
        //System.err.println("1. Player clicked. Player is sneaking.");

        if (event.getClickedInventory() == null) return;
        //System.err.println("2. Click inside inventory, not outside.");

        final PlayerInventory playerInventory = player.getInventory();
        final int mainHandSlot = playerInventory.getHeldItemSlot();

        if (event.getClick() == ClickType.NUMBER_KEY) {
            //System.err.println("3. NUMBER_KEY click");

            final int hotbarSlot = event.getHotbarButton();
            if (hotbarSlot != mainHandSlot) return;
            //System.err.println("4. NUMBER_KEY slot == MAIN_HAND slot");

            ServerUtils.dischargeIfCrossbowWeapon(playerInventory.getItem(hotbarSlot));
            ServerUtils.chargeIfCrossbowWeapon(event.getCurrentItem());
            return;
        }

        if (event.getSlot() == mainHandSlot) {
            //System.err.println("3. Direct click on MAIN_HAND slot.");

            ServerUtils.dischargeIfCrossbowWeapon(event.getCurrentItem());
            ServerUtils.chargeIfCrossbowWeapon(event.getCursor());
        }
    }

    /**
     * Cancel the default {@code melee} atack damage and instead invoke the weapon's LMB behavior.
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void handleMeleeAttackOnEntityDamageByEntity(EntityDamageByEntityEvent event) {
        //System.err.println("===============================");
        //System.err.println("0. EntityDamageByEntityEvent.");

        if (!(event.getDamager() instanceof Player damager)) return;
        //System.err.println("1. Damager is a Player.");

        final EntityDamageEvent.DamageCause cause = event.getCause();
        //System.err.println("2. Damage cause: " + cause);

        if (cause != EntityDamageEvent.DamageCause.ENTITY_ATTACK &&
            cause != EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK)
            return;

        //System.err.println("3. MainHand item: " + damager.getInventory().getItemInMainHand().getType());
        final Weapon weapon = getWeaponOrNull(damager.getInventory().getItemInMainHand());
        if (weapon != null && !CurrentHitTracker.isAlreadyHit(damager.getUniqueId(), event.getEntity().getUniqueId())) {
            //System.err.println("4. MainHand item is a Weapon. Cancel event. Call onLMB.");
            event.setCancelled(true);

            // Runs on the next tick to prevent PlayerDeathEvent being called twice
            // since onLMB may also trigger EntityDamageByEntityEvent
            // (Temporal workaround, until we find a better solution without scheduling)
            Bukkit.getScheduler().runTask(
                BulletCore.instance(),
                () -> {
                    if (!damager.isOnline() || damager.isDead()) return;

                    final ItemStack currentWeapon = damager.getInventory().getItemInMainHand();
                    if (weapon.isThisWeapon(currentWeapon))
                        weapon.onLMB(damager, currentWeapon);
                }
            );
        }
    }

    /**
     * Applies custom durability damage to shields and armor when a player is hit by a custom weapon.
     * <p>
     * If the incoming damage is blocked by a shield, the default shield durability loss is replaced
     * with a weapon-defined shield damage value.
     * <p>
     * If the hit is <b>not</b> blocked by a shield, and an {@link ArmorHit} is registered for the victim,
     * custom armor durability damage is applied.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void damageShieldAndArmorOnEntityDamageByEntity(EntityDamageByEntityEvent event) {
        //System.err.println("===============================");
        //System.err.println("0. EntityDamageByEntityEvent (shield, armor damage).");

        if (!(event.getDamager() instanceof Player damager && event.getEntity() instanceof Player victim)) return;
        //System.err.println("1. Damager and Victim are Players.");

        final CurrentHit currentHit = CurrentHitTracker.getCurrentHit(damager.getUniqueId(), victim.getUniqueId());
        if (currentHit == null) return;
        //System.err.println("2. Damager " + damager.getName() + " damaging victim " + victim.getName());

        @SuppressWarnings("deprecation") final var shieldDamageModifier = EntityDamageEvent.DamageModifier.BLOCKING;
        final double damageBlockedByShield = event.getDamage(shieldDamageModifier);
        final boolean wasAnyDamageBlockedByShield = damageBlockedByShield != 0.0D;
        //System.err.println("3. Original damage blocked by shield: " + damageBlockedByShield);

        final double customShieldDamage = currentHit.weapon().damage.shield();

        if (wasAnyDamageBlockedByShield) {
            //System.err.println("4.1. Shield block. Using value from weapon to damage shield: " + customShieldDamage);
            event.setDamage(shieldDamageModifier, -customShieldDamage);
        }

        final ArmorHit armorHit = CurrentHitTracker.getArmorHit(victim.getUniqueId());
        if (armorHit != null) {
            //System.err.println("4.2. Try damaging armor. Blocked damage: " + (wasAnyDamageBlockedByShield ? customShieldDamage : 0.0D));
            armorHit.applyArmorDamage(wasAnyDamageBlockedByShield ? customShieldDamage : 0.0D);
        }
    }

    /**
     * Suppress arm swing animation when holding a weapon.
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void suppressWeaponSwingOnPlayerAnimation(PlayerAnimationEvent event) {
        //System.err.println("===============================");
        //System.err.println("0. PlayerAnimationEvent.");

        //System.err.println("1. MainHand item: " + event.getPlayer().getInventory().getItemInMainHand().getType());
        if (isWeapon(event.getPlayer().getInventory().getItemInMainHand())) {
            //System.err.println("2. MainHand item is a Weapon. Cancel event.");
            event.setCancelled(true);
        }
    }

    /**
     * Handle player toggle sneak while holding a weapon.
     * <ul>
     *   <li>Start or stop automatic shooting depending on sneak state</li>
     *   <li>Charge or discharge weapon if crossbow</li>
     * </ul>
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
        //System.err.println("===============================");
        //System.err.println("0. PlayerToggleSneakEvent.");

        final Player player = event.getPlayer();
        final ItemStack mainHandItem = player.getInventory().getItemInMainHand();
        final Weapon weapon = getWeaponOrNull(mainHandItem);
        if (weapon == null) return;
        //System.err.println("1. Player has Weapon in MainHand.");

        final boolean isReallySneaking = event.isSneaking() && !player.isInsideVehicle();
        if (isReallySneaking) {
            ShootingHandler.tryAutoShootOnToggleSneak(player, weapon);
        } else {
            ShootingHandler.cancelAutoShooting(player);
        }

        ServerUtils.chargeOrDischargeIfCrossbowMeta(mainHandItem, isReallySneaking);
    }

    /**
     * Update states every server tick.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void updateStatesOnServerTickEnd(ServerTickEndEvent event) {
        SprayHandler.tick();
    }
}