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