package org.dredd.bulletcore.listeners;

import java.util.Map;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.command.UnknownCommandEvent;

import static org.dredd.bulletcore.config.messages.component.ComponentMessage.COMMAND_UNKNOWN;

/**
 * Listens for unknown commands events.
 *
 * @author dredd
 * @since 1.0.0
 */
public enum UnknownCommandListener implements Listener {

    INSTANCE;

    /**
     * Called when an unknown command is executed.
     * <p>
     * Sets the event's message to the unknown command message
     * that will be sent to the sender.
     *
     * @param event the {@link UnknownCommandEvent} triggered
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onUnknownCommand(UnknownCommandEvent event) {
        event.message(
            COMMAND_UNKNOWN.toComponent(
                event.getSender(),
                Map.of("commandline", event.getCommandLine())
            )
        );
    }
}