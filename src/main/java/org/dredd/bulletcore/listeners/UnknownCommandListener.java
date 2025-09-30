package org.dredd.bulletcore.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.command.UnknownCommandEvent;

import java.util.Map;

import static org.dredd.bulletcore.config.messages.component.ComponentMessage.COMMAND_UNKNOWN;

/**
 * Listens for unknown commands; sends a formatted message to the sender.
 *
 * @author dredd
 * @since 1.0.0
 */
public class UnknownCommandListener implements Listener {

    /**
     * Called when an unknown command is executed.
     *
     * @param event the {@link UnknownCommandEvent} triggered
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onUnknownCommand(UnknownCommandEvent event) {
        event.message(COMMAND_UNKNOWN.toComponent(event.getSender(), Map.of("commandline", event.getCommandLine())));
    }
}