package org.dredd.bulletcore.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.dredd.bulletcore.listeners.trackers.PlayerActionTracker;
import org.dredd.bulletcore.models.weapons.reloading.ReloadHandler;
import org.dredd.bulletcore.models.weapons.shooting.ShootingHandler;
import org.dredd.bulletcore.models.weapons.shooting.recoil.RecoilHandler;
import org.dredd.bulletcore.models.weapons.shooting.spray.PlayerSprayContext;
import org.dredd.bulletcore.models.weapons.shooting.spray.SprayHandler;
import org.dredd.bulletcore.utils.ServerUtils;

/**
 * Listens to player-related events and updates gameplay trackers accordingly.
 *
 * @author dredd
 * @since 1.0.0
 */
public enum PlayerActionsListener implements Listener {

    INSTANCE;

    // ----------< Interactions >----------

    /**
     * Called when a player clicks in an inventory.<br>
     * Updates the player's last inventory interaction time.
     *
     * @param event the {@link InventoryClickEvent} triggered
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryInteract(InventoryClickEvent event) {
        PlayerActionTracker.recordInventoryInteraction(event.getWhoClicked().getUniqueId());
    }

    /**
     * Called when a player drags items in an inventory.<br>
     * Updates the player's last inventory interaction time.
     *
     * @param event the {@link InventoryDragEvent} triggered
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryDrag(InventoryDragEvent event) {
        PlayerActionTracker.recordInventoryInteraction(event.getWhoClicked().getUniqueId());
    }

    /**
     * Called when a player drops an item.<br>
     * Updates the player's last drop time.
     *
     * @param event the {@link PlayerDropItemEvent} triggered
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onDrop(PlayerDropItemEvent event) {
        PlayerActionTracker.recordDrop(event.getPlayer().getUniqueId());
    }


    // ----------< Lifecycle >----------

    /**
     * Called when a player joins the server.<br>
     * Creates a new {@link PlayerSprayContext} instance for the player to track their state.
     *
     * @param event the {@link PlayerJoinEvent} triggered
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();

        SprayHandler.getSprayContext(player);

        ServerUtils.chargeOrDischargeIfWeapon(player.getInventory().getItemInMainHand(), player.isSneaking());
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

        PlayerActionTracker.clear(player.getUniqueId());

        ReloadHandler.cancelReload(player, false);
        ShootingHandler.cancelAutoShooting(player);
        RecoilHandler.cancelAndRemoveRecoil(player);
        SprayHandler.clearSprayContext(player);
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
        final Player player = event.getEntity();

        ServerUtils.dischargeIfWeapon(player.getInventory().getItemInMainHand());

        ReloadHandler.cancelReload(player, false);
        ShootingHandler.cancelAutoShooting(player);
        RecoilHandler.cancelAndRemoveRecoil(player);
    }
}