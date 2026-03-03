package org.dredd.bulletcore.compatibility.entity;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Factory for creating fake entities (base class {@link FakeEntity}).
 *
 * @author dredd
 * @since 1.0.0
 */
public final class FakeEntityFactory {

    /**
     * Private constructor to prevent instantiation.
     */
    private FakeEntityFactory() {}

    /**
     * Creates a new {@link FakeEntity} implementation compatible with the
     * currently running server version.
     * <p>
     * This method acts as a version abstraction layer and returns the
     * appropriate internal implementation for the active Minecraft version.
     * <p>
     * <b>Current support:</b> {@code 1.21.1} (implemented by {@link FakeEntity_1_21_1}).
     *
     * @param type           the {@link EntityType} to emulate
     * @param entityLocation the initial spawn location of the fake entity;
     *                       the provided {@link Location} instance may be
     *                       modified internally (e.g., to apply visual offsets)
     * @param disguiseData   optional additional data required by certain entity
     *                       types; depending on {@code type}, this may be:
     *                       <ul>
     *                           <li>{@link Material}</li>
     *                           <li>{@link ItemStack}</li>
     *                           <li>{@code null} if not required</li>
     *                       </ul>
     *
     * @return a newly constructed {@link FakeEntity} instance that is not yet
     *         visible to any players and must be explicitly spawned/shown
     *         through its API
     */
    public static FakeEntity create(@NotNull EntityType type,
                                    @NotNull Location entityLocation,
                                    @Nullable Object disguiseData) {
        return new FakeEntity_1_21_1(type, entityLocation, disguiseData);
    }
}