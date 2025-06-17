package org.dredd.bulletcore.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.dredd.bulletcore.custom_item_manager.registries.CustomItemsRegistry;
import org.dredd.bulletcore.models.CustomBase;

public class BulletCoreListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onClick(final PlayerInteractEvent event) {
        /* (Called once for each hand) */
        //System.err.println("===============================");
        //System.err.println("0. PlayerInteractEvent Called.");

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
                //System.err.println("4. Left click cancelled the event.");
                event.setCancelled(true);
            }
        } else if (action.isRightClick()) {
            //System.err.println("3. Right click detected.");
            if (usedCustomItem.onRMB(event.getPlayer(), usedItem)) {
                //System.err.println("4. Right click cancelled the event.");
                event.setCancelled(true);
            }
        }
        //System.err.println("===============================\n\n");
    }
}