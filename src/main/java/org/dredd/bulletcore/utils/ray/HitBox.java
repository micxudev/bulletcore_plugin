package org.dredd.bulletcore.utils.ray;

import java.util.List;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

// TODO: replace purely by BoundingBox

public final class HitBox extends BoundingBox {

    // -----< Attributes >-----

    private Block block;
    private LivingEntity livingEntity;
    private List<HitBox> voxelShape;


    // -----< Construction >-----

    private HitBox(@NotNull BoundingBox box) {
        super(box.getMinX(), box.getMinY(), box.getMinZ(), box.getMaxX(), box.getMaxY(), box.getMaxZ());
    }


    // -----< Internal Stuff >-----

    private void setBlockHitBox(@NotNull Block block) {
        if (livingEntity != null) throw new IllegalArgumentException("Hitbox is already set for living entity");
        this.block = block;
    }

    private void setLivingEntity(@NotNull LivingEntity livingEntity) {
        if (block != null) throw new IllegalArgumentException("Hitbox is already set for block");
        this.livingEntity = livingEntity;
    }

    private void setVoxelShape(@NotNull List<HitBox> voxelShape) {
        this.voxelShape = voxelShape;
    }


    // -----< Raytrace >-----

    @Nullable RayTraceResult rayTrace(@NotNull Vector location, @NotNull Vector normalizedDirection) {
        final RayTraceResult mainBoxHit = ray(location, normalizedDirection);

        // Voxel shape not used or didn't hit main hitbox
        if (voxelShape == null || mainBoxHit == null) return null;

        // Here we know main hitbox was hit, now check all voxel shapes
        RayTraceResult hit = null;
        double closestHit = Double.POSITIVE_INFINITY;
        for (final HitBox boxPart : voxelShape) {
            if (mainBoxHit instanceof BlockTraceResult blockHit) {
                boxPart.setBlockHitBox(blockHit.getBlock());
            } else if (mainBoxHit instanceof EntityTraceResult entityHit) {
                boxPart.setLivingEntity(entityHit.getEntity());
            }

            final RayTraceResult boxPartHit = boxPart.ray(location, normalizedDirection);
            if (boxPartHit == null) continue;

            if (boxPartHit.getHitMin() < closestHit) {
                closestHit = boxPartHit.getHitMin();
                hit = boxPartHit;
            }
        }
        return hit;
    }

    /**
     * @see BoundingBox#rayTrace(Vector, Vector, double)
     */
    private @Nullable RayTraceResult ray(@NotNull Vector start, @NotNull Vector normalizedDirection) {
        final double startX = start.getX();
        final double startY = start.getY();
        final double startZ = start.getZ();

        final double dirX = normalizedDirection.getX();
        final double dirY = normalizedDirection.getY();
        final double dirZ = normalizedDirection.getZ();

        final double divX = 1.0D / dirX;
        final double divY = 1.0D / dirY;
        final double divZ = 1.0D / dirZ;

        double tMin;
        double tMax;
        BlockFace hitBlockFaceMin;
        BlockFace hitBlockFaceMax;

        // intersections with x planes:
        if (dirX >= 0.0D) {
            tMin = (getMinX() - startX) * divX;
            tMax = (getMaxX() - startX) * divX;
            hitBlockFaceMin = BlockFace.WEST;
            hitBlockFaceMax = BlockFace.EAST;
        } else {
            tMin = (getMaxX() - startX) * divX;
            tMax = (getMinX() - startX) * divX;
            hitBlockFaceMin = BlockFace.EAST;
            hitBlockFaceMax = BlockFace.WEST;
        }

        // intersections with y planes:
        double tyMin;
        double tyMax;
        BlockFace hitBlockFaceYMin;
        BlockFace hitBlockFaceYMax;
        if (dirY >= 0.0D) {
            tyMin = (getMinY() - startY) * divY;
            tyMax = (getMaxY() - startY) * divY;
            hitBlockFaceYMin = BlockFace.DOWN;
            hitBlockFaceYMax = BlockFace.UP;
        } else {
            tyMin = (getMaxY() - startY) * divY;
            tyMax = (getMinY() - startY) * divY;
            hitBlockFaceYMin = BlockFace.UP;
            hitBlockFaceYMax = BlockFace.DOWN;
        }
        if ((tMin > tyMax) || (tMax < tyMin)) {
            return null;
        }
        if (tyMin > tMin) {
            tMin = tyMin;
            hitBlockFaceMin = hitBlockFaceYMin;
        }
        if (tyMax < tMax) {
            tMax = tyMax;
            hitBlockFaceMax = hitBlockFaceYMax;
        }

        // intersections with z planes:
        double tzMin;
        double tzMax;
        BlockFace hitBlockFaceZMin;
        BlockFace hitBlockFaceZMax;
        if (dirZ >= 0.0D) {
            tzMin = (getMinZ() - startZ) * divZ;
            tzMax = (getMaxZ() - startZ) * divZ;
            hitBlockFaceZMin = BlockFace.NORTH;
            hitBlockFaceZMax = BlockFace.SOUTH;
        } else {
            tzMin = (getMaxZ() - startZ) * divZ;
            tzMax = (getMinZ() - startZ) * divZ;
            hitBlockFaceZMin = BlockFace.SOUTH;
            hitBlockFaceZMax = BlockFace.NORTH;
        }
        if ((tMin > tzMax) || (tMax < tzMin)) {
            return null;
        }
        if (tzMin > tMin) {
            tMin = tzMin;
            hitBlockFaceMin = hitBlockFaceZMin;
        }
        if (tzMax < tMax) {
            tMax = tzMax;
            hitBlockFaceMax = hitBlockFaceZMax;
        }

        // intersections are behind the start:
        if (tMax < 0.0) return null;

        // Start - Added hitBlockFace handling same as in BoundingBox
        // intersections are to far away:
        //if (tMin > maxDistance) { // We do not have maxDistance (infinite ray?)
        //    return null;
        //}

        // find the closest intersection:
        //double t;
        BlockFace hitBlockFace;
        if (tMin < 0.0D) {
            //t = tMax;
            hitBlockFace = hitBlockFaceMax;
        } else {
            //t = tMin;
            hitBlockFace = hitBlockFaceMin;
        }
        // End - Added hitBlockFace handling same as in BoundingBox

        if (block != null) {
            return new BlockTraceResult(start, normalizedDirection, this, hitBlockFace, tMin, tMax, block);
        }
        if (livingEntity != null) {
            return new EntityTraceResult(start, normalizedDirection, this, hitBlockFace, tMin, tMax, livingEntity);
        }
        return null; // Should not happen
    }


    // -----< Static >-----

    public static @Nullable HitBox getHitbox(@NotNull LivingEntity livingEntity) {
        if (livingEntity.isInvulnerable()) return null; // Should be moved to entity filter

        final HitBox hitBox = new HitBox(livingEntity.getBoundingBox());
        hitBox.setLivingEntity(livingEntity);

        return hitBox;
    }

    public static @Nullable HitBox getHitbox(@NotNull Block block, boolean allowLiquid) {
        if (!block.getChunk().isLoaded() || block.isEmpty()) return null;

        final boolean isLiquid = block.isLiquid();
        final boolean passable = block.isPassable();
        if (!allowLiquid) {
            if (isLiquid || passable) return null;
        } else if (!isLiquid && passable) {
            // Check like this because liquid is also passable...
            return null;
        }

        final HitBox hitBox = new HitBox(block.getBoundingBox());
        hitBox.setBlockHitBox(block);

        final var voxelShape = block.getCollisionShape().getBoundingBoxes();
        if (voxelShape.size() > 1) {
            final int x = block.getX();
            final int y = block.getY();
            final int z = block.getZ();

            final List<HitBox> voxelShapeList = voxelShape.stream()
                .map(boxPart -> new HitBox(boxPart.shift(x, y, z)))
                .toList();

            hitBox.setVoxelShape(voxelShapeList);
        }

        return hitBox;
    }
}