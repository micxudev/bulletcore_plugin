package org.dredd.bulletcore.config.messages.component;

import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.dredd.bulletcore.config.ConfigManager;
import org.dredd.bulletcore.utils.ComponentUtils;
import org.dredd.bulletcore.utils.ServerUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Map;

/**
 * Defines server-side component messages used for (e.g., command feedback, errors, and notifications).
 * <p>
 * Messages are localized per {@link CommandSender} and support placeholder substitution.<br>
 * They can be converted into {@link Component} instances via {@link #toComponent(CommandSender, Map)}.
 *
 * @author dredd
 * @since 1.0.0
 */
public enum ComponentMessage {

    // ----------< Enum Fields >----------

    // -----< Command >-----

    /**
     * Shown when a player or console executes a command that is not defined.
     * <p>
     * {@code %commandline%} – the command that was entered
     */
    COMMAND_UNKNOWN("<red>Unknown command <white>%commandline%</white>"),

    /**
     * Shown when a command is executed without specifying a subcommand.
     * <p>
     * {@code %command%} – the main command name
     */
    COMMAND_MISSING_SUBCOMMAND("<red>Missing subcommand. Usage <white>/%command% <subcommand></white>"),

    /**
     * Shown when an unknown subcommand is used.
     * <p>
     * {@code %subcommand%} – the provided subcommand name
     */
    COMMAND_UNKNOWN_SUBCOMMAND("<red>Unknown subcommand <white>%subcommand%</white>"),

    /**
     * Shown when the sender lacks permission to use a subcommand.
     * <p>
     * No placeholders.
     */
    COMMAND_NO_PERMISSION("<red>You do not have permission to use this command."),

    /**
     * Shown when not enough arguments are provided to a subcommand.
     * <p>
     * {@code %command%} – the main command name<br>
     * {@code %subcommand%} – the subcommand name<br>
     * {@code %args%} – the correct arguments usage string
     */
    COMMAND_NOT_ENOUGH_ARGS("<red>Usage <white>/%command% %subcommand% %args%</white>"),

    /**
     * Shown when a non-player (e.g., console) attempts to use a subcommand that requires a player.
     * <p>
     * No placeholders.
     */
    COMMAND_PLAYERS_ONLY("<red>Command can only be used by players."),

    /**
     * Shown when the specified operation does not exist.
     * <p>
     * {@code %operation%} – the operation name
     */
    COMMAND_INVALID_OPERATION("<red>Invalid operation <white>%operation%</white>"),


    // -----< Config >-----

    /**
     * Shown after reloading the configuration.
     * <p>
     * {@code %time%} – reload time in milliseconds
     */
    CONFIG_RELOADED("<green>Config reloaded in <white>%time%</white> ms."),


    // -----< Player >-----

    /**
     * Shown when the specified player does not exist or is not online.
     * <p>
     * {@code %player%} – the player name
     */
    PLAYER_NOT_FOUND("<red>Player not found or offline <white>%player%</white>"),


    // -----< Item >-----

    /**
     * Shown when the specified item does not exist.
     * <p>
     * {@code %item%} – the item name
     */
    ITEM_NOT_FOUND("<red>Item not found <white>%item%</white>"),

    /**
     * Shown when an item is successfully given to a player.
     * <p>
     * {@code %item%} – the name of the given item<br>
     * {@code %player%} – the name of the receiving player
     */
    ITEM_GIVEN_SUCCESS("<green>Gave <white>%item%</white> to <white>%player%</white>"),


    // -----< Weapon >-----

    /**
     * Shown when the specified weapon does not exist.
     * <p>
     * {@code %weapon%} – the weapon name
     */
    WEAPON_NOT_FOUND("<red>Weapon not found <white>%weapon%</white>"),

    /**
     * Shown on the actionbar during weapon interactions (e.g., shooting, reload attempt).
     * <p>
     * {@code %displayname%} – weapon’s raw display name (unstyled)<br>
     * {@code %bullets%} – current bullet count<br>
     * {@code %maxbullets%} – max bullet capacity<br>
     * {@code %total%} – total ammo count in the player’s inventory (for this weapon)
     */
    WEAPON_STATUS("<b><dark_gray><#7fdbff>%displayname%</#7fdbff> = <white>%bullets%</white> / <#7fdbff>%maxbullets%</#7fdbff> [<#39ff14>%total%</#39ff14>]"),

    /**
     * Shown on the actionbar when a weapon is reloading.
     * <p>
     * {@code %bullets%} – current bullet count<br>
     * {@code %maxbullets%} – max bullet capacity<br>
     * {@code %total%} – total ammo count in the player’s inventory (for this weapon)<br>
     * {@code %time%} – seconds remaining for reload
     */
    WEAPON_RELOADING("<b><dark_gray><#7fdbff>Reloading</#7fdbff> = <white>%bullets%</white> / <#7fdbff>%maxbullets%</#7fdbff> [<#39ff14>%total%</#39ff14>] | <white>%time%</white> secs"),

    /**
     * Shown on the actionbar when a weapon reload is canceled.
     * <p>
     * No placeholders.
     */
    WEAPON_RELOAD_CANCELED("<red>Reload canceled."),

    /**
     * Shown when a subcommand requires holding a weapon in the main hand.
     * <p>
     * No placeholders.
     */
    WEAPON_MAINHAND_REQUIRED("<red>You must have a weapon in your main hand."),


    // -----< Skins >-----

    /**
     * Shown when the specified skin does not exist for the given weapon.
     * <p>
     * No placeholders.
     */
    SKIN_NOT_FOUND("<red>Weapon does not have this skin."),

    /**
     * Shown when a player attempts to use a skin they own but failed to load on server startup.
     * <p>
     * No placeholders.
     */
    SKIN_NOT_LOADED("<red>Server did not load this skin on startup."),

    /**
     * Shown when a player attempts to use a skin they do not own.
     * <p>
     * No placeholders.
     */
    SKIN_NOT_OWNED_SELF("<red>You do not have this skin."),

    /**
     * Shown when attempting to remove a skin that the target player does not own.
     * <p>
     * No placeholders.
     */
    SKIN_NOT_OWNED_OTHER("<red>Player does not have this skin."),

    /**
     * Shown when attempting to add a skin that the target player already owns.
     * <p>
     * No placeholders.
     */
    SKIN_ALREADY_OWNED_OTHER("<yellow>Player already owns this skin."),

    /**
     * Shown when a skin is successfully added to a player.
     * <p>
     * No placeholders.
     */
    SKIN_ADDED_SUCCESS("<green>Skin added successfully."),

    /**
     * Shown when a skin is successfully removed from a player.
     * <p>
     * No placeholders.
     */
    SKIN_REMOVED_SUCCESS("<green>Skin removed successfully."),

    /**
     * Shown when all skins have been successfully added to a player.
     * <p>
     * {@code %count%} – the number of skins added
     */
    SKINS_ADDED_SUCCESS("<green>Added <white>%count%</white> skin(s) to the player."),

    /**
     * Shown when all skins have been successfully removed from a player.
     * <p>
     * {@code %count%} – the number of skins removed
     */
    SKINS_REMOVED_SUCCESS("<green>Removed <white>%count%</white> skin(s) from the player."),


    // -----< Debug >-----

    /**
     * Shown when weapon spray debug messages are enabled.
     * <p>
     * No placeholders.
     */
    DEBUG_SPRAY_ENABLED("<white>Spray debug <green>ENABLED</green>"),

    /**
     * Shown when weapon spray debug messages are disabled.
     * <p>
     * No placeholders.
     */
    DEBUG_SPRAY_DISABLED("<white>Spray debug <red>DISABLED</red>");


    // ----------< Instance >----------

    /**
     * The default Mini-Message formatted message used if the message was not loaded from the config.
     */
    final String defaultMessage;

    /**
     * The config key used to identify this message in the config file.
     */
    final String configKey;

    ComponentMessage(@NotNull String defaultMessage) {
        this.defaultMessage = defaultMessage;
        this.configKey = this.name().toLowerCase(Locale.ROOT);
    }

    // -----< Public Messaging API >-----

    /**
     * Converts this enum message into a parsed {@link Component} for the given {@link CommandSender}.
     *
     * @param sender the command sender (used for locale resolution)
     * @param values optional placeholder values; if {@code null}, no substitution is performed
     * @return the parsed {@link Component}
     */
    public @NotNull Component toComponent(@NotNull CommandSender sender,
                                          @Nullable Map<String, String> values) {
        String message = getMessage(sender);
        String formatted = values == null ? message : applyPlaceholders(message, values);
        return ComponentUtils.deserialize(formatted);
    }

    /**
     * Sends this parsed message to the chat of the recipient.
     *
     * @param recipient the recipient of the message
     * @param values    optional placeholder values
     */
    public void sendMessage(@NotNull CommandSender recipient,
                            @Nullable Map<String, String> values) {
        recipient.sendMessage(toComponent(recipient, values));
    }

    /**
     * Sends this parsed message on the action bar of the recipient.
     *
     * @param recipient the recipient of the message
     * @param values    optional placeholder values
     */
    public void sendActionBar(@NotNull CommandSender recipient,
                              @Nullable Map<String, String> values) {
        recipient.sendActionBar(toComponent(recipient, values));
    }

    // -----< Internal Resolution Logic >-----

    /**
     * Resolves the localized MiniMessage-formatted message string for the given {@link CommandSender}.
     *
     * @param sender the command sender whose locale is used
     * @return a localized message string or a fallback when no translation is available
     */
    private @NotNull String getMessage(@NotNull CommandSender sender) {
        Locale serverDefault = ConfigManager.instance().locale;
        Locale senderLocale = ServerUtils.getLocaleOrDefault(sender, serverDefault);

        String resolved = MessageManager.instance().resolveMessage(senderLocale, serverDefault, this);
        return resolved != null ? resolved : defaultMessage;
    }

    /**
     * Applies placeholder substitutions to the template.
     *
     * @param template the message containing placeholders (e.g., {@code %player%})
     * @param values   placeholder key → replacement value map
     * @return the message with all placeholders replaced
     */
    private @NotNull String applyPlaceholders(@NotNull String template,
                                              @NotNull Map<String, String> values) {
        for (Map.Entry<String, String> e : values.entrySet())
            template = template.replace("%" + e.getKey() + "%", e.getValue());
        return template;
    }
}