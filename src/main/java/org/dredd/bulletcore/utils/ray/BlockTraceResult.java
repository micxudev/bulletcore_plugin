package org.dredd.bulletcore.utils.ray;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

// TODO: to be replaced by org.bukkit.util.RayTraceResult

public final class BlockTraceResult extends RayTraceResult {

    private final @NotNull Block block;
    private final @NotNull BlockState blockState;

    BlockTraceResult(@NotNull Vector origin,
                     @NotNull Vector direction,
                     @NotNull HitBox hitBox,
                     @NotNull BlockFace hitFace,
                     double hitMin,
                     double hitMax,
                     @NotNull Block block) {
        super(origin, direction, hitBox, hitFace, hitMin, hitMax);
        this.block = block;
        this.blockState = block.getState();
    }

    /**
     * Returns the block that was hit. This block should only be used for x-y-z coordinates. To get
     * information about the block (material, data, etc.), use {@link #getBlockState()}.
     *
     * @return The hit block.
     */
    public @NotNull Block getBlock() {return block;}

    /**
     * Returns information about the block that was hit.
     *
     * @return The hit block state.
     */
    public @NotNull BlockState getBlockState() {return blockState;}
}