package org.dredd.bulletcore.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.dredd.bulletcore.custom_item_manager.registries.CustomItemsRegistry;
import org.dredd.bulletcore.listeners.trackers.PlayerActionTracker;
import org.dredd.bulletcore.models.CustomBase;

public enum CustomBaseListener implements Listener {

    INSTANCE;

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

        final Player player = event.getPlayer();
        final long now = System.currentTimeMillis();
        final long lastDrop = PlayerActionTracker.getLastDrop(player.getUniqueId());

        if (now - lastDrop < 25) {
            //System.err.println("2. Interact is right after the drop (most probably using key (Q)). Do not process.");
            return;
        }

        final ItemStack usedItem = event.getItem();
        if (usedItem == null) return;
        //System.err.println("2. Interact item is not null");

        final CustomBase usedCustomItem = CustomItemsRegistry.getItemOrNull(usedItem);
        if (usedCustomItem == null) return;
        //System.err.println("3. Custom item found. Name: " + usedCustomItem.name);

        final Action action = event.getAction();
        if (action.isLeftClick()) {
            //System.err.println("4. Left click detected.");
            if (usedCustomItem.onLMB(player, usedItem)) {
                //System.err.println("5. Left click canceled event.");
                event.setCancelled(true);
            }
        } else if (action.isRightClick()) {
            //System.err.println("4. Right click detected.");
            if (usedCustomItem.onRMB(player, usedItem)) {
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
        final CustomBase prevCustomItem = CustomItemsRegistry.getItemOrNull(prevItem);
        if (prevCustomItem != null && prevCustomItem.onSwapAway(player, prevItem))
            event.setCancelled(true);

        final ItemStack newItem = inventory.getItem(event.getNewSlot());
        final CustomBase newCustomItem = CustomItemsRegistry.getItemOrNull(newItem);
        if (newCustomItem != null && newCustomItem.onSwapTo(player, newItem))
            event.setCancelled(true);
    }
}