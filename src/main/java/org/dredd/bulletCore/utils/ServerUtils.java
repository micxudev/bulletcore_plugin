package org.dredd.bulletCore.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

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
     * @return a list of player names currently online
     */
    public static List<String> getOnlinePlayerNames() {
        return Bukkit.getOnlinePlayers().stream()
            .map(Player::getName)
            .toList();
    }
}