package org.dredd.bulletcore.compatibility.entity;

import java.util.ArrayList;
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
import net.minecraft.world.phys.Vec3;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.block.data.CraftBlockData;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import static net.minecraft.network.protocol.game.ClientboundMoveEntityPacket.PosRot;
import static net.minecraft.network.protocol.game.ClientboundMoveEntityPacket.Rot;

public class FakeEntity_1_21_1 extends FakeEntity {

    // -----< Static fields >-----

    public static final EquipmentSlot[] SLOTS = EquipmentSlot.values();


    // -----< Instance fields >-----

    private final Entity entity;
    private final Set<ServerGamePacketListenerImpl> trackedByPlayers;
    private int entityData;


    // -----< Constructor >-----

    public FakeEntity_1_21_1(@NotNull EntityType type, @NotNull Location location, @Nullable Object data) {
        super(type, location.getWorld());

        final CraftWorld world = (CraftWorld) location.getWorld();
        final ServerLevel handle = world.getHandle();

        // Mutating location is a bad thing, but acceptable for now
        if (offset != null) location.add(offset);

        // Some entity types require extra data to be displayed.
        // It is up to the caller to make sure that "data" is not null and is of correct type.
        // Otherwise, NullPointerException, ClassCastException will occur.
        this.entity = switch (type) {
            case ITEM -> {
                final ItemStack item = CraftItemStack.asNMSCopy((org.bukkit.inventory.ItemStack) data);
                final ItemEntity itemEntity = new ItemEntity(net.minecraft.world.entity.EntityType.ITEM, handle);
                itemEntity.setItem(item);
                yield itemEntity;
            }
            case FALLING_BLOCK -> {
                final BlockState blockState = ((CraftBlockData) ((Material) data).createBlockData()).getState();
                this.entityData = Block.getId(blockState);
                yield new FallingBlockEntity(net.minecraft.world.entity.EntityType.FALLING_BLOCK, handle);
            }
            case FIREWORK_ROCKET -> {
                final ItemStack item = CraftItemStack.asNMSCopy((org.bukkit.inventory.ItemStack) data);
                yield new FireworkRocketEntity(handle, item, 0, 0, 0, true);
            }
            case ARMOR_STAND -> {
                final ItemStack item = CraftItemStack.asNMSCopy((org.bukkit.inventory.ItemStack) data);
                final ArmorStand armorStand = new ArmorStand(net.minecraft.world.entity.EntityType.ARMOR_STAND, handle);
                armorStand.setItemSlot(EquipmentSlot.HEAD, item);
                armorStand.setMarker(true);
                armorStand.setInvisible(true);
                armorStand.setShowArms(false);
                armorStand.setNoBasePlate(true);
                yield armorStand;
            }
            case BLOCK_DISPLAY -> {
                final BlockState blockState = ((CraftBlockData) ((Material) data).createBlockData()).getState();
                final var blockDisplay = new Display.BlockDisplay(net.minecraft.world.entity.EntityType.BLOCK_DISPLAY, handle);
                blockDisplay.setBlockState(blockState);
                yield blockDisplay;
            }
            case ITEM_DISPLAY -> {
                final ItemStack item = CraftItemStack.asNMSCopy((org.bukkit.inventory.ItemStack) data);
                final var itemDisplay = new Display.ItemDisplay(net.minecraft.world.entity.EntityType.ITEM_DISPLAY, handle);
                itemDisplay.setItemStack(item);
                yield itemDisplay;
            }
            default -> world.makeEntity(location, type.getEntityClass());
        };
        entity.setNoGravity(true);

        this.trackedByPlayers = new ReferenceOpenHashSet<>();
        this.setLocation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
    }


    // -----< Entity Meta >-----

    @Override
    protected boolean getMeta(@NonNull EntityMetaFlag flag) {
        return entity.getSharedFlag(flag.index);
    }

