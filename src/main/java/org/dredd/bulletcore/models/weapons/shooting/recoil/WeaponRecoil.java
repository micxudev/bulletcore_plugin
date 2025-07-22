package org.dredd.bulletcore.models.weapons.shooting.recoil;

import org.bukkit.configuration.file.FileConfiguration;
import org.dredd.bulletcore.utils.MathUtils;
import org.jetbrains.annotations.NotNull;

/**
 * Defines the parameters used to represent weapon recoil.
 *
 * @author dredd
 * @since 1.0.0
 */
public class WeaponRecoil {

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
     * <b>Allowed Range:</b> ≥ 0.0<br>
     * <b>Effect:</b> Higher values introduce more unpredictability in horizontal aim offset.
     */
    public final float varianceX;

    /**
     * Adds a randomized offset to the vertical recoil on each shot, centered around {@link #meanY}.<br>
     * For example, if {@code meanY = 1.0} and {@code varianceY = 0.2}, the actual recoil per shot may vary between {@code 0.8} and {@code 1.2}.<br>
     * <b>Allowed Range:</b> ≥ 0.0<br>
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
     * The number of ticks required to reduce the target recoil by {@link #recoveryPercent} based on {@link #damping}.<br>
     */
    public final int ticksToRecoverTarget;

    /**
     * Estimated number of ticks to fully recover from recoil, including both the decay of the target recoil
     * (via {@link #damping}) and the smoothing interpolation toward the final position (via {@link #lerpFactor}).
     */
    public final int totalTicksToRecover;
    // END TEST VALUES

    /**
     * Constructs a new {@link WeaponRecoil} instance.
     * <p>
     * All parameters must be already validated.
     */
    private WeaponRecoil(float meanX, float meanY,
                         float varianceX, float varianceY,
                         float speed, float lerpFactor,
                         float damping, float recoveryPercent) {
        this.meanX = meanX;
        this.meanY = meanY;
        this.varianceX = varianceX;
        this.varianceY = varianceY;
        this.speed = speed;
        this.lerpFactor = lerpFactor;
        this.damping = damping;
        this.recoveryPercent = recoveryPercent;

        // TEST VALUES
        float MIN_ZERO = 0.0001f;
        float MAX_ONE = 0.9999f;

        float safeDamping = MathUtils.clamp(damping, MIN_ZERO, MAX_ONE);
        float safeLerp = MathUtils.clamp(lerpFactor, MIN_ZERO, MAX_ONE);
        float safeRecoveryPercent = MathUtils.clamp(recoveryPercent, MIN_ZERO, MAX_ONE);

        double dTicksToRecoverTarget = Math.log10(1.0f - safeRecoveryPercent) / Math.log10(1.0f - safeDamping);
        this.ticksToRecoverTarget = (int) Math.ceil(dTicksToRecoverTarget);

        double dSmoothingTicks = Math.log10(0.01f) / Math.log10(1.0f - safeLerp);
        this.totalTicksToRecover = (int) Math.ceil(dTicksToRecoverTarget + dSmoothingTicks);
        // END TEST VALUES
    }

    /**
     * Loads a {@link WeaponRecoil} from config, defaulting and clamping to the specified values.
     *
     * @param cfg the YAML configuration to load from
     * @return a new {@link WeaponRecoil} instance
     */
    public static @NotNull WeaponRecoil load(@NotNull FileConfiguration cfg) {
        String s = "recoil.";
        return new WeaponRecoil(
            (float) cfg.getDouble(s + "meanX", 0.5),
            (float) cfg.getDouble(s + "meanY", 2.0),
            MathUtils.clamp((float) cfg.getDouble(s + "varianceX", 0.0), 0.0f, Float.MAX_VALUE),
            MathUtils.clamp((float) cfg.getDouble(s + "varianceY", 0.0), 0.0f, Float.MAX_VALUE),
            (float) cfg.getDouble(s + "speed", 1.0),
            MathUtils.clamp((float) cfg.getDouble(s + "lerpFactor", 0.5), 0.0f, 1.0f),
            MathUtils.clamp((float) cfg.getDouble(s + "damping", 0.125), 0.0f, 1.0f),
            MathUtils.clamp((float) cfg.getDouble(s + "recoveryPercent", 0.75), 0.0f, 1.0f)
        );
    }
}