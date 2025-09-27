package org.dredd.bulletcore.config.messages;

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
 * They can be converted into {@link Component} instances via {@link #asComponent(CommandSender, Map)}.
 *
 * @author dredd
 * @since 1.0.0
 */
public enum ComponentMessage {

    /**
     * Shown when a player or console executes a command that is not defined.
     * <p>
     * {@code %commandline%} – the command that was entered
     */
    UNKNOWN_COMMAND("<red>Unknown command: <white>%commandline%</white>"),

    /**
     * Shown when a command is executed without specifying a subcommand.
     * <p>
     * {@code %command%} – the main command name
     */
    NO_SUBCOMMAND_PROVIDED("<red>Usage: <white>/%command% <subcommand></white>"),

    /**
     * Shown when an unknown subcommand is used.
     * <p>
     * {@code %subcommand%} – the provided subcommand name
     */
    UNKNOWN_SUBCOMMAND("<red>Unknown subcommand: <white>%subcommand%</white>"),

    /**
     * Shown when the sender lacks permission to use a subcommand.
     * <p>
     * No placeholders.
     */
    NO_SUBCOMMAND_PERMISSION("<red>You do not have permission to use this subcommand."),

    /**
     * Shown when not enough arguments are provided to a subcommand.
     * <p>
     * {@code %command%} – the main command name<br>
     * {@code %subcommand%} – the subcommand name<br>
     * {@code %args%} – the correct arguments usage string
     */
    NOT_ENOUGH_ARGS("<red>Usage: <white>/%command% %subcommand% %args%</white>"),

    /**
     * Shown when a non-player (e.g., console) attempts to use a subcommand that requires a player.
     * <p>
     * No placeholders.
     */
    ONLY_PLAYERS("<red>This subcommand can only be used by players."),

    /**
     * Shown after successfully reloading the configuration.
     * <p>
     * {@code %time%} – reload time in milliseconds
     */
    CONFIG_RELOADED("<green>Reloaded in <white>%time%</white> ms."),

    /**
     * Shown when an invalid item is specified.
     * <p>
     * {@code %item%} – the name of the invalid item
     */
    INVALID_ITEM("<red>Invalid item: <white>%item%</white>"),

    /**
     * Shown when a player cannot be found.
     * <p>
     * {@code %player%} – the name of the player
     */
    PLAYER_NOT_FOUND("<red>Player not found: <white>%player%</white>"),

    /**
     * Shown when an item is successfully given to a player.
     * <p>
     * {@code %item%} – the name of the given item<br>
     * {@code %player%} – the name of the receiving player
     */
    ITEM_GIVEN("<green>Gave <white>%item%</white> to <white>%player%</white>"),

    /**
     * Shown on the actionbar during weapon interactions (e.g., shooting, reload).
     * <p>
     * {@code %displayname%} – weapon’s raw display name (unstyled)<br>
     * {@code %bullets%} – current bullet count<br>
     * {@code %maxbullets%} – max bullet capacity<br>
     * {@code %total%} – total ammo count in the player’s inventory
     */
    WEAPON_ACTIONBAR("<b><dark_gray><#7fdbff>%displayname%</#7fdbff> = <white>%bullets%</white> / <#7fdbff>%maxbullets%</#7fdbff> [<#39ff14>%total%</#39ff14>]"),

    /**
     * Shown on the actionbar when a weapon reload is canceled.
     * <p>
     * No placeholders.
     */
    WEAPON_RELOAD_CANCEL("<red>Reload canceled"),

    /**
     * Shown on the actionbar when a weapon is reloading.
     * <p>
     * {@code %bullets%} – current bullet count<br>
     * {@code %maxbullets%} – max bullet capacity<br>
     * {@code %total%} – total ammo count in the player’s inventory<br>
     * {@code %time%} – seconds remaining for reload
     */
    WEAPON_RELOAD("<b><dark_gray><#7fdbff>Reloading</#7fdbff> = <white>%bullets%</white> / <#7fdbff>%maxbullets%</#7fdbff> [<#39ff14>%total%</#39ff14>] | <white>%time%</white> secs"),

    /**
     * Shown when a subcommand requires holding a weapon in the main hand.
     * <p>
     * No placeholders.
     */
    NO_WEAPON_IN_MAINHAND("<red>You must hold a weapon in your main hand to use this subcommand."),

    /**
     * Shown when a player attempts to use a skin they do not own.
     * <p>
     * {@code %skin%} – the requested skin name
     */
    NO_SKIN("<red>You do not have the skin: <white>%skin%</white>"),

    /**
     * Shown when a player attempts to use an owned skin that failed to load on a server startup.
     * <p>
     * {@code %skin%} – the requested skin name
     */
    ERROR_LOADING_SKIN("<red>An error occurred while loading the skin: <white>%skin%</white>"),

    /**
     * Shown when an invalid operation is specified.
     * <p>
     * {@code %operation%} – the invalid operation name
     */
    INVALID_OPERATION("<red>Invalid operation: <white>%operation%</white>"),

    /**
     * Shown when an invalid weapon is specified.
     * <p>
     * {@code %weapon%} – the invalid weapon name
     */
    INVALID_WEAPON("<red>Invalid weapon: <white>%weapon%</white>"),

    /**
     * Shown when all skins have been successfully added to a player.
     * <p>
     * {@code %count%} – the number of skins added
     */
    SKINS_ADDED("<green>Successfully added <white>%count%</white> skin(s) to the player."),

    /**
     * Shown when all skins have been successfully removed from a player.
     * <p>
     * {@code %count%} – the number of skins removed
     */
    SKINS_REMOVED("<yellow>Successfully removed <white>%count%</white> skin(s) from the player."),

    /**
     * Shown when the specified skin is not found for a weapon.
     * <p>
     * {@code %skin%} – the missing skin name
     */
    SKIN_NOT_FOUND("<red>Skin not found: <white>%skin%</white>"),

    /**
     * Shown when a skin is successfully added to a player.
     * <p>
     * No placeholders.
     */
    SKIN_ADDED("<green>Skin was successfully added to the player."),

    /**
     * Shown when a player already owns the specified skin.
     * <p>
     * No placeholders.
     */
    SKIN_ALREADY_OWNED("<yellow>The player already has this skin."),

    /**
     * Shown when a skin is successfully removed from a player.
     * <p>
     * No placeholders.
     */
    SKIN_REMOVED("<yellow>Skin was successfully removed from the player."),

    /**
     * Shown when a player does not own the specified skin.
     * <p>
     * No placeholders.
     */
    SKIN_NOT_OWNED("<red>The player does not have this skin."),

    /**
     * Shown when weapon spray debug messages are enabled.
     * <p>
     * No placeholders.
     */
    SPRAY_INFO_ON("<white>Spray info is <green>ON</green>"),

    /**
     * Shown when weapon spray debug messages are disabled.
     * <p>
     * No placeholders.
     */
    SPRAY_INFO_OFF("<white>Spray info is <red>OFF</red>");

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

    /**
     * Resolves the localized MiniMessage-formatted message string for the given {@link CommandSender}.
     *
     * @param sender the command sender whose locale is used
     * @return a localized message string or a fallback when no translation is available
     */
    private @NotNull String getMessage(@NotNull CommandSender sender) {
        Locale serverDefault = ConfigManager.get().locale;
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

    /**
     * Converts this enum message into a parsed {@link Component} for the given {@link CommandSender}.
     *
     * @param sender the command sender (used for locale resolution)
     * @param values optional placeholder values; if {@code null}, no substitution is performed
     * @return the parsed {@link Component}
     */
    public @NotNull Component asComponent(@NotNull CommandSender sender,
                                          @Nullable Map<String, String> values) {
        String message = getMessage(sender);
        String formatted = values == null ? message : applyPlaceholders(message, values);
        return ComponentUtils.deserialize(formatted);
    }
}