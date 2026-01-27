package org.dredd.bulletcore.commands.subcommands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.StringUtil;
import org.dredd.bulletcore.custom_item_manager.registries.CustomItemsRegistry;
import org.dredd.bulletcore.models.weapons.Weapon;
import org.dredd.bulletcore.models.weapons.skins.SkinsManager;
import org.dredd.bulletcore.models.weapons.skins.WeaponSkin;
import org.jetbrains.annotations.NotNull;

import static org.dredd.bulletcore.config.messages.component.ComponentMessage.COMMAND_PLAYERS_ONLY;
import static org.dredd.bulletcore.config.messages.component.ComponentMessage.SKIN_NOT_LOADED;
import static org.dredd.bulletcore.config.messages.component.ComponentMessage.SKIN_NOT_OWNED_SELF;
import static org.dredd.bulletcore.config.messages.component.ComponentMessage.WEAPON_MAINHAND_REQUIRED;
import static org.dredd.bulletcore.utils.ServerUtils.EMPTY_LIST;

/**
 * Applies a skin to the weapon in a player's main hand.
 *
 * @author dredd
 * @since 1.0.0
 */
public enum SubcommandSkin implements Subcommand {

    INSTANCE;

    private final static String DEFAULT_SKIN_NAME = "--default";

    @Override
    public @NotNull String getName() {
        return "skin";
    }

    @Override
    public @NotNull String getUsageArgs() {
        return "<skin>";
    }

    @Override
    public int getMinArgs() {
        return 1;
    }

    @Override
    public @NotNull String getPermission() {
        return "bulletcore.command.skin";
    }

    @Override
    public void execute(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            COMMAND_PLAYERS_ONLY.sendMessage(sender, null);
            return;
        }

        final ItemStack mainHandItem = player.getInventory().getItemInMainHand();
        final Weapon weapon = CustomItemsRegistry.getWeaponOrNull(mainHandItem);
        if (weapon == null) {
            WEAPON_MAINHAND_REQUIRED.sendMessage(player, null);
            return;
        }

        final String skinName = args[1];
        final WeaponSkin weaponSkin;
        if (skinName.equals(DEFAULT_SKIN_NAME)) {
            weaponSkin = weapon.skins.defaultSkin;
        } else {
            if (!SkinsManager.playerHasSkin(player, weapon, skinName)) {
                SKIN_NOT_OWNED_SELF.sendMessage(player, null);
                return;
            }
            weaponSkin = SkinsManager.getWeaponSkin(weapon, skinName);
            if (weaponSkin == null) {
                SKIN_NOT_LOADED.sendMessage(player, null);
                return;
            }
        }

        final ItemMeta meta = mainHandItem.getItemMeta();
        meta.setCustomModelData(weaponSkin.customModelData());
        meta.displayName(weaponSkin.displayName());
        mainHandItem.setItemMeta(meta);
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof Player player)) return EMPTY_LIST;

        final ItemStack mainHandItem = player.getInventory().getItemInMainHand();
        final Weapon weapon = CustomItemsRegistry.getWeaponOrNull(mainHandItem);
        if (weapon == null) return EMPTY_LIST;

        if (args.length == 2) {
            final List<String> playerWeaponSkins = SkinsManager.getPlayerWeaponSkins(player, weapon);
            final List<String> skinOptions = new ArrayList<>(playerWeaponSkins.size() + 1);
            skinOptions.add(DEFAULT_SKIN_NAME);
            skinOptions.addAll(playerWeaponSkins);

            return StringUtil.copyPartialMatches(args[1], skinOptions, new ArrayList<>(skinOptions.size()));
        }

        return EMPTY_LIST;
    }
}