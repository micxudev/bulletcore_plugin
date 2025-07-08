package org.dredd.bulletcore.commands.subcommands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.dredd.bulletcore.custom_item_manager.registries.CustomItemsRegistry;
import org.dredd.bulletcore.models.weapons.Weapon;
import org.dredd.bulletcore.models.weapons.skins.SkinsManager;
import org.dredd.bulletcore.models.weapons.skins.WeaponSkin;
import org.dredd.bulletcore.utils.ServerUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Implements the {@code /bulletcore skin_manage} subcommand.
 *
 * @author dredd
 * @since 1.0.0
 */
public class SubcommandSkinManage implements Subcommand {

    private static final List<String> EMPTY = Collections.emptyList();
    private static final List<String> OPERATIONS = List.of("add", "remove");
    private static final String ALL_OPTION = "--all";

    @Override
    public @NotNull String getName() {
        return "skin_manage";
    }

    @Override
    public @NotNull String getUsageArgs() {
        return "<add|remove> <player> <weapon> <skin>";
    }

    @Override
    public int getMinArgs() {
        return 4;
    }

    @Override
    public @NotNull String getPermission() {
        return "bulletcore.command.skin_manage";
    }

    @Override
    public void execute(@NotNull CommandSender sender, @NotNull String[] args) {
        String operation = args[1];
        if (!OPERATIONS.contains(operation)) {
            sender.sendMessage("Invalid operation: " + operation);
            return;
        }

        String playerName = args[2];
        Player player = Bukkit.getPlayer(playerName);
        if (player == null) {
            sender.sendMessage("Player not found: " + playerName);
            return;
        }

        String weaponName = args[3];
        Weapon weapon = CustomItemsRegistry.weapon.getItemOrNull(weaponName);
        if (weapon == null) {
            sender.sendMessage("Weapon not found: " + weaponName);
            return;
        }

        String skinName = args[4];
        if (skinName.equals(ALL_OPTION)) {
            switch (operation) {
                case "add" -> {
                    int skinsAdded = SkinsManager.addAllWeaponSkinsToPlayer(player, weapon);
                    sender.sendMessage("Added " + skinsAdded + " skins to player.");
                }
                case "remove" -> {
                    int skinsRemoved = SkinsManager.removeAllWeaponSkinsToPlayer(player, weapon);
                    sender.sendMessage("Removed " + skinsRemoved + " skins from player.");
                }
            }
            return;
        }

        WeaponSkin weaponSkin = SkinsManager.getWeaponSkin(weapon, skinName);
        if (weaponSkin == null) {
            sender.sendMessage("Skin not found: " + skinName);
            return;
        }

        switch (operation) {
            case "add" -> {
                if (SkinsManager.addSkinToPlayer(player, weapon, skinName))
                    sender.sendMessage("Skin added to player.");
                else
                    sender.sendMessage("Player already has this skin.");
            }
            case "remove" -> {
                if (SkinsManager.removeSkinFromPlayer(player, weapon, skinName))
                    sender.sendMessage("Skin removed from the player.");
                else
                    sender.sendMessage("Player does not have this skin.");
            }
        }
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        String operation = args[1];
        if (args.length == 2)
            return StringUtil.copyPartialMatches(operation, OPERATIONS, new ArrayList<>());
        if (!OPERATIONS.contains(operation)) return EMPTY;

        String playerName = args[2];
        if (args.length == 3)
            return StringUtil.copyPartialMatches(playerName, ServerUtils.getOnlinePlayerNames(), new ArrayList<>());
        Player player = Bukkit.getPlayer(playerName);
        if (player == null) return EMPTY;

        String weaponName = args[3];
        if (args.length == 4)
            return StringUtil.copyPartialMatches(weaponName, SkinsManager.getWeaponNamesWithSkins(), new ArrayList<>());
        Weapon weapon = CustomItemsRegistry.weapon.getItemOrNull(weaponName);
        if (weapon == null) return EMPTY;

        if (args.length == 5) {
            return switch (operation) {
                case "add" -> {
                    List<String> missingWeaponSkins = SkinsManager.getMissingWeaponSkins(player, weapon);
                    List<String> skinOptions = new ArrayList<>(missingWeaponSkins.size() + 1);
                    skinOptions.add(ALL_OPTION);
                    skinOptions.addAll(missingWeaponSkins);
                    yield StringUtil.copyPartialMatches(args[4], skinOptions, new ArrayList<>(skinOptions.size()));
                }
                case "remove" -> {
                    List<String> playerWeaponSkins = SkinsManager.getPlayerWeaponSkins(player, weapon);
                    List<String> skinOptions = new ArrayList<>(playerWeaponSkins.size() + 1);
                    skinOptions.add(ALL_OPTION);
                    skinOptions.addAll(playerWeaponSkins);
                    yield StringUtil.copyPartialMatches(args[4], skinOptions, new ArrayList<>(skinOptions.size()));
                }
                default -> EMPTY;
            };
        }

        return EMPTY;
    }
}