package org.dredd.bulletcore.listeners;

import io.papermc.paper.event.player.PlayerArmSwingEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.dredd.bulletcore.custom_item_manager.registries.CustomItemsRegistry;
import org.dredd.bulletcore.listeners.trackers.PlayerActionTracker;
import org.dredd.bulletcore.models.CustomBase;

/**
 * Listens for the specific interaction events related to items representing {@link CustomBase}.
 *
 * @author dredd
 * @since 1.0.0
 */
public enum CustomBaseListener implements Listener {

    INSTANCE;

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        // Called once for each hand, do not process off-hand
        if (event.getHand() != EquipmentSlot.HAND) return;

        final Player player = event.getPlayer();

        final ItemStack usedItem = event.getItem();
        if (usedItem == null) return;

        final CustomBase usedCustomItem = CustomItemsRegistry.getItemOrNull(usedItem);
        if (usedCustomItem == null) return;

        final Action action = event.getAction();
        if (action.isLeftClick()) {
            event.setCancelled(true);
        } else if (action.isRightClick()) {
            if (usedCustomItem.onRMB(player, usedItem))
                event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerArmSwing(PlayerArmSwingEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;

        final Player player = event.getPlayer();

        // If less than 25 ms passed since LastDrop, assume this event caused by using drop key (Q)
        // (due to PlayerDropItemEvent being fired right before this event)
        final long now = System.currentTimeMillis();
        final long lastDrop = PlayerActionTracker.getLastDrop(player.getUniqueId());
        if (now - lastDrop < 25L) return;

        final ItemStack mainHandItem = player.getInventory().getItemInMainHand();

        final CustomBase usedCustomItem = CustomItemsRegistry.getItemOrNull(mainHandItem);
        if (usedCustomItem == null) return;

        if (usedCustomItem.onLMB(player, mainHandItem))
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerItemHeld(PlayerItemHeldEvent event) {
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

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerSwapHandItems(PlayerSwapHandItemsEvent event) {
        final Player player = event.getPlayer();

        // event.getMainHandItem() = will be a NEW main-hand item (if the event is not cancelled) (not the current one)
        // event.getOffHandItem()  = will be a NEW off-hand  item (if the event is not cancelled) (not the current one)
        final ItemStack currentOff = event.getMainHandItem();
        final ItemStack currentMain = event.getOffHandItem();

        // Called on item moving from main-hand → off-hand
        final CustomBase currentMainCustom = CustomItemsRegistry.getItemOrNull(currentMain);
        if (currentMainCustom != null && currentMainCustom.onSwapFromMainToOff(player, currentMain, currentOff))
            event.setCancelled(true);

        // Called on item moving from off-hand → main-hand
        final CustomBase currentOffCustom = CustomItemsRegistry.getItemOrNull(currentOff);
        if (currentOffCustom != null && currentOffCustom.onSwapFromOffToMain(player, currentMain, currentOff))
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        final ItemStack droppedItem = event.getItemDrop().getItemStack();
        final CustomBase droppedCustomItem = CustomItemsRegistry.getItemOrNull(droppedItem);
        if (droppedCustomItem == null) return;

        final Player player = event.getPlayer();

        // If less than 50 ms passed since LastInventoryInteraction, assume it came from GUI
        // (due to InventoryClickEvent being fired right before this event)
        final long now = System.currentTimeMillis();
        final long last = PlayerActionTracker.getLastInventoryInteraction(player.getUniqueId());
        final boolean isFromGui = now - last < 50L;

        if (droppedCustomItem.onDropItem(player, droppedItem, isFromGui))
            event.setCancelled(true);
    }
}