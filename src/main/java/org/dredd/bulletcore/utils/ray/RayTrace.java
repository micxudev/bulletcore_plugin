package org.dredd.bulletcore.utils.ray;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.dredd.bulletcore.utils.MathUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

// TODO: replace by world.rayTrace + world.rayTraceBlocks + world.rayTraceEntities

public final class RayTrace {

    // -----< Attributes >-----

    private boolean disableEntityChecks;
    private boolean disableBlockChecks;
    private @Nullable Predicate<LivingEntity> entityFilter;
    private @Nullable Predicate<Block> blockFilter;
    private boolean allowLiquid;
    private double raySize = 0.1;


    // -----< Builder >-----

    public @NotNull RayTrace disableEntityChecks() {
        this.disableEntityChecks = true;
        return this;
    }

    public @NotNull RayTrace disableBlockChecks() {
        this.disableBlockChecks = true;
        return this;
    }

    public @NotNull RayTrace withEntityFilter(@Nullable Predicate<LivingEntity> entityFilter) {
        this.entityFilter = entityFilter;
        return this;
    }

    public @NotNull RayTrace withBlockFilter(@Nullable Predicate<Block> blockFilter) {
        this.blockFilter = blockFilter;
        return this;
    }

    public @NotNull RayTrace enableLiquidChecks() {
        this.allowLiquid = true;
        return this;
    }

    public @NotNull RayTrace withRaySize(double size) {
        this.raySize = size;
        return this;
    }


    // -----< API >-----

    public @NotNull List<RayTraceResult> cast(@NotNull World world,
                                              @NotNull Vector start,
                                              @NotNull Vector end,
                                              @NotNull Vector direction,
                                              double maximumBlockThrough) {
        final List<RayTraceResult> hits = new ArrayList<>(8);

        final Vector normalizedDirection = direction.clone().normalize();

        getBlockHits(hits, world, start, end, normalizedDirection, maximumBlockThrough);
        getEntityHits(hits, world, start, end, normalizedDirection);

        if (hits.size() > 1) {
            // Sort based on distance traveled (lowest to highest)
            hits.sort(Comparator.comparingDouble(RayTraceResult::getHitMin));
        }

        return hits;
    }


    // -----< Helpers >-----

    private void getBlockHits(@NotNull List<RayTraceResult> output,
                              @NotNull World world,
                              @NotNull Vector start,
                              @NotNull Vector end,
                              @NotNull Vector normalizedDirection,
                              double maximumBlockThrough) {
        if (this.disableBlockChecks) return;

        // Method based on NMS block traversing
        // (BlockGetter#traverseBlocks on 1.21.1)

        final double startX = MathUtils.lerp(start.getX(), end.getX(), -1.0E-7D);
        final double startY = MathUtils.lerp(start.getY(), end.getY(), -1.0E-7D);
        final double startZ = MathUtils.lerp(start.getZ(), end.getZ(), -1.0E-7D);

        int currentX = MathUtils.floor(startX);
        int currentY = MathUtils.floor(startY);
        int currentZ = MathUtils.floor(startZ);

        final Block startBlock = world.getBlockAt(currentX, currentY, currentZ);
        final RayTraceResult rayStartBlock = rayBlock(startBlock, start, normalizedDirection);
        if (rayStartBlock != null) {
            output.add(rayStartBlock);

            // Don't count liquid as actual hits along the path
            if (!allowLiquid || !startBlock.isLiquid()) {
                if (maximumBlockThrough != -1.0 && (maximumBlockThrough -= rayStartBlock.getThroughDistance()) < 0)
                    return;
            }
        }

        final double endX = MathUtils.lerp(end.getX(), start.getX(), -1.0E-7D);
        final double endY = MathUtils.lerp(end.getY(), start.getY(), -1.0E-7D);
        final double endZ = MathUtils.lerp(end.getZ(), start.getZ(), -1.0E-7D);

        final double directionX = endX - startX;
        final double directionY = endY - startY;
        final double directionZ = endZ - startZ;

        final int blockX = MathUtils.sign(directionX);
        final int blockY = MathUtils.sign(directionY);
        final int blockZ = MathUtils.sign(directionZ);

        final double addX = blockX == 0 ? Double.MAX_VALUE : blockX / directionX;
        final double addY = blockY == 0 ? Double.MAX_VALUE : blockY / directionY;
        final double addZ = blockZ == 0 ? Double.MAX_VALUE : blockZ / directionZ;

        double maxX = addX * (blockX > 0 ? 1.0D - MathUtils.frac(startX) : MathUtils.frac(startX));
        double maxY = addY * (blockY > 0 ? 1.0D - MathUtils.frac(startY) : MathUtils.frac(startY));
        double maxZ = addZ * (blockZ > 0 ? 1.0D - MathUtils.frac(startZ) : MathUtils.frac(startZ));

        while (maximumBlockThrough > -1) {
            if (maxX > 1.0 && maxY > 1.0 && maxZ > 1.0) break;

            if (maxX < maxY) {
                if (maxX < maxZ) {
                    currentX += blockX;
                    maxX += addX;
                } else {
                    currentZ += blockZ;
                    maxZ += addZ;
                }
            } else if (maxY < maxZ) {
                currentY += blockY;
                maxY += addY;
            } else {
                currentZ += blockZ;
                maxZ += addZ;
            }

            final Block newBlock = world.getBlockAt(currentX, currentY, currentZ);
            final RayTraceResult rayNewBlock = rayBlock(newBlock, start, normalizedDirection);
            if (rayNewBlock != null) {
                output.add(rayNewBlock);

                // Don't count liquid as actual hits along the path
                if (!allowLiquid || !newBlock.isLiquid()) {
                    if (--maximumBlockThrough < 0)
                        break;
                }
            }
        }
    }

    private @Nullable RayTraceResult rayBlock(@NotNull Block block,
                                              @NotNull Vector start,
                                              @NotNull Vector normalizedDirection) {
        if (blockFilter != null && blockFilter.test(block)) return null;

        final HitBox blockBox = HitBox.getHitbox(block, allowLiquid);
        if (blockBox == null) return null;

        return blockBox.rayTrace(start, normalizedDirection);
    }


    private void getEntityHits(@NotNull List<RayTraceResult> output,
                               @NotNull World world,
                               @NotNull Vector start,
                               @NotNull Vector end,
                               @NotNull Vector normalizedDirection) {
        if (this.disableEntityChecks) return;

        final BoundingBox bb = BoundingBox.of(start, end);

        final int minX = MathUtils.floor((bb.getMinX() - 2.0) / 16.0);
        final int maxX = MathUtils.floor((bb.getMaxX() + 2.0) / 16.0);
        final int minZ = MathUtils.floor((bb.getMinZ() - 2.0) / 16.0);
        final int maxZ = MathUtils.floor((bb.getMaxZ() + 2.0) / 16.0);

        for (int x = minX; x <= maxX; ++x) {
            for (int z = minZ; z <= maxZ; ++z) {
                final Chunk chunk = world.getChunkAt(x, z);
                for (final Entity entity : chunk.getEntities()) {
                    final RayTraceResult result = rayEntity(bb, entity, start, normalizedDirection);
                    if (result != null) output.add(result);
                }
            }
        }
    }

    private @Nullable RayTraceResult rayEntity(@NotNull BoundingBox bb,
                                               @NotNull Entity entity,
                                               @NotNull Vector start,
                                               @NotNull Vector normalizedDirection) {
        if (!(entity instanceof LivingEntity livingEntity)) return null;

        if (entityFilter != null && entityFilter.test(livingEntity)) return null;

        final HitBox entityBox = HitBox.getHitbox(livingEntity);
        if (entityBox == null) return null;

        if (!bb.overlaps(entityBox.expand(raySize))) return null;

        return entityBox.rayTrace(start, normalizedDirection);
    }
}