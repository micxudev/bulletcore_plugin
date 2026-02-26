package org.dredd.bulletcore.utils.ray;

import org.bukkit.block.BlockFace;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

// TODO: to be replaced by org.bukkit.util.RayTraceResult

public final class EntityTraceResult extends RayTraceResult {

    private final @NotNull LivingEntity entity;

    EntityTraceResult(@NotNull Vector origin,
                      @NotNull Vector direction,
                      @NotNull HitBox hitBox,
                      @NotNull BlockFace hitFace,
                      double hitMin,
                      double hitMax,
                      @NotNull LivingEntity entity) {
        super(origin, direction, hitBox, hitFace, hitMin, hitMax);
        this.entity = entity;
    }

    public @NotNull LivingEntity getEntity() {return entity;}
}