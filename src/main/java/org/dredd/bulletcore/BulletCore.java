package org.dredd.bulletcore;

import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabExecutor;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.dredd.bulletcore.commands.CommandHandler;
import org.dredd.bulletcore.config.ConfigManager;
import org.dredd.bulletcore.config.YMLLModelLoader;
import org.dredd.bulletcore.config.messages.MessageManager;
import org.dredd.bulletcore.config.messages.StylesManager;
import org.dredd.bulletcore.custom_item_manager.registries.CustomItemsRegistry;
import org.dredd.bulletcore.listeners.BulletCoreListener;
import org.dredd.bulletcore.listeners.PlayerActionsListener;
import org.dredd.bulletcore.listeners.UnknownCommandListener;
import org.dredd.bulletcore.listeners.trackers.PlayerActionTracker;
import org.dredd.bulletcore.models.weapons.reloading.ReloadHandler;
import org.dredd.bulletcore.models.weapons.reloading.ReloadManager;
import org.dredd.bulletcore.models.weapons.shooting.ShootingHandler;
import org.dredd.bulletcore.models.weapons.skins.SkinsManager;
import org.dredd.bulletcore.utils.JsonUtils;

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
        return plugin;
    }

    @Override
    public void onLoad() {
        plugin = this;
    }

    @Override
    public void onEnable() {
        plugin.getLogger().info("==========================< BulletCore >==========================");

        initAll();
        registerCommand(MAIN_COMMAND_NAME, new CommandHandler());

        PlayerActionTracker tracker = new PlayerActionTracker();
        registerListener(new BulletCoreListener(tracker));
        registerListener(new PlayerActionsListener(tracker));
        registerListener(new UnknownCommandListener());

        plugin.getLogger().info("Version: " + getPluginMeta().getVersion() + " - Plugin Enabled");
        plugin.getLogger().info("==================================================================");
    }

    /**
     * Initializes and loads all the necessary parts of the plugin.<br>
     * This method is also used to reload the plugin.
     */
    public static void initAll() {
        SkinsManager.load();
        MessageManager.reload(plugin);
        StylesManager.reload(plugin);
        ConfigManager.reload(plugin);
        CustomItemsRegistry.clearAll();
        ReloadManager.initAll();
        ShootingHandler.clearAllAutoShootingTasks();
        YMLLModelLoader.loadAllItems(plugin);
    }

    @Override
    public void onDisable() {
        plugin.getLogger().info("==========================< BulletCore >==========================");

        JsonUtils.shutdownSaveExecutor();
        ReloadHandler.clearAllReloadTasks();
        ShootingHandler.clearAllAutoShootingTasks();
        CustomItemsRegistry.clearAll();

        plugin.getLogger().info("Version: " + getPluginMeta().getVersion() + " - Plugin Disabled");
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
     */
    private void registerListener(Listener listener) {
        Bukkit.getPluginManager().registerEvents(listener, this);
    }
}