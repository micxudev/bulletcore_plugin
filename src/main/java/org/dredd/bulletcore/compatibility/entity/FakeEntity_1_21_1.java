package org.dredd.bulletcore.compatibility.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.core.Rotations;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundRotateHeadPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.block.data.CraftBlockData;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

public final class FakeEntity_1_21_1 extends FakeEntity {

    // -----< Attributes >-----

    private final Entity entity;
    private final Set<ServerGamePacketListenerImpl> trackedByPlayers;
    private final ServerLevel nmsWorld;
    private final int entityData;


    // -----< Construction >-----

    public FakeEntity_1_21_1(@NotNull EntityType type, @NotNull Location location, @Nullable Object data) {
        super(type, location.getWorld());

        final CraftWorld bukkitWorld = (CraftWorld) location.getWorld();
        final ServerLevel nmsWorld = bukkitWorld.getHandle();

        int entityData = 0;

        // Some entity types require extra data to be displayed.
        // It is up to the caller to make sure that "data" is not null and is of correct type.
        // Otherwise, NullPointerException, ClassCastException will occur.
        this.entity = switch (type) {
            case ITEM -> {
                final ItemStack item = CraftItemStack.asNMSCopy((org.bukkit.inventory.ItemStack) data);
                final ItemEntity itemEntity = new ItemEntity(net.minecraft.world.entity.EntityType.ITEM, nmsWorld);
                itemEntity.setItem(item);
                yield itemEntity;
            }
            case FALLING_BLOCK -> {
                final BlockState blockState = ((CraftBlockData) ((Material) data).createBlockData()).getState();
                entityData = Block.getId(blockState);
                yield new FallingBlockEntity(net.minecraft.world.entity.EntityType.FALLING_BLOCK, nmsWorld);
            }
            case FIREWORK_ROCKET -> {
                final ItemStack item = CraftItemStack.asNMSCopy((org.bukkit.inventory.ItemStack) data);
                yield new FireworkRocketEntity(nmsWorld, item, 0, 0, 0, true);
            }
            case ARMOR_STAND -> {
                final ItemStack item = CraftItemStack.asNMSCopy((org.bukkit.inventory.ItemStack) data);
                final ArmorStand armorStand = new ArmorStand(net.minecraft.world.entity.EntityType.ARMOR_STAND, nmsWorld);
                armorStand.setItemSlot(EquipmentSlot.HEAD, item);
                armorStand.setMarker(true);
                armorStand.setInvisible(true);
                armorStand.setShowArms(false);
                armorStand.setNoBasePlate(true);
                yield armorStand;
            }
            case BLOCK_DISPLAY -> {
                final BlockState blockState = ((CraftBlockData) ((Material) data).createBlockData()).getState();
                final var blockDisplay = new Display.BlockDisplay(net.minecraft.world.entity.EntityType.BLOCK_DISPLAY, nmsWorld);
                blockDisplay.setBlockState(blockState);
                yield blockDisplay;
            }
            case ITEM_DISPLAY -> {
                final ItemStack item = CraftItemStack.asNMSCopy((org.bukkit.inventory.ItemStack) data);
                final var itemDisplay = new Display.ItemDisplay(net.minecraft.world.entity.EntityType.ITEM_DISPLAY, nmsWorld);
                itemDisplay.setItemStack(item);
                //itemDisplay.getEntityData().set(Display.DATA_POS_ROT_INTERPOLATION_DURATION_ID, 1);
                //itemDisplay.setViewRange(5.0F);
                yield itemDisplay;
            }
            default -> bukkitWorld.makeEntity(location, type.getEntityClass());
        };
        entity.setNoGravity(true);

        this.trackedByPlayers = new ReferenceOpenHashSet<>();
        this.entityData = entityData;
        this.nmsWorld = nmsWorld;
        this.setLocation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
    }


    // -----< Entity Meta >-----

    @Override
    public boolean getMeta(@NonNull EntityMetaFlag flag) {
        return entity.getSharedFlag(flag.index);
    }

