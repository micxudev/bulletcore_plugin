package org.dredd.bulletcore.models.weapons.shooting.recoil;

import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.dredd.bulletcore.utils.MathUtils;
import org.jetbrains.annotations.NotNull;

import static net.minecraft.world.entity.RelativeMovement.ALL;

/**
 * Tracks and updates the recoil state for a specific player.<br>
 * Handles camera rotation after shots and automatically stops when the recoil is complete or inactive.
 *
 * @author dredd
 * @since 1.0.0
 */
public final class PlayerRecoil {

    // ----------< Static >----------

    /**
     * Number of consecutive ticks without sending camera movement packets before the recoil task stops.
     */
    private static final int NO_SENT_PACKET_IN_A_ROW_TO_STOP = 3;


    // ----------< Instance >----------

    // -----< Attributes >-----

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
    // END: Recoil parameters from the weapon used in the last shot

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

    // -----< Construction >-----

    /**
     * Constructs a new {@link PlayerRecoil} instance.
     *
     * @param player the owner of the current instance
     */
    public PlayerRecoil(@NotNull Player player) {
        this.player = player;
    }

    // -----< Public API >-----

    /**
     * Applies new recoil parameters and adds random target recoil based on the fired weapon's profile.
     *
     * @param recoil the recoil data from the fired weapon
     */
    public void onShotFired(@NotNull WeaponRecoil recoil) {
        noSentPacketInARow = 0;
        taskTicks = 0;

        speed = recoil.speed;
        lerpFactor = recoil.lerpFactor;
        damping = recoil.damping;
        ticksToRecoverTarget = recoil.ticksToRecoverTarget;
        totalTicksToRecover = recoil.totalTicksToRecover;

        targetRecoilX += MathUtils.variance(recoil.meanX, recoil.varianceX);
        targetRecoilY += MathUtils.variance(recoil.meanY, recoil.varianceY);
    }

    /**
     * Updates the player's camera to simulate recoil for this tick.<br>
     * Automatically stops the recoil task when recoil is fully applied or becomes negligible.
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

        currentRecoilX = MathUtils.lerp(currentRecoilX, targetRecoilX, lerpFactor);
        currentRecoilY = MathUtils.lerp(currentRecoilY, targetRecoilY, lerpFactor);

        float deltaYaw = (currentRecoilX - oldX) * speed;
        float deltaPitch = (currentRecoilY - oldY) * speed;

        // Move camera if there is a significant change in yaw or pitch
        if (!MathUtils.approximatelyZero(deltaYaw, 0.01f) ||
            !MathUtils.approximatelyZero(deltaPitch, 0.01f)) {
            noSentPacketInARow = 0;
            modifyCameraRotation(player, deltaYaw, -deltaPitch);
        } else {
            if (++noSentPacketInARow >= NO_SENT_PACKET_IN_A_ROW_TO_STOP) {
                //System.out.println("Stopped in " + taskTicks + " ticks using 'NOT SENDING PACKETS'");
                clearAndStop();
            }
        }
    }

    // -----< Internal Utilities >-----

    /**
     * Clears the accumulated recoil and stops the recoil task for this player.
     */
    private void clearAndStop() {
        targetRecoilX = 0.0f;
        targetRecoilY = 0.0f;
        currentRecoilX = 0.0f;
        currentRecoilY = 0.0f;

        RecoilHandler.cancelRecoilTask(player);
    }

    /**
     * Applies a relative camera rotation to the player by sending a position packet.
     *
     * @param player the player to affect
     * @param yaw    horizontal rotation delta (X axis), in degrees
     * @param pitch  vertical rotation delta (Y axis), in degrees
     */
    private void modifyCameraRotation(@NotNull Player player,
                                      float yaw,
                                      float pitch) {
        // tested on 1.20.6, 1.21.1 - (multiversion later)
        var packet = new ClientboundPlayerPositionPacket(0, 0, 0, yaw, pitch, ALL, 0);
        ((CraftPlayer) player).getHandle().connection.send(packet);
    }
}