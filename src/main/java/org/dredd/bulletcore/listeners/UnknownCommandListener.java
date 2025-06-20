package org.dredd.bulletcore.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.command.UnknownCommandEvent;

import java.util.Map;

import static org.dredd.bulletcore.config.messages.ComponentMessage.UNKNOWN_COMMAND;
import static org.dredd.bulletcore.config.messages.MessageManager.of;

/**
 * Listens for unknown commands; sends a formatted message to the sender.
 *
 * @author dredd
 * @since 1.0.0
 */
public class UnknownCommandListener implements Listener {

    /**
     * Called when an unknown command is executed.<br>
     *
     * @param event the {@link UnknownCommandEvent} triggered
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onUnknownCommand(UnknownCommandEvent event) {
        event.message(of(event.getSender(), UNKNOWN_COMMAND, Map.of("commandline", event.getCommandLine())));
    }
}