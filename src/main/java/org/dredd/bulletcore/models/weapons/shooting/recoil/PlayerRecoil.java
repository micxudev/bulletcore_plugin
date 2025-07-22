package org.dredd.bulletcore.models.weapons.shooting.recoil;

import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static net.minecraft.world.entity.RelativeMovement.ALL;
import static org.dredd.bulletcore.utils.MathUtils.*;

/**
 * Tracks and updates the recoil state for a specific player.<br>
 * Handles camera rotation after shots and automatically stops when the recoil is complete or inactive.
 *
 * @author dredd
 * @since 1.0.0
 */
public class PlayerRecoil {

    /**
     * Number of consecutive ticks without sending camera movement packets before the recoil task stops.
     */
    private static final int NO_SENT_PACKET_IN_A_ROW_TO_STOP = 3;

    /**
     * Tracks consecutive ticks where no movement packet was sent.
     */
    private int noSentPacketInARow;

    /**
     * Number of ticks since the last shot while this recoil task has been active.
     */
    private int taskTicks;

    /**
     * The player this recoil instance is associated with.
     */
    private final Player player;

    // START: Recoil parameters from the weapon used in the last shot
    /**
     * Recoil speed multiplier from the weapon's profile. See {@link WeaponRecoil#speed}.
     */
    private float speed;

    /**
     * Recoil smoothing factor from the weapon's profile. See {@link WeaponRecoil#lerpFactor}.
     */
    private float lerpFactor;

    /**
     * Recoil decay rate from the weapon's profile. See {@link WeaponRecoil#damping}.
     */
    private float damping;

    // TEST VALUES
    /**
     * Estimated ticks required to reduce target recoil by {@link WeaponRecoil#recoveryPercent}.
     */
    private int ticksToRecoverTarget;

    /**
     * Estimated total ticks needed to fully apply and recover from recoil.
     */
    private int totalTicksToRecover;
    // END TEST VALUES
    // END

    /**
     * Accumulated horizontal recoil (target offset), in degrees.
     */
    private float targetRecoilX;

    /**
     * Accumulated vertical recoil (target offset), in degrees.
     */
    private float targetRecoilY;

    /**
     * Current horizontal recoil being interpolated toward the target, in degrees.
     */
    private float currentRecoilX;

    /**
     * Current vertical recoil being interpolated toward the target, in degrees.
     */
    private float currentRecoilY;

    /**
     * Constructs a new {@link PlayerRecoil} instance.
     *
     * @param player the owner of the current instance
     */
    public PlayerRecoil(@NotNull Player player) {
        this.player = player;
    }

    /**
     * Applies new recoil parameters and adds random target recoil based on the fired weapon's profile.
     *
     * @param recoil the recoil data from the fired weapon
     */
    public void onShotFired(@NotNull WeaponRecoil recoil) {
        speed = recoil.speed;
        lerpFactor = recoil.lerpFactor;
        damping = recoil.damping;
        ticksToRecoverTarget = recoil.ticksToRecoverTarget;
        totalTicksToRecover = recoil.totalTicksToRecover;

        targetRecoilX += variance(recoil.meanX, recoil.varianceX);
        targetRecoilY += variance(recoil.meanY, recoil.varianceY);

        taskTicks = 0;
    }

    /**
     * Updates the player's camera to simulate recoil for this tick.<br>
     * Automatically stops when recoil is fully applied or becomes negligible.
     */
    public void tick() {
        // TEST VALUES
        if (++taskTicks >= totalTicksToRecover) {
            //System.out.println("Stopped in " + taskTicks + " ticks using 'RECOVERING'");
            clearAndStop();
            return;
        }
        // END TEST VALUES

        if (taskTicks <= ticksToRecoverTarget) {
            targetRecoilX *= (1.0f - damping);
            targetRecoilY *= (1.0f - damping);
        }

        float oldX = currentRecoilX;
        float oldY = currentRecoilY;

        currentRecoilX = lerp(currentRecoilX, targetRecoilX, lerpFactor);
        currentRecoilY = lerp(currentRecoilY, targetRecoilY, lerpFactor);

        float deltaYaw = (currentRecoilX - oldX) * speed;
        float deltaPitch = (currentRecoilY - oldY) * speed;

        // Move camera if there is a significant change in yaw or pitch
        if (!approximatelyZero(deltaYaw, 0.01f) || !approximatelyZero(deltaPitch, 0.01f)) {
            noSentPacketInARow = 0;
            modifyCameraRotation(player, deltaYaw, -deltaPitch);
        } else {
            // If there was no movement for a couple of ticks, stop the recoil task
            if (++noSentPacketInARow == NO_SENT_PACKET_IN_A_ROW_TO_STOP) {
                //System.out.println("Stopped in " + taskTicks + " ticks using 'NOT SENDING PACKETS'");
                clearAndStop();
            }
        }
    }

    /**
     * Resets all accumulated recoil values to zero.
     */
    private void clearAccumulatedRecoil() {
        currentRecoilX = 0.0f;
        currentRecoilY = 0.0f;
        targetRecoilX = 0.0f;
        targetRecoilY = 0.0f;
    }

    /**
     * Clears all the recoil and stops the active recoil task for this player.
     */
    private void clearAndStop() {
        clearAccumulatedRecoil();
        RecoilHandler.stopRecoilTask(player);
    }

    /**
     * Applies a relative camera rotation to the player by sending a position packet.
     *
     * @param player the player to affect
     * @param yaw    horizontal rotation delta (X axis), in degrees
     * @param pitch  vertical rotation delta (Y axis), in degrees
     */
    private void modifyCameraRotation(@NotNull Player player, float yaw, float pitch) {
        // 1.20.6 only (multiversion later)
        var packet = new ClientboundPlayerPositionPacket(0, 0, 0, yaw, pitch, ALL, 0);
        ((CraftPlayer) player).getHandle().connection.send(packet);
    }
}