    @Override
    protected void setMeta(@NonNull EntityMetaFlag flag, boolean enabled) {
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
    public void setRotation(float yaw, float pitch) {
        if (offset != null) {
            yaw += offset.getYaw();
            pitch += offset.getPitch();
        }

        location.setYaw(yaw);
        location.setPitch(pitch);

        final Entity entity = this.entity;
        entity.setYHeadRot(yaw);
        entity.setYRot(yaw);
        entity.setXRot(pitch);

        final byte byteYaw = convertYaw(yaw);
        final var rotationPacket = new Rot(entity.getId(), byteYaw, convertPitch(pitch), false);
        final var headRotationPacket = new ClientboundRotateHeadPacket(entity, byteYaw);

        sendPackets(rotationPacket, headRotationPacket);

        if (type == EntityType.ARMOR_STAND || entity instanceof Display)
            updateMeta();
    }

    @Override
    public void setPositionRaw(double x, double y, double z, float yaw, float pitch) {
        final Entity entity = this.entity;
        final var teleportPacket = new ClientboundTeleportEntityPacket(entity);
        final var headRotationPacket = new ClientboundRotateHeadPacket(entity, convertYaw(yaw));

        sendPackets(teleportPacket, headRotationPacket);
    }

    @Override
    public void setPositionRotation(short dx, short dy, short dz, byte yaw, byte pitch) {
        final Entity entity = this.entity;
        final var positionRotationPacket = new PosRot(entity.getId(), dx, dy, dz, yaw, pitch, false);
        final var headRotationPacket = new ClientboundRotateHeadPacket(entity, convertYaw(yaw));

        sendPackets(positionRotationPacket, headRotationPacket);
    }


    // -----< Visibility >-----

    @Override
    public void show() {
        final int viewDistanceInBlocks = Bukkit.getServer().getViewDistance() * 16;
        final var nearbyPlayers = location.getWorld().getNearbyPlayers(location, viewDistanceInBlocks);
        if (nearbyPlayers.isEmpty()) return;

        final Entity entity = this.entity;
        final Vec3 pos = entity.position();

        final var spawnPacket = new ClientboundAddEntityPacket(entity.getId(), entity.getUUID(), pos.x, pos.y, pos.z, entity.getXRot(), entity.getYRot(), entity.getType(), entityData, Vec3.ZERO, entity.getYHeadRot());
        final var metaPacket = new ClientboundSetEntityDataPacket(entity.getId(), entity.getEntityData().packAll());
        final var headRotationPacket = new ClientboundRotateHeadPacket(entity, convertYaw(getYaw()));
        final var rotationPacket = new Rot(entity.getId(), convertYaw(getYaw()), convertPitch(getPitch()), false);
        final var equipmentPacket = getEquipmentPacket();

        for (final Player temp : nearbyPlayers) {
            final var connection = ((CraftPlayer) temp).getHandle().connection;

            if (!trackedByPlayers.add(connection)) continue;

            connection.send(spawnPacket);
            connection.send(metaPacket);
            connection.send(headRotationPacket);
            connection.send(rotationPacket);
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


    // -----< Equipment >-----

    @Override
    public void setEquipment(@NotNull org.bukkit.inventory.EquipmentSlot equipmentSlot, org.bukkit.inventory.ItemStack itemStack) {
        if (!type.isAlive())
            throw new IllegalStateException("Cannot set equipment for non living entity: " + type);

        final EquipmentSlot slot = switch (equipmentSlot) {
            case HAND -> EquipmentSlot.MAINHAND;
            case OFF_HAND -> EquipmentSlot.OFFHAND;
            case FEET -> EquipmentSlot.FEET;
            case CHEST -> EquipmentSlot.CHEST;
            case LEGS -> EquipmentSlot.LEGS;
            case HEAD -> EquipmentSlot.HEAD;
            case BODY -> EquipmentSlot.BODY;
        };

        final var livingEntity = (LivingEntity) entity;
        livingEntity.setItemSlot(slot, CraftItemStack.asNMSCopy(itemStack));
    }

    @Override
    public void updateEquipment() {
        final var packet = getEquipmentPacket();
        if (packet != null) sendPackets(packet);
    }

    private @Nullable ClientboundSetEquipmentPacket getEquipmentPacket() {
        if (!type.isAlive()) return null;

        final var livingEntity = (LivingEntity) entity;

        final List<Pair<EquipmentSlot, ItemStack>> equipmentList = new ArrayList<>(SLOTS.length);

        for (final EquipmentSlot slot : SLOTS) {
            final ItemStack stack = livingEntity.getItemBySlot(slot);
            if (stack.isEmpty()) continue;
            equipmentList.add(Pair.of(slot, stack));
        }
        return equipmentList.isEmpty()
            ? null
            : new ClientboundSetEquipmentPacket(entity.getId(), equipmentList);
    }


    // -----< Utils >-----
    private void sendPackets(@NotNull Packet<?>... packets) {
        if (packets.length == 0) return;

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
}