    @Override
    public void setMeta(@NonNull EntityMetaFlag flag, boolean enabled) {
        entity.setSharedFlag(flag.index, enabled);
    }

    @Override
    public void updateMeta() {
        if (entity instanceof ArmorStand armorStand) {
            // TODO: how does this affect?
            armorStand.setHeadPose(new Rotations(getPitch(), 0, 0));
        }

        sendPackets(new ClientboundSetEntityDataPacket(entity.getId(), entity.getEntityData().packAll()));
    }


    // -----< Location >-----

    @Override
    protected void setLocation(double x, double y, double z, float yaw, float pitch) {
        super.setLocation(x, y, z, yaw, pitch);

        // Needed for teleport packet.
        final Entity entity = this.entity;
        entity.setPosRaw(x, y, z);
        entity.setYHeadRot(yaw);
        entity.setYRot(yaw);
        entity.setXRot(pitch);
    }

    @Override
    protected void sendTeleportPacket(float yaw) {
        final Entity entity = this.entity;
        final var teleportPacket = new ClientboundTeleportEntityPacket(entity);
        final var headRotationPacket = new ClientboundRotateHeadPacket(entity, convertToByte(yaw));

        sendPackets(teleportPacket, headRotationPacket);
    }


    // -----< Visibility >-----

    @Override
    public void show() {
        final int viewDistanceInBlocks = Bukkit.getServer().getViewDistance() * 16;

        final List<ServerPlayer> nearbyPlayers = getNearByPlayers(location, viewDistanceInBlocks);
        if (nearbyPlayers.isEmpty()) return;

        final Entity entity = this.entity;
        final Vec3 pos = entity.position();

        final var spawnPacket = new ClientboundAddEntityPacket(entity.getId(), entity.getUUID(), pos.x, pos.y, pos.z, entity.getXRot(), entity.getYRot(), entity.getType(), entityData, Vec3.ZERO, entity.getYHeadRot());
        final var metaPacket = new ClientboundSetEntityDataPacket(entity.getId(), entity.getEntityData().packAll());
        final var equipmentPacket = getEquipmentPacket();

        for (final ServerPlayer player : nearbyPlayers) {
            final var connection = player.connection;

            if (!trackedByPlayers.add(connection)) continue;

            connection.send(spawnPacket);
            connection.send(metaPacket);
            if (equipmentPacket != null) {
                connection.send(equipmentPacket);
            }
        }
    }

    @Override
    public void remove() {
        sendPackets(new ClientboundRemoveEntitiesPacket(entity.getId()));
        trackedByPlayers.clear();
    }


    // -----< Utils >-----
    private void sendPackets(@NotNull Packet<?>... packets) {
        final var iterator = trackedByPlayers.iterator();
        while (iterator.hasNext()) {
            final var connection = iterator.next();

            if (connection.isDisconnected()) {
                iterator.remove();
                continue;
            }

            for (final var packet : packets) {
                connection.send(packet);
            }
        }
    }

    private @NotNull List<ServerPlayer> getNearByPlayers(@NotNull Location l, double r) {
        final List<ServerPlayer> result = new ArrayList<>();
        final double x = l.getX();
        final double y = l.getY();
        final double z = l.getZ();
        final AABB box = new AABB(x - r, y - r, z - r, x + r, y + r, z + r);

        for (final ServerPlayer player : nmsWorld.players())
            if (box.contains(player.getX(), player.getY(), player.getZ()))
                result.add(player);

        return result;
    }

    private @Nullable ClientboundSetEquipmentPacket getEquipmentPacket() {
        if (type != EntityType.ARMOR_STAND) return null;
        if (!(entity instanceof LivingEntity livingEntity)) return null;

        final ItemStack headItem = livingEntity.getItemBySlot(EquipmentSlot.HEAD);
        if (headItem.isEmpty()) return null;

        return new ClientboundSetEquipmentPacket(
            livingEntity.getId(),
            Collections.singletonList(Pair.of(EquipmentSlot.HEAD, headItem))
        );
    }
}