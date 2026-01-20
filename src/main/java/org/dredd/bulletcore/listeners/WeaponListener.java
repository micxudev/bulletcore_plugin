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
import org.dredd.bulletcore.config.ConfigManager;
import org.dredd.bulletcore.custom_item_manager.registries.CustomItemsRegistry;
import org.dredd.bulletcore.listeners.trackers.CurrentHitTracker;
import org.dredd.bulletcore.listeners.trackers.PlayerActionTracker;
import org.dredd.bulletcore.models.armor.Armor;
import org.dredd.bulletcore.models.armor.ArmorHit;
import org.dredd.bulletcore.models.weapons.Weapon;
import org.dredd.bulletcore.models.weapons.shooting.ShootingHandler;
import org.dredd.bulletcore.models.weapons.shooting.spray.SprayHandler;
import org.dredd.bulletcore.utils.ServerUtils;

import static org.dredd.bulletcore.custom_item_manager.registries.CustomItemsRegistry.getWeaponOrNull;
import static org.dredd.bulletcore.custom_item_manager.registries.CustomItemsRegistry.isWeapon;

// TODO: clean docs, rename methods

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

    // ----------< Weapon-Specific Events >----------

    /**
     * Prevent placing Weapon into the off-hand slot.
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
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
     * Handles inventory clicks to charge or discharge weapons when sneaking.
     * <p>
     * Taking an item out of the main-hand -> discharge<br>
     * Placing an item into the main-hand -> charge
     *
     * @param event the {@link InventoryClickEvent} triggered when a player clicks inside an inventory
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void dischargeWeaponOnInteract(InventoryClickEvent event) {
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

            ServerUtils.dischargeIfWeapon(playerInventory.getItem(hotbarSlot));
            ServerUtils.chargeIfWeapon(event.getCurrentItem());
            return;
        }

        if (event.getSlot() == mainHandSlot) {
            //System.err.println("3. Direct click on MAIN_HAND slot.");

            ServerUtils.dischargeIfWeapon(event.getCurrentItem());
            ServerUtils.chargeIfWeapon(event.getCursor());
        }
    }

    /**
     * Handles inventory drag events to prevent players from dragging custom weapons into the off-hand slot.
     *
     * @param event the {@link InventoryDragEvent} triggered when a player drags items in an inventory
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onDrag(InventoryDragEvent event) {
        //System.err.println("===============================");
        //System.err.println("0. InventoryDragEvent.");

        if (event.getView().getTopInventory().getType() != InventoryType.CRAFTING) return;
        //System.err.println("1. Top inventory is player inventory (no external container open).");

        if (!event.getRawSlots().contains(RAW_OFFHAND_SLOT)) return;
        //System.err.println("2. Drag includes off-hand slot.");

        final ItemStack draggedItem = event.getNewItems().get(RAW_OFFHAND_SLOT);
        //System.err.println("3. Dragged item into off-hand slot: " + draggedItem.getType());
        if (isWeapon(draggedItem)) {
            //System.err.println("4. Off-hand slot item is a Weapon. Canceled event.");
            event.setCancelled(true);
        }
    }

    /**
     * Handles entity damage events to intercept direct player attacks with custom weapons.
     * <p>
     * If a player damages another entity using a weapon registered in the {@link CustomItemsRegistry},
     * this method cancels the default damage event and instead invokes the weapon's left-click behavior
     * via {@link Weapon#onLMB(Player, ItemStack)}.
     *
     * @param event the {@link EntityDamageByEntityEvent} triggered when an entity is damaged by another entity
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
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
                BulletCore.instance(), () -> {
                    if (!damager.isOnline() || damager.isDead()) return;

                    final ItemStack currentWeapon = damager.getInventory().getItemInMainHand();
                    if (weapon.isThisWeapon(currentWeapon))
                        weapon.onLMB(damager, currentWeapon);
                }
            );
        }
    }

    /**
     * Handles entity damage events to damage the {@link Armor}
     *
     * @param event the {@link EntityDamageByEntityEvent} triggered when an entity is damaged by another entity
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void damageArmor(EntityDamageByEntityEvent event) {
        //System.err.println("===============================");
        //System.err.println("0. EntityDamageByEntityEvent (armor damage).");

        if (!(event.getDamager() instanceof Player && event.getEntity() instanceof Player victim)) return;
        //System.err.println("1. Damager and Victim are Players.");

        final ArmorHit armorHit = CurrentHitTracker.getArmorHit(victim.getUniqueId());
        if (armorHit != null) {
            //System.err.println("2. Damaging armor.");
            armorHit.run();
        }
    }

    /**
     * Handles player animation events (e.g., arm swinging) to suppress animation when holding a custom weapon.
     * <p>
     * This is used to prevent visual feedback (like swing animations) when using weapons that override
     * default behavior, ensuring consistent interaction logic and visuals.
     *
     * @param event the {@link PlayerAnimationEvent} triggered when a player performs an animation
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onAnimation(PlayerAnimationEvent event) {
        //System.err.println("===============================");
        //System.err.println("0. PlayerAnimationEvent.");

        //System.err.println("1. MainHand item: " + event.getPlayer().getInventory().getItemInMainHand().getType());
        if (isWeapon(event.getPlayer().getInventory().getItemInMainHand())) {
            //System.err.println("2. MainHand item is a Weapon. Cancel event.");
            event.setCancelled(true);
        }
    }

    /**
     * Handles player sneaking to simulate charging and discharging a custom crossbow weapon.<br>
     * As well as cancel automatic shooting when the player stops sneaking.
     * <p>
     * When a player begins sneaking while holding a valid custom crossbow weapon,
     * an arrow is visually charged into it. When the player stops sneaking,
     * the crossbow is visually discharged.
     *
     * @param event the {@link PlayerToggleSneakEvent} triggered when a player toggles sneaking
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onShift(PlayerToggleSneakEvent event) {
        //System.err.println("===============================");
        //System.err.println("0. PlayerToggleSneakEvent.");

        final Player player = event.getPlayer();
        final ItemStack mainHandItem = player.getInventory().getItemInMainHand();
        final Weapon weapon = getWeaponOrNull(mainHandItem);
        if (weapon == null) return;
        //System.err.println("1. Player has Weapon in MainHand.");

        final boolean isNowSneaking = event.isSneaking();
        if (!isNowSneaking) {
            //System.err.println("2.1. Player is NO MORE sneaking. Cancel auto shooting.");
            ShootingHandler.cancelAutoShooting(player);
        }

        final boolean isReallySneaking = isNowSneaking && !player.isInsideVehicle();

        if (isReallySneaking && weapon.isAutomatic) {
            //System.err.println("2.1. Player is NOW sneaking with automatic Weapon.");
            final long now = System.currentTimeMillis();
            final long lastSingleShot = PlayerActionTracker.getLastSingleShotAutomatic(player.getUniqueId());
            final long threshold = ConfigManager.instance().fireResumeThreshold;
            if (now - lastSingleShot < threshold) {
                //System.err.println("2.1.1. Player shot a single bullet " + (now - lastSingleShot) + "ms ago.");
                ShootingHandler.tryAutoShoot(player, weapon);
            }
        }

        ServerUtils.chargeOrDischargeIfCrossbowMeta(mainHandItem, isReallySneaking);
    }

    // ----------< Server Tick Updates >----------

    /**
     * Called when the server has finished ticking the main loop.<br>
     * Updates player states.
     *
     * @param event the {@link ServerTickEndEvent} triggered
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onTickEnd(ServerTickEndEvent event) {
        SprayHandler.tick();
    }
}