package org.dredd.bulletcore.config.messages;

/**
 * Represents translatable component messages used across the plugin.
 * <p>
 * Each enum constant defines:
 * <ul>
 *   <li>A unique message {@link #key} used to look up translations in language files</li>
 *   <li>A MiniMessage-formatted default string {@link #def}, used if no translation is available</li>
 * </ul>
 *
 * <p>Messages can be resolved and rendered via {@link MessageManager}, which handles locale lookup,
 * placeholder substitution, and MiniMessage deserialization.</p>
 *
 * @author dredd
 * @since 1.0.0
 */
public enum ComponentMessage {

    /**
     * Shown when a player/console executes a command that is not defined.<br>
     * {@code %commandline%} – The command that was sent.
     */
    UNKNOWN_COMMAND(
        "unknown-command",
        "<red>Unknown command: <white>%commandline%</white>"
    ),

    /**
     * Shown when a command is executed without a subcommand.<br>
     * {@code %command%} – The main command name.
     */
    NO_SUBCOMMAND_PROVIDED(
        "no-subcommand-provided",
        "<red>Usage: <white>/%command% <subcommand></white>"
    ),

    /**
     * Shown when an unknown subcommand is used.<br>
     * {@code %subcommand%} - The subcommand name that was provided.
     */
    UNKNOWN_SUBCOMMAND(
        "unknown-subcommand",
        "<red>Unknown subcommand: <white>%subcommand%</white>"
    ),

    /**
     * Shown when the sender lacks permission to use a subcommand.<br>
     * No placeholders.
     */
    NO_SUBCOMMAND_PERMISSION(
        "no-subcommand-permission",
        "<red>You do not have permission to use this subcommand."
    ),

    /**
     * Shown when not enough arguments are provided to a subcommand.<br>
     * {@code %command%} – The main command name.<br>
     * {@code %subcommand%} – The subcommand name.<br>
     * {@code %args%} – Correct arguments usage string.<br>
     */
    NOT_ENOUGH_ARGS(
        "not-enough-args",
        "<red>Usage: <white>/%command% %subcommand% %args%</white>"
    ),

    /**
     * Shown when a non-player (e.g., console) attempts to use a subcommand that requires a player.<br>
     * No placeholders.
     */
    ONLY_PLAYERS(
        "only-players",
        "<red>This subcommand can only be used by players."
    ),

    /**
     * Shown after successfully reloading the configuration.<br>
     * {@code %time%} – Reload time in milliseconds.
     */
    CONFIG_RELOADED(
        "config-reloaded",
        "<green>Reloaded in <white>%time%</white> ms."
    ),

    /**
     * Shown when an invalid item is specified.<br>
     * {@code %item%} – The name of the invalid item.
     */
    INVALID_ITEM(
        "invalid-item",
        "<red>Invalid item: <white>%item%</white>"
    ),

    /**
     * Shown when a player cannot be found.<br>
     * {@code %player%} – The name of the entered player.
     */
    PLAYER_NOT_FOUND(
        "player-not-found",
        "<red>Player not found: <white>%player%</white>"
    ),

    /**
     * Shown when an item is successfully given to a player.<br>
     * {@code %item%} – The name of the given item.<br>
     * {@code %player%} – The name of the entered player.
     */
    ITEM_GIVEN(
        "item-given",
        "<green>Gave <white>%item%</white> to <white>%player%</white>"
    ),

    /**
     * Shown on the actionbar during some interactions with a weapon (e.g., shooting, successful reload)<br>
     * {@code %displayname%} – Weapon's raw display name text (without styles).<br>
     * {@code %bullets%} – Current weapon's bullet count.<br>
     * {@code %maxbullets%} – Max weapon's bullet capacity.<br>
     * {@code %total%} – Total ammo count for this weapon inside player's inventory.
     */
    WEAPON_ACTIONBAR(
        "weapon-actionbar",
        "<b><dark_gray><#7fdbff>%displayname%</#7fdbff> = <white>%bullets%</white> / <#7fdbff>%maxbullets%</#7fdbff> [<#39ff14>%total%</#39ff14>]"
    ),

    /**
     * Shown on the actionbar when weapon reload is canceled<br>
     * No placeholders.
     */
    WEAPON_RELOAD_CANCEL(
        "weapon-reload-cancel",
        "<red>Reload canceled"
    ),

    /**
     * Shown on the actionbar when a weapon is reloading<br>
     * {@code %bullets%} – Current weapon's bullet count.<br>
     * {@code %maxbullets%} – Max weapon's bullet capacity.<br>
     * {@code %total%} – Total ammo count for this weapon inside player's inventory.<br>
     * {@code %time%} – Time in seconds remaining for reload to complete its current iteration.
     */
    WEAPON_RELOAD(
        "weapon-reload",
        "<b><dark_gray><#7fdbff>Reloading</#7fdbff> = <white>%bullets%</white> / <#7fdbff>%maxbullets%</#7fdbff> [<#39ff14>%total%</#39ff14>] | <white>%time%</white> secs"
    ),

    /**
     * Shown when a subcommand requires a weapon in the main hand.<br>
     * No placeholders.
     */
    NO_WEAPON_IN_MAINHAND(
        "no-weapon-in-mainhand",
        "<red>You must hold a weapon in your main hand to use this subcommand."
    ),

    /**
     * Shown when a player attempts to use a skin they don't have.<br>
     * {@code %skin%} - The name of the skin that was requested.
     */
    NO_SKIN(
        "no-skin",
        "<red>You do not have the skin: <white>%skin%</white>"
    ),

    /**
     * Shown when a player attempts to use the skin they own that was not loaded during server start.<br>
     * {@code %skin%} - The name of the skin that was requested.
     */
    ERROR_LOADING_SKIN(
        "error-loading-skin",
        "<red>An error occurred while loading the skin: <white>%skin%</white>"
    );

    /**
     * The key used to look up the localized version of the message in language files.
     */
    public final String key;

    /**
     * The default MiniMessage-formatted string used if the key is not found in language files.
     */
    public final String def;

    ComponentMessage(String key, String def) {
        this.key = key;
        this.def = def;
    }
}