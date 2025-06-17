package org.dredd.bulletcore;

import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabExecutor;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.dredd.bulletcore.commands.CommandHandler;
import org.dredd.bulletcore.config.ConfigManager;
import org.dredd.bulletcore.config.YMLLModelLoader;
import org.dredd.bulletcore.config.messages.MessageManager;
import org.dredd.bulletcore.custom_item_manager.registries.CustomItemsRegistry;
import org.dredd.bulletcore.listeners.BulletCoreListener;

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
     * The plugin version.
     */
    private final String version;

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
        version = this.getPluginMeta().getVersion();
    }

    /**
     * Called by Bukkit when the plugin is enabled.
     * <p>Performs initialization and registers commands.
     */
    @Override
    public void onEnable() {
        plugin.getLogger().info("==========================< BulletCore >==========================");

        MessageManager.reload(this);
        ConfigManager.reload(this);
        YMLLModelLoader.loadAllItems(this);
        registerCommand(MAIN_COMMAND_NAME, new CommandHandler());
        registerListener(new BulletCoreListener(), this);

        plugin.getLogger().info("Version: " + version + " - Plugin Enabled");
        plugin.getLogger().info("==================================================================");
    }

    /**
     * Called by Bukkit when the plugin is disabled.
     * <p>Performs cleanup and unregisters commands.
     */
    @Override
    public void onDisable() {
        plugin.getLogger().info("==========================< BulletCore >==========================");

        CustomItemsRegistry.clearAll();

        plugin.getLogger().info("Version: " + version + " - Plugin Disabled");
        plugin.getLogger().info("==================================================================");
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

    /**
     * Registers a listener.
     *
     * @param listener the {@link Listener} to register
     * @param plugin   the {@link Plugin} that owns the listener
     */
    private void registerListener(Listener listener, Plugin plugin) {
        Bukkit.getPluginManager().registerEvents(listener, plugin);
    }
}