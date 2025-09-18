package org.dredd.bulletcore;

import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
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
import org.dredd.bulletcore.models.weapons.reloading.ReloadHandler;
import org.dredd.bulletcore.models.weapons.reloading.ReloadManager;
import org.dredd.bulletcore.models.weapons.shooting.ShootingHandler;
import org.dredd.bulletcore.models.weapons.shooting.recoil.RecoilHandler;
import org.dredd.bulletcore.models.weapons.skins.SkinsManager;
import org.dredd.bulletcore.utils.JsonUtils;
import org.jetbrains.annotations.NotNull;

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
     * @return the {@link BulletCore} instance or {@code null} if called before the plugin was loaded
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
        getLogger().info("==========================< BulletCore >==========================");

        initAll();
        registerMainCommand();

        registerListener(new BulletCoreListener());
        registerListener(new PlayerActionsListener());
        registerListener(new UnknownCommandListener());

        getLogger().info("Version: " + getPluginMeta().getVersion() + " - Plugin Enabled");
        getLogger().info("==================================================================");
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
        RecoilHandler.stopAndClearAllRecoils();
        YMLLModelLoader.loadAllItems(plugin);
    }

    @Override
    public void onDisable() {
        getLogger().info("==========================< BulletCore >==========================");

        JsonUtils.shutdownSaveExecutor();
        ReloadHandler.clearAllReloadTasks();
        ShootingHandler.clearAllAutoShootingTasks();
        RecoilHandler.stopAndClearAllRecoils();
        CustomItemsRegistry.clearAll();

        getLogger().info("Version: " + getPluginMeta().getVersion() + " - Plugin Disabled");
        getLogger().info("==================================================================");
        plugin = null;
    }

    /**
     * Registers the main plugin command.
     */
    private void registerMainCommand() {
        String name = CommandHandler.MAIN_COMMAND_NAME;
        PluginCommand command = getCommand(name);
        if (command != null) {
            CommandHandler executor = new CommandHandler();
            command.setExecutor(executor);
            command.setTabCompleter(executor);
        } else {
            getLogger().warning("Command '" + name + "' not found in plugin.yml");
        }
    }

    /**
     * Registers a listener.
     *
     * @param listener the {@link Listener} to register
     */
    private void registerListener(@NotNull Listener listener) {
        Bukkit.getPluginManager().registerEvents(listener, this);
    }
}