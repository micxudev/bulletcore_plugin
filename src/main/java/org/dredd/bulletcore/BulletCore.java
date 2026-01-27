package org.dredd.bulletcore;

import org.bukkit.event.Listener;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.dredd.bulletcore.commands.CommandHandler;
import org.dredd.bulletcore.config.ConfigManager;
import org.dredd.bulletcore.config.materials.MaterialsManager;
import org.dredd.bulletcore.config.messages.component.MessageManager;
import org.dredd.bulletcore.config.messages.translatable.StylesManager;
import org.dredd.bulletcore.custom_item_manager.registries.CustomItemsRegistry;
import org.dredd.bulletcore.listeners.CustomBaseListener;
import org.dredd.bulletcore.listeners.PlayerActionsListener;
import org.dredd.bulletcore.listeners.UnknownCommandListener;
import org.dredd.bulletcore.listeners.WeaponListener;
import org.dredd.bulletcore.models.CustomItemType;
import org.dredd.bulletcore.models.weapons.reloading.ReloadHandler;
import org.dredd.bulletcore.models.weapons.shooting.ShootingHandler;
import org.dredd.bulletcore.models.weapons.shooting.recoil.RecoilHandler;
import org.dredd.bulletcore.models.weapons.skins.SkinsManager;
import org.dredd.bulletcore.utils.JsonUtils;
import org.jetbrains.annotations.NotNull;

/**
 * Main plugin class.
 *
 * @author dredd
 * @since 1.0.0
 */
public final class BulletCore extends JavaPlugin {

    // ----------< Static >----------

    /**
     * Singleton instance of the plugin.
     */
    private static BulletCore plugin;

    // -----< Initialization & Lifecycle >-----

    /**
     * Initializes and loads all the necessary parts of the plugin.<br>
     * This method is also used on plugin reload.
     */
    public static void init(@NotNull BulletCore plugin) {
        BulletCore.cancelAndClear();

        SkinsManager.load(plugin);
        MessageManager.load(plugin);
        StylesManager.load(plugin);
        ConfigManager.load(plugin);
        MaterialsManager.load(plugin);
        CustomItemType.load(plugin);
    }

    /**
     * Cancels current running tasks and clears all registries.<br>
     * This method is used on plugin reload and disable.
     */
    private static void cancelAndClear() {
        ReloadHandler.cancelAllReloadTasks();
        ShootingHandler.cancelAllAutoShootingTasks();
        RecoilHandler.cancelAllRecoilTasks();
        CustomItemsRegistry.clearAllItems();
    }

    // -----< Access Utilities >-----

    public static BulletCore instance() {
        return plugin;
    }

    public static void logInfo(String msg) {
        plugin.getLogger().info(msg);
    }

    public static void logError(String msg) {
        plugin.getLogger().severe(msg);
    }


    // ----------< Instance >----------

    // -----< Lifecycle >-----

    @Override
    public void onLoad() {
        plugin = this;
    }

    @Override
    public void onEnable() {
        logInfo("==========================< BulletCore >==========================");

        CommandHandler.init(this);
        BulletCore.init(this);

        registerListener(CustomBaseListener.INSTANCE);
        registerListener(WeaponListener.INSTANCE);
        registerListener(PlayerActionsListener.INSTANCE);
        registerListener(UnknownCommandListener.INSTANCE);

        logInfo("==================================================================");
    }

    @Override
    public void onDisable() {
        CommandHandler.destroy();
        JsonUtils.shutdownSaveExecutor();
        BulletCore.cancelAndClear();
        plugin = null;
    }

    // -----< Utilities >-----

    /**
     * Registers a listener.
     *
     * @param listener the {@link Listener} to register
     */
    private void registerListener(@NotNull Listener listener) {
        getServer().getPluginManager().registerEvents(listener, this);
    }

    /**
     * Registers a permission if it doesn't already exist.
     *
     * @param perm permission to register
     */
    public void registerPermission(@NotNull String perm) {
        final Permission permission = new Permission(perm);
        final PluginManager pluginManager = getServer().getPluginManager();
        if (pluginManager.getPermission(permission.getName()) == null)
            pluginManager.addPermission(permission);
    }
}