package org.dredd.bulletcore.models.weapons.shooting.recoil;

import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

/**
 * Defines the parameters used to represent weapon recoil.
 *
 * @author dredd
 * @since 1.0.0
 */
public final class WeaponRecoil {

    // ----------< Static >----------

    // -----< Loader >-----

    /**
     * Loads a {@link WeaponRecoil} from config, defaulting and clamping to the specified values.
     *
     * @param config the YAML configuration to load from
     * @return a new {@link WeaponRecoil} instance
     */
    public static @NotNull WeaponRecoil load(@NotNull YamlConfiguration config) {
        return new WeaponRecoil(config);
    }


    // ----------< Instance >----------

    // -----< Attributes >-----

    /**
     * Base horizontal recoil applied per shot, in degrees.<br>
     * Positive = pulls right, Negative = pulls left.
     */
    public final float meanX;

    /**
     * Base vertical recoil applied per shot, in degrees.<br>
     * Positive = pulls up, Negative = pulls down.
     */
    public final float meanY;

    /**
     * Adds a randomized offset to the horizontal recoil on each shot, centered around {@link #meanX}.<br>
     * For example, if {@code meanX = 0.5} and {@code varianceX = 0.3}, the actual recoil per shot may vary between {@code 0.2} and {@code 0.8}.<br>
     * <b>Allowed Range:</b> [0.0 – 90.0]<br>
     * <b>Effect:</b> Higher values introduce more unpredictability in horizontal aim offset.
     */
    public final float varianceX;

    /**
     * Adds a randomized offset to the vertical recoil on each shot, centered around {@link #meanY}.<br>
     * For example, if {@code meanY = 1.0} and {@code varianceY = 0.2}, the actual recoil per shot may vary between {@code 0.8} and {@code 1.2}.<br>
     * <b>Allowed Range:</b> [0.0 – 90.0]<br>
     * <b>Effect:</b> Higher values introduce more vertical aim randomness.
     */
    public final float varianceY;

    /**
     * Scales how aggressively the recoil is applied to the player's aim each tick.<br>
     * A higher value results in a snappier, more forceful kickback effect.<br>
     * <br>
     * <b>Typical range:</b> 1.0 – 4.0<br>
     * <b>Effect:</b> Controls the speed of the visual recoil response.
     */
    public final float speed;

    /**
     * Interpolation factor used to smoothly transition the player's current view angle toward the target.<br>
     * Lower values produce smoother movement but increase the number of sent packets.<br>
     * <br>
     * <b>Allowed range:</b> [0.0 – 1.0]<br>
     * <b>Typical range:</b> 0.25 – 0.8<br>
     * <b>Effect:</b> Controls how 'smooth' the recoil feels over time.
     */
    public final float lerpFactor;

    /**
     * Percentage of the remaining target recoil that is decayed each tick to prevent infinite stacking.<br>
     * For example, a value of {@code 0.1} reduces the target recoil by 10% every tick.<br>
     * <br>
     * <b>Allowed range:</b> [0.0 – 1.0]<br>
     * <b>Typical range:</b> 0.05 – 0.3<br>
     * <b>Effect:</b> Controls how quickly accumulated recoil fades out.
     */
    public final float damping;

    /**
     * Determines how far the camera should recover from the total applied recoil back toward the origin.<br>
     * For example, if the total recoil applied is {@code 10.0} and {@code recoveryPercent = 0.9}, the camera will recover {@code 9.0} degrees, stopping at {@code 1.0}.<br>
     * <br>
     * <b>Allowed range:</b> [0.0 – 1.0]<br>
     * <b>Typical range:</b> 0.75 – 0.95<br>
     * <b>Effect:</b> Controls how much of the recoil is recovered during the recovery phase.
     */
    public final float recoveryPercent;

    // TEST VALUES
    /**
     * The number of ticks required to reduce the target recoil by {@link #recoveryPercent} based on {@link #damping}.
     */
    public final int ticksToRecoverTarget;

    /**
     * Estimated number of ticks to fully recover from recoil, including both the decay of the target recoil
     * (via {@link #damping}) and the smoothing interpolation toward the final position (via {@link #lerpFactor}).
     */
    public final int totalTicksToRecover;
    // END TEST VALUES

    // -----< Construction >-----

    /**
     * Private constructor. Use {@link #load(YamlConfiguration)} instead.
     */
    private WeaponRecoil(@NotNull YamlConfiguration config) {
        this.meanX = (float) config.getDouble("recoil.meanX", 0.5D);
        this.meanY = (float) config.getDouble("recoil.meanY", 2.0D);
        this.varianceX = Math.clamp((float) config.getDouble("recoil.varianceX", 0.0D), 0.0f, 90.0f);
        this.varianceY = Math.clamp((float) config.getDouble("recoil.varianceY", 0.0D), 0.0f, 90.0f);
        this.speed = (float) config.getDouble("recoil.speed", 1.0D);
        this.lerpFactor = Math.clamp((float) config.getDouble("recoil.lerpFactor", 0.5D), 0.0f, 1.0f);
        this.damping = Math.clamp((float) config.getDouble("recoil.damping", 0.125D), 0.0f, 1.0f);
        this.recoveryPercent = Math.clamp((float) config.getDouble("recoil.recoveryPercent", 0.5D), 0.0f, 1.0f);

        // TEST VALUES
        final float MIN_ZERO = 0.0001f;
        final float MAX_ONE = 0.9999f;

        final float safeDamping = Math.clamp(damping, MIN_ZERO, MAX_ONE);
        final float safeLerp = Math.clamp(lerpFactor, MIN_ZERO, MAX_ONE);
        final float safeRecoveryPercent = Math.clamp(recoveryPercent, MIN_ZERO, MAX_ONE);

        final double dTicksToRecoverTarget = Math.log10(1.0f - safeRecoveryPercent) / Math.log10(1.0f - safeDamping);
        this.ticksToRecoverTarget = (int) Math.ceil(dTicksToRecoverTarget);

        final double dSmoothingTicks = Math.log10(0.01f) / Math.log10(1.0f - safeLerp);
        this.totalTicksToRecover = (int) Math.ceil(dTicksToRecoverTarget + dSmoothingTicks);
        // END TEST VALUES
    }
}