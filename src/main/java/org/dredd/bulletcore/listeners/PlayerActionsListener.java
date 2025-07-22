package org.dredd.bulletcore.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.dredd.bulletcore.listeners.trackers.PlayerActionTracker;
import org.dredd.bulletcore.models.weapons.reloading.ReloadHandler;
import org.dredd.bulletcore.models.weapons.shooting.ShootingHandler;
import org.dredd.bulletcore.models.weapons.shooting.recoil.RecoilHandler;

/**
 * Listens for player actions and records them using {@link PlayerActionTracker}.
 *
 * @author dredd
 * @since 1.0.0
 */
public class PlayerActionsListener implements Listener {

    private final PlayerActionTracker tracker;

    /**
     * Constructs a new listener using the provided tracker instance.
     *
     * @param tracker the {@link PlayerActionTracker} used to store inventory interaction timestamps
     */
    public PlayerActionsListener(PlayerActionTracker tracker) {
        this.tracker = tracker;
    }

    /**
     * Called when a player clicks in an inventory.<br>
     * Updates the player's last inventory interaction time.
     *
     * @param event the {@link InventoryClickEvent} triggered
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryInteract(InventoryClickEvent event) {
        tracker.markInventoryInteraction(event.getWhoClicked().getUniqueId());
    }

    /**
     * Called when a player drags items in an inventory.<br>
     * Updates the player's last inventory interaction time.
     *
     * @param event the {@link InventoryDragEvent} triggered
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryDrag(InventoryDragEvent event) {
        tracker.markInventoryInteraction(event.getWhoClicked().getUniqueId());
    }

    /**
     * Called when a player quits the server.<br>
     * Clears tracking information for the player.
     *
     * @param event the {@link PlayerQuitEvent} triggered
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        final Player player = event.getPlayer();
        tracker.clear(player.getUniqueId());
        ReloadHandler.cancelReload(player, false);
        ShootingHandler.cancelAutoShooting(player);
        RecoilHandler.stopAndClearRecoil(player);
    }

    /**
     * Called when a player dies.<br>
     * Stops the player's current reload if it is in progress.<br>
     * Stops automatic shooting if the player was shooting.<br>
     * Stops and clears recoil data.
     *
     * @param event the {@link PlayerDeathEvent} triggered
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player entity = event.getEntity();
        ReloadHandler.cancelReload(entity, false);
        ShootingHandler.cancelAutoShooting(entity);
        RecoilHandler.stopAndClearRecoil(entity);
    }

    /**
     * Called when a player drops an item.<br>
     * Updates the player's last drop time.
     *
     * @param event the {@link PlayerDropItemEvent} triggered
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onDrop(PlayerDropItemEvent event) {
        tracker.markDrop(event.getPlayer().getUniqueId());
    }
}