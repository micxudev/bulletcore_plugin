package org.dredd.bulletcore.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.dredd.bulletcore.custom_item_manager.registries.CustomItemsRegistry;
import org.dredd.bulletcore.models.CustomBase;

/**
 * The main listener for the plugin.
 *
 * @author dredd
 * @since 1.0.0
 */
@SuppressWarnings("unused")
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
     * Handles player interaction events to detect and respond to clicks involving custom items.
     * <p>
     * If the interacted item is a {@link CustomBase} and defines a custom left or right click action,
     * this method will trigger the appropriate method and cancel the event if necessary.
     *
     * @param event the {@link PlayerInteractEvent} triggered when a player interacts with the world
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerInteract(final PlayerInteractEvent event) {
        /* (Called once for each hand) */
        //System.err.println("===============================");
        //System.err.println("0. PlayerInteractEvent.");

        final ItemStack usedItem = event.getItem();
        if (usedItem == null) return;
        //System.err.println("1. Interact item is not null");

        final CustomBase usedCustomItem = CustomItemsRegistry.getItemOrNull(usedItem);
        if (usedCustomItem == null) return;
        //System.err.println("2. Custom item found. Name: " + usedCustomItem.name);

        final Action action = event.getAction();
        if (action.isLeftClick()) {
            //System.err.println("3. Left click detected.");
            if (usedCustomItem.onLMB(event.getPlayer(), usedItem)) {
                //System.err.println("4. Left click canceled event.");
                event.setCancelled(true);
            }
        } else if (action.isRightClick()) {
            //System.err.println("3. Right click detected.");
            if (usedCustomItem.onRMB(event.getPlayer(), usedItem)) {
                //System.err.println("4. Right click canceled event.");
                event.setCancelled(true);
            }
        }
    }

    /**
     * Handles inventory click events to prevent custom weapons from being placed into the off-hand slot.
     * <p>
     * This includes cases where the off-hand is accessed via direct clicking, cursor movement, hotbar swaps,
     * or the off-hand swap click type. If any attempt is made to move a weapon into the off-hand,
     * the event will be canceled.
     *
     * @param event the {@link InventoryClickEvent} triggered when a player clicks inside an inventory
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onInventoryClick(final InventoryClickEvent event) {
        //System.err.println("===============================");
        //System.err.println("0. InventoryClickEvent.");

        if (event.isCancelled()) return;
        //System.err.println("1. Event is not canceled.");

        if (!(event.getClickedInventory() instanceof PlayerInventory playerInventory)) return;
        //System.err.println("2. Click is inside player inventory.");

        if (event.getClick() == ClickType.SWAP_OFFHAND) {
            //System.err.println("3.1. Current item during Swap: " + (event.getCurrentItem() != null ? event.getCurrentItem().getType() : "null"));
            if (CustomItemsRegistry.isWeapon(event.getCurrentItem())) {
                //System.err.println("4. Current item during Swap is a Weapon. Canceled event.");
                event.setCancelled(true);
                return;
            }
        }

        //System.err.println("3.2. Clicked (converted) slot: " + event.getSlot());
        if (event.getSlot() != CONVERTED_OFFHAND_SLOT) return;
        //System.err.println("4. Slot is off-hand: " + event.getSlot());

        //System.err.println("5. Cursor item: " + event.getCursor().getType());
        if (CustomItemsRegistry.isWeapon(event.getCursor())) {
            //System.err.println("6. Cursor item is a Weapon. Canceled event.");
            event.setCancelled(true);
            event.getView().setCursor(event.getCursor());
            return;
        }

        if (event.getClick() == ClickType.NUMBER_KEY) {
            final ItemStack hotbarItem = playerInventory.getItem(event.getHotbarButton());
            //System.err.println("6. Hotbar item: " + (hotbarItem != null ? hotbarItem.getType() : "null"));
            if (CustomItemsRegistry.isWeapon(hotbarItem)) {
                //System.err.println("7. Hotbar item is a Weapon. Canceled event.");
                event.setCancelled(true);
                return;
            }
        }

        //System.err.println("7. Current item: " + (event.getCurrentItem() != null ? event.getCurrentItem().getType() : "null"));
        if (CustomItemsRegistry.isWeapon(event.getCurrentItem())) {
            //System.err.println("8. Current item is a Weapon. Canceled event.");
            event.setCancelled(true);
        }
    }

    /**
     * Handles hand-swap events (F key by default) to prevent players from swapping weapons
     * between the main hand and off-hand.
     *
     * @param event the {@link PlayerSwapHandItemsEvent} triggered when a player swaps items between hands
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onSwapHandItems(PlayerSwapHandItemsEvent event) {
        //System.err.println("===============================");
        //System.err.println("0. PlayerSwapHandItemsEvent.");

        //System.err.println("1. OffHand item: " + event.getOffHandItem().getType());
        if (CustomItemsRegistry.isWeapon(event.getOffHandItem())) {
            //System.err.println("2. OffHand item is a Weapon. Canceled event.");
            event.setCancelled(true);
            return;
        }

        //System.err.println("2. MainHand item: " + event.getMainHandItem().getType());
        if (CustomItemsRegistry.isWeapon(event.getMainHandItem())) {
            //System.err.println("3. MainHand item is a Weapon. Canceled event.");
            event.setCancelled(true);
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
        if (CustomItemsRegistry.isWeapon(draggedItem)) {
            //System.err.println("4. Off-hand slot item is a Weapon. Canceled event.");
            event.setCancelled(true);
        }
    }
}