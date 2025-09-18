package org.dredd.bulletcore.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.dredd.bulletcore.BulletCore;
import org.dredd.bulletcore.config.ConfigManager;
import org.dredd.bulletcore.custom_item_manager.registries.CustomItemsRegistry;
import org.dredd.bulletcore.listeners.trackers.CurrentHitTracker;
import org.dredd.bulletcore.listeners.trackers.PlayerActionTracker;
import org.dredd.bulletcore.models.CustomBase;
import org.dredd.bulletcore.models.armor.Armor;
import org.dredd.bulletcore.models.armor.ArmorHit;
import org.dredd.bulletcore.models.weapons.Weapon;
import org.dredd.bulletcore.models.weapons.reloading.ReloadHandler;
import org.dredd.bulletcore.models.weapons.shooting.ShootingHandler;
import org.dredd.bulletcore.utils.ServerUtils;

import static org.dredd.bulletcore.custom_item_manager.registries.CustomItemsRegistry.*;

/**
 * The main listener for the plugin.
 *
 * @author dredd
 * @since 1.0.0
 */
public class BulletCoreListener implements Listener {
    /* Don't remove commented debug statements; they might become handy any time */

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

    /**
     * The {@link PlayerActionTracker} instance used to track player interactions.
     */
    private final PlayerActionTracker tracker;

    /**
     * Constructs a new listener using the provided tracker instance.
     *
     * @param tracker the {@link PlayerActionTracker} used to track player interactions
     */
    public BulletCoreListener(PlayerActionTracker tracker) {
        this.tracker = tracker;
    }


    /* ========== All Custom Items Listeners ========== */

    /**
     * Handles player interaction events to detect and respond to clicks involving custom items.
     * <p>
     * If the interacted item is a {@link CustomBase} and defines a custom left or right click action,
     * this method will trigger the appropriate method and cancel the event if necessary.
     *
     * @param event the {@link PlayerInteractEvent} triggered when a player interacts with the world
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        /* (Called once for each hand) */
        //System.err.println("===============================");
        //System.err.println("0. PlayerInteractEvent.");
        if (event.getHand() != EquipmentSlot.HAND) return;
        //System.err.println("1. Used MAIN HAND." + " Action: " + event.getAction());

        long now = System.currentTimeMillis();
        long lastDrop = tracker.getLastDrop(event.getPlayer().getUniqueId());

        if (now - lastDrop < 25) {
            //System.err.println("2. Interact is right after the drop (most probably using key (Q)). Do not process.");
            return;
        }

        final ItemStack usedItem = event.getItem();
        if (usedItem == null) return;
        //System.err.println("2. Interact item is not null");

        final CustomBase usedCustomItem = getItemOrNull(usedItem);
        if (usedCustomItem == null) return;
        //System.err.println("3. Custom item found. Name: " + usedCustomItem.name);

        final Action action = event.getAction();
        if (action.isLeftClick()) {
            //System.err.println("4. Left click detected.");
            if (usedCustomItem.onLMB(event.getPlayer(), usedItem)) {
                //System.err.println("5. Left click canceled event.");
                event.setCancelled(true);
            }
        } else if (action.isRightClick()) {
            //System.err.println("4. Right click detected.");
            if (usedCustomItem.onRMB(event.getPlayer(), usedItem)) {
                //System.err.println("5. Right click canceled event.");
                event.setCancelled(true);
            }
        }
    }

    /**
     * Handles item slot changes for custom items.
     *
     * @param event the {@link PlayerItemHeldEvent} triggered when a player changes their selected hotbar slot
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onItemSwap(PlayerItemHeldEvent event) {
        //System.err.println("===============================");
        //System.err.println("0. PlayerItemHeldEvent.");

        final Player player = event.getPlayer();
        final PlayerInventory inventory = player.getInventory();

        final ItemStack prevItem = inventory.getItem(event.getPreviousSlot());
        final CustomBase prevCustomItem = getItemOrNull(prevItem);
        if (prevCustomItem != null && prevCustomItem.onSwapAway(player, prevItem))
            event.setCancelled(true);

        final ItemStack newItem = inventory.getItem(event.getNewSlot());
        final CustomBase newCustomItem = getItemOrNull(newItem);
        if (newCustomItem != null && newCustomItem.onSwapTo(player, newItem))
            event.setCancelled(true);
    }


    /* ========== Weapon-Specific Listeners ========== */

    /**
     * Handles inventory click events to prevent custom weapons from being placed into the off-hand slot.
     * <p>
     * This includes cases where the off-hand is accessed via direct clicking, cursor movement, hotbar swaps,
     * or the off-hand swap click type. If any attempt is made to move a weapon into the off-hand,
     * the event will be canceled.
     *
     * @param event the {@link InventoryClickEvent} triggered when a player clicks inside an inventory
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
     * Handles inventory click events to cancel weapon automatic shooting;<br>
     * or reloading if the player interacts with the inventory slot currently holding the reloading weapon.
     *
     * @param event the {@link InventoryClickEvent} triggered when a player clicks inside an inventory
     */
    @EventHandler(priority = EventPriority.LOW)
    public void cancelActionsOnWeaponInteract(InventoryClickEvent event) {
        //System.err.println("===============================");
        //System.err.println("0. InventoryClickEvent.");

        if (!(event.getWhoClicked() instanceof Player player)) return;
        //System.err.println("1. Player clicked.");

        ShootingHandler.cancelAutoShooting(player);

        if (!ReloadHandler.isReloading(player)) return;
        //System.err.println("2. Player is reloading.");

        // Check if the click is in the player's inventory
        final Inventory clickedInventory = event.getClickedInventory();
        if (clickedInventory == null || !clickedInventory.equals(player.getInventory())) return;
        //System.err.println("3. Click inside player inventory.");

        int heldWeaponSlot = player.getInventory().getHeldItemSlot();
        //System.err.println("4. Held weapon slot: " + heldWeaponSlot);

        if (event.getSlot() == heldWeaponSlot || event.getHotbarButton() == heldWeaponSlot) {
            //System.err.println("5. Click slot: " + event.getSlot() + ". hotbar slot: " + event.getHotbarButton());
            //System.err.println("6. Player used held weapon slot. Cancel reload.");
            ReloadHandler.cancelReload(player, false);
        }
    }

    /**
     * Handles inventory clicks to charge or discharge weapons when sneaking.
     * <p>
     * Taking item out of the main-hand -> discharge<br>
     * Placing item into the main-hand -> charge
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
        int mainHandSlot = playerInventory.getHeldItemSlot();

        if (event.getClick() == ClickType.NUMBER_KEY) {
            //System.err.println("3. NUMBER_KEY click");

            int hotbarSlot = event.getHotbarButton();
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
     * Handles hand-swap events (F key by default) to prevent players from swapping weapons
     * between the main hand and off-hand to prevent placing a weapon into the off-hand slot.
     *
     * @param event the {@link PlayerSwapHandItemsEvent} triggered when a player swaps items between hands
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onSwapHandItems(PlayerSwapHandItemsEvent event) {
        //System.err.println("===============================");
        //System.err.println("0. PlayerSwapHandItemsEvent.");

        //System.err.println("1. OffHand item: " + event.getOffHandItem().getType());
        if (isWeapon(event.getOffHandItem())) {
            //System.err.println("2. OffHand item is a Weapon. Canceled event.");
            event.setCancelled(true);
            return;
        }

        // The code below is only needed if we want to prevent removing a weapon from the off-hand slot.
        // As for now, we never put a weapon to the off-hand slot.
        // But if we ever do (e.g., we might: place scope, fake weapon), we uncomment it.
        //System.err.println("2. MainHand item: " + event.getMainHandItem().getType());
        /*if (isWeapon(event.getMainHandItem())) {
            //System.err.println("3. MainHand item is a Weapon. Canceled event.");
            event.setCancelled(true);
        }*/
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
            Bukkit.getScheduler().runTask(BulletCore.getInstance(), () -> {
                if (!damager.isOnline() || damager.isDead()) return;

                final ItemStack currentWeapon = damager.getInventory().getItemInMainHand();
                if (getWeaponOrNull(currentWeapon) == weapon)
                    weapon.onLMB(damager, currentWeapon);
            });
        }
    }

    /**
     * Handles entity damage events to damage the {@link Armor}
     *
     * @param event the {@link EntityDamageByEntityEvent} triggered when an entity is damaged by another entity
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
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
    @EventHandler(priority = EventPriority.LOWEST)
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
     * Handles item drop events to prevent players from dropping weapons using key (Q)
     *
     * @param event the {@link PlayerDropItemEvent} triggered when a player drops an item
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onItemDrop(PlayerDropItemEvent event) {
        //System.err.println("===============================");
        //System.err.println("0. PlayerDropItemEvent.");

        final ItemStack droppedItem = event.getItemDrop().getItemStack();
        final Weapon weapon = getWeaponOrNull(droppedItem);
        if (weapon == null) return;
        //System.err.println("1. Dropped item is a Weapon.");

        final Player player = event.getPlayer();

        long now = System.currentTimeMillis();
        long last = tracker.getLastInventoryInteraction(player.getUniqueId());

        // If less than 50 ms passed since the last inventory interaction, assume it came from GUI
        if (now - last < 50) {
            //System.err.println("2. Used GUI to drop item. Allow drop.");
            if (droppedItem == player.getInventory().getItemInMainHand()) {
                //System.err.println("3. If the player is reloading, this must be the dropped item. Cancel reload.");
                ReloadHandler.cancelReload(player, false);
            }
            return;
        }

        //System.err.println("2. Used key (Q) to drop item. Cancel drop.");
        event.setCancelled(true);
        weapon.onDrop(player, droppedItem);
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

        boolean isNowSneaking = event.isSneaking();
        if (!isNowSneaking) {
            //System.err.println("2.1. Player is NO MORE sneaking. Cancel auto shooting.");
            ShootingHandler.cancelAutoShooting(player);
        }

        boolean isReallySneaking = isNowSneaking && !player.isInsideVehicle();

        if (isReallySneaking && weapon.isAutomatic) {
            //System.err.println("2.1. Player is NOW sneaking with automatic Weapon.");
            long now = System.currentTimeMillis();
            long lastSingleShot = PlayerActionTracker.getLastSingleShotUsingAutomaticWeapon(player.getUniqueId());
            long threshold = ConfigManager.get().fireResumeThreshold;
            if (now - lastSingleShot < threshold) {
                //System.err.println("2.1.1. Player shot a single bullet " + (now - lastSingleShot) + "ms ago.");
                ShootingHandler.tryAutoShoot(player, weapon);
            }
        }

        ServerUtils.chargeOrDischargeIfCrossbowMeta(mainHandItem, isReallySneaking);
    }

    /**
     * Handles player death to discharge crossbow weapons upon death.
     *
     * @param event the {@link PlayerDeathEvent} triggered when a player dies
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event) {
        ServerUtils.dischargeIfWeapon(event.getPlayer().getInventory().getItemInMainHand());
    }
}