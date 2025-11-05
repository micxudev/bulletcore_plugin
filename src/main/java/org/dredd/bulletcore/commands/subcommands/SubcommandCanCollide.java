package org.dredd.bulletcore.commands.subcommands;

import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;
import org.dredd.bulletcore.config.materials.MaterialsManager;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

import static org.dredd.bulletcore.config.messages.component.ComponentMessage.*;
import static org.dredd.bulletcore.utils.ServerUtils.EMPTY_LIST;

/**
 * Checks if the target block will collide with bullets.
 *
 * @author dredd
 * @since 1.0.0
 */
public enum SubcommandCanCollide implements Subcommand {

    INSTANCE;

    private static final double DEFAULT_BLOCK_INTERACTION_RANGE = 4.5D;

    @Override
    public @NotNull String getName() {
        return "can_collide";
    }

    @Override
    public @NotNull String getUsageArgs() {
        return "";
    }

    @Override
    public int getMinArgs() {
        return 0;
    }

    @Override
    public @NotNull String getPermission() {
        return "bulletcore.command.can_collide";
    }

    @Override
    public void execute(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            COMMAND_PLAYERS_ONLY.sendMessage(sender, null);
            return;
        }

        final Location start = player.getEyeLocation();
        final RayTraceResult result = player.getWorld().rayTraceBlocks(
            start,
            start.getDirection().normalize(),
            DEFAULT_BLOCK_INTERACTION_RANGE,
            FluidCollisionMode.ALWAYS,
            false
        );

        if (result == null || result.getHitBlock() == null) {
            DEBUG_BLOCK_NOT_FOUND.sendMessage(player, null);
            return;
        }

        final Block hitBlock = result.getHitBlock();
        final var placeholders = Map.of("block", hitBlock.translationKey());

        final boolean willCollide = MaterialsManager.instance().canCollide.test(hitBlock);
        if (willCollide)
            DEBUG_BLOCK_COLLIDABLE.sendMessage(player, placeholders);
        else
            DEBUG_BLOCK_NON_COLLIDABLE.sendMessage(player, placeholders);
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        return EMPTY_LIST;
    }
}