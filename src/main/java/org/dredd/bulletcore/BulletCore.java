package org.dredd.bulletcore;

import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabExecutor;
import org.bukkit.plugin.java.JavaPlugin;
import org.dredd.bulletcore.commands.CommandHandler;
import org.dredd.bulletcore.config.ConfigManager;
import org.dredd.bulletcore.config.YMLLModelLoader;
import org.dredd.bulletcore.config.messages.MessageManager;

import static org.dredd.bulletcore.commands.CommandHandler.MAIN_COMMAND_NAME;

/**
 * Main plugin class for <b>BulletCore</b>.
 *
 * @author dredd
 * @since 1.0.0
 */
public final class BulletCore extends JavaPlugin {

    /**
     * Singleton instance of the plugin.
     */
    private static BulletCore plugin;

    /**
     * Gets the singleton instance of the plugin.
     *
     * @return the {@code BulletCore} instance
     */
    public static BulletCore getInstance() {
        if (plugin == null)
            throw new IllegalStateException("Attempted to use getInstance() while the plugin is null.");

        return plugin;
    }

    /**
     * Constructs a new {@code BulletCore} instance.
     */
    public BulletCore() {
        plugin = this;
    }

    /**
     * Called by Bukkit when the plugin is enabled.
     * <p>Performs initialization and registers commands.
     */
    @Override
    public void onEnable() {
        MessageManager.reload(this);
        ConfigManager.reload(this);
        YMLLModelLoader.loadAllItems();
        registerCommand(MAIN_COMMAND_NAME, new CommandHandler());
    }

    /**
     * Called by Bukkit when the plugin is disabled.
     * <p>Performs cleanup and unregisters commands.
     */
    @Override
    public void onDisable() {
        plugin = null;
    }

    /**
     * Registers a command and its tab executor.
     *
     * @param label    the name of the command as defined in {@code plugin.yml}
     * @param executor the {@link TabExecutor} responsible for handling the command
     */
    private void registerCommand(String label, TabExecutor executor) {
        PluginCommand command = getCommand(label);
        if (command != null) {
            command.setExecutor(executor);
            command.setTabCompleter(executor);
        } else {
            getLogger().warning("Command '" + label + "' not found in plugin.yml");
        }
    }
}