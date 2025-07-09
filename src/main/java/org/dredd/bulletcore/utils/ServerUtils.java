package org.dredd.bulletcore.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

/**
 * Utility class for retrieving information about the current server state.
 *
 * @since 1.0.0
 */
public final class ServerUtils {

    /**
     * Private constructor to prevent instantiation.
     */
    private ServerUtils() {}

    /**
     * An empty immutable list.
     */
    public static final List<String> EMPTY_LIST = Collections.emptyList();

    /**
     * @return a list of player names currently online
     */
    public static List<String> getOnlinePlayerNames() {
        return Bukkit.getOnlinePlayers().stream()
            .map(Player::getName)
            .toList();
    }
}