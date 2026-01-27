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
import org.dredd.bulletcore.models.weapons.shooting.spray.SprayHandler;
import org.dredd.bulletcore.utils.ServerUtils;

// TODO: verify whether ignoreCancelled should be applied to some/all events

/**
 * Listens for the player-related events and updates gameplay trackers accordingly.
 *
 * @author dredd
 * @since 1.0.0
 */
public enum PlayerActionsListener implements Listener {

    INSTANCE;

    // ----------< Interactions >----------

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        PlayerActionTracker.recordInventoryInteraction(event.getWhoClicked().getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryDrag(InventoryDragEvent event) {
        PlayerActionTracker.recordInventoryInteraction(event.getWhoClicked().getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        PlayerActionTracker.recordDrop(event.getPlayer().getUniqueId());
    }

    // ----------< Lifecycle >----------

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();

        SprayHandler.getSprayContext(player);

        ServerUtils.chargeOrDischargeIfCrossbowWeapon(player.getInventory().getItemInMainHand(), player.isSneaking());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        final Player player = event.getPlayer();

        PlayerActionTracker.clear(player.getUniqueId());

        ReloadHandler.cancelReload(player, false);
        ShootingHandler.cancelAutoShooting(player);
        RecoilHandler.cancelAndRemoveRecoil(player);
        SprayHandler.clearSprayContext(player);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event) {
        final Player player = event.getEntity();

        ServerUtils.dischargeIfCrossbowWeapon(player.getInventory().getItemInMainHand());

        ReloadHandler.cancelReload(player, false);
        ShootingHandler.cancelAutoShooting(player);
        RecoilHandler.cancelAndRemoveRecoil(player);
    }
}