package org.dredd.bulletcore.config.messages;

/**
 * Represents localizable component messages used across the plugin.
 * <p>
 * Messages can be resolved via {@link MessageManager} which handles locale lookup,
 * placeholder resolution, and deserialization.
 *
 * @author dredd
 * @since 1.0.0
 */
public enum ComponentMessage {

    /**
     * Shown when a player/console executes a command that is not defined.<br>
     * {@code %commandline%} – The command that was sent.
     */
    UNKNOWN_COMMAND("<red>Unknown command: <white>%commandline%</white>"),

    /**
     * Shown when a command is executed without a subcommand.<br>
     * {@code %command%} – The main command name.
     */
    NO_SUBCOMMAND_PROVIDED("<red>Usage: <white>/%command% <subcommand></white>"),

    /**
     * Shown when an unknown subcommand is used.<br>
     * {@code %subcommand%} - The subcommand name that was provided.
     */
    UNKNOWN_SUBCOMMAND("<red>Unknown subcommand: <white>%subcommand%</white>"),

    /**
     * Shown when the sender lacks permission to use a subcommand.<br>
     * No placeholders.
     */
    NO_SUBCOMMAND_PERMISSION("<red>You do not have permission to use this subcommand."),

    /**
     * Shown when not enough arguments are provided to a subcommand.<br>
     * {@code %command%} – The main command name.<br>
     * {@code %subcommand%} – The subcommand name.<br>
     * {@code %args%} – Correct arguments usage string.<br>
     */
    NOT_ENOUGH_ARGS("<red>Usage: <white>/%command% %subcommand% %args%</white>"),

    /**
     * Shown when a non-player (e.g., console) attempts to use a subcommand that requires a player.<br>
     * No placeholders.
     */
    ONLY_PLAYERS("<red>This subcommand can only be used by players."),

    /**
     * Shown after successfully reloading the configuration.<br>
     * {@code %time%} – Reload time in milliseconds.
     */
    CONFIG_RELOADED("<green>Reloaded in <white>%time%</white> ms."),

    /**
     * Shown when an invalid item is specified.<br>
     * {@code %item%} – The name of the invalid item.
     */
    INVALID_ITEM("<red>Invalid item: <white>%item%</white>"),

    /**
     * Shown when a player cannot be found.<br>
     * {@code %player%} – The name of the entered player.
     */
    PLAYER_NOT_FOUND("<red>Player not found: <white>%player%</white>"),

    /**
     * Shown when an item is successfully given to a player.<br>
     * {@code %item%} – The name of the given item.<br>
     * {@code %player%} – The name of the entered player.
     */
    ITEM_GIVEN("<green>Gave <white>%item%</white> to <white>%player%</white>"),

    /**
     * Shown on the actionbar during some interactions with a weapon (e.g., shooting, successful reload)<br>
     * {@code %displayname%} – Weapon's raw display name text (without styles).<br>
     * {@code %bullets%} – Current weapon's bullet count.<br>
     * {@code %maxbullets%} – Max weapon's bullet capacity.<br>
     * {@code %total%} – Total ammo count for this weapon inside player's inventory.
     */
    WEAPON_ACTIONBAR("<b><dark_gray><#7fdbff>%displayname%</#7fdbff> = <white>%bullets%</white> / <#7fdbff>%maxbullets%</#7fdbff> [<#39ff14>%total%</#39ff14>]"),

    /**
     * Shown on the actionbar when weapon reload is canceled<br>
     * No placeholders.
     */
    WEAPON_RELOAD_CANCEL("<red>Reload canceled"),

    /**
     * Shown on the actionbar when a weapon is reloading<br>
     * {@code %bullets%} – Current weapon's bullet count.<br>
     * {@code %maxbullets%} – Max weapon's bullet capacity.<br>
     * {@code %total%} – Total ammo count for this weapon inside player's inventory.<br>
     * {@code %time%} – Time in seconds remaining for reload to complete its current iteration.
     */
    WEAPON_RELOAD("<b><dark_gray><#7fdbff>Reloading</#7fdbff> = <white>%bullets%</white> / <#7fdbff>%maxbullets%</#7fdbff> [<#39ff14>%total%</#39ff14>] | <white>%time%</white> secs"),

    /**
     * Shown when a subcommand requires a weapon in the main hand.<br>
     * No placeholders.
     */
    NO_WEAPON_IN_MAINHAND("<red>You must hold a weapon in your main hand to use this subcommand."),

    /**
     * Shown when a player attempts to use a skin they don't have.<br>
     * {@code %skin%} - The name of the skin that was requested.
     */
    NO_SKIN("<red>You do not have the skin: <white>%skin%</white>"),

    /**
     * Shown when a player attempts to use the skin they own that was not loaded during server start.<br>
     * {@code %skin%} - The name of the skin that was requested.
     */
    ERROR_LOADING_SKIN("<red>An error occurred while loading the skin: <white>%skin%</white>"),

    /**
     * Shown when an invalid operation is specified.<br>
     * {@code %operation%} – The name of the invalid operation.
     */
    INVALID_OPERATION("<red>Invalid operation: <white>%operation%</white>"),

    /**
     * Shown when an invalid weapon is specified.<br>
     * {@code %weapon%} – The name of the invalid weapon.
     */
    INVALID_WEAPON("<red>Invalid weapon: <white>%weapon%</white>"),

    /**
     * Shown when all skins have been successfully added to the player.<br>
     * {@code %count%} – The number of skins added.
     */
    SKINS_ADDED("<green>Successfully added <white>%count%</white> skin(s) to the player."),

    /**
     * Shown when all skins have been successfully removed from the player.<br>
     * {@code %count%} – The number of skins removed.
     */
    SKINS_REMOVED("<yellow>Successfully removed <white>%count%</white> skin(s) from the player."),

    /**
     * Shown when the specified skin is not found for the weapon.<br>
     * {@code %skin%} – The name of the missing skin.
     */
    SKIN_NOT_FOUND("<red>Skin not found: <white>%skin%</white>"),

    /**
     * Shown when a skin was successfully added to the player.<br>
     * No placeholders.
     */
    SKIN_ADDED("<green>Skin was successfully added to the player."),

    /**
     * Shown when the player already has the specified skin.<br>
     * No placeholders.
     */
    SKIN_ALREADY_OWNED("<yellow>The player already has this skin."),

    /**
     * Shown when a skin was successfully removed from the player.<br>
     * No placeholders.
     */
    SKIN_REMOVED("<yellow>Skin was successfully removed from the player."),

    /**
     * Shown when the player does not have the specified skin.<br>
     * No placeholders.
     */
    SKIN_NOT_OWNED("<red>The player does not have this skin."),

    /**
     * Shown when weapon spray debug messages are enabled.<br>
     * No placeholders.
     */
    SPRAY_INFO_ON("<white>Spray info is <green>ON</green>"),

    /**
     * Shown when weapon spray debug messages are disabled.<br>
     * No placeholders.
     */
    SPRAY_INFO_OFF("<white>Spray info is <red>OFF</red>");

    /**
     * The default MiniMessage-formatted string used if the key is not found in language files.
     */
    public final String def;

    ComponentMessage(String def) {
        this.def = def;
    }
}