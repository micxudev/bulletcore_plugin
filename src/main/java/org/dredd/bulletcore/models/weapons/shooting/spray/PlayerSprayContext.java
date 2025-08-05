package org.dredd.bulletcore.models.weapons.shooting.spray;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import static org.dredd.bulletcore.models.weapons.shooting.spray.MovementModifier.*;
import static org.dredd.bulletcore.models.weapons.shooting.spray.MovementState.*;
import static org.dredd.bulletcore.utils.ComponentUtils.WHITE;
import static org.dredd.bulletcore.utils.ComponentUtils.noItalic;

/**
 * Tracks and updates the state context for a specific player.
 *
 * @author dredd
 * @since 1.0.0
 */
public class PlayerSprayContext {

    /**
     * Decimal formatter for spray values.
     */
    private static final DecimalFormat df = new DecimalFormat("#.##");

    /**
     * The player this state context instance is associated with.
     */
    private final Player player;

    /**
     * Player location from the last tick.
     */
    private Location lastTickLocation;

    /**
     * Player vertical velocity from the last tick.
     */
    private double lastTickVelocityY;

    /**
     * Whether the player was standing on the previous tick.
     */
    private boolean wasStanding;

    /**
     * Shot number since this instance was created.<br>
     * Used to display shot number in messages.
     */
    private int shot;

    // States
    private boolean gliding;
    private boolean swimming;
    private boolean jumping;
    private boolean standing;
    private boolean crawling;
    private boolean riding;
    private boolean flying;
    private boolean climbing;
    private boolean walking;

    // Modifiers
    private boolean sprinting;
    private boolean sneaking;
    private boolean onClimbable;
    private boolean inWater;
    private boolean underwater;
    private boolean inVehicle;
    private boolean inFlight;
    private boolean inCrawlingPose;

    public PlayerSprayContext(@NotNull Player player) {
        this.player = player;
        this.lastTickLocation = player.getLocation();
    }

    /**
     * Gets the current {@link MovementState} of the player.<br>
     * Remark: checking order is important, do not rearrange.
     *
     * @return the current {@link MovementState} of the player
     * @throws IllegalStateException if the player is in an invalid state
     */
    public @NotNull MovementState getState() {
        if (gliding) return GLIDING;
        if (swimming) return SWIMMING;
        if (jumping) return JUMPING;
        if (standing) return STANDING;
        if (crawling) return CRAWLING;
        if (riding) return RIDING;
        if (flying) return FLYING;
        if (climbing) return CLIMBING;
        if (walking) return WALKING;
        throw new IllegalStateException("Player is in invalid state");
    }

    /**
     * Gets the list of the active {@link MovementModifier}s for the player.
     *
     * @return the list of {@link MovementModifier}s for the player
     */
    public @NotNull List<MovementModifier> getModifiers() {
        List<MovementModifier> mods = new ArrayList<>(MovementModifier.values().length);
        if (sprinting) mods.add(SPRINTING);
        if (sneaking) mods.add(SNEAKING);
        if (onClimbable) mods.add(ON_CLIMBABLE);
        if (inWater) mods.add(IN_WATER);
        if (underwater) mods.add(UNDERWATER);
        if (inVehicle) mods.add(IN_VEHICLE);
        if (inFlight) mods.add(IN_FLIGHT);
        if (inCrawlingPose) mods.add(IN_CRAWLING_POSE);
        return mods;
    }

    /**
     * Checks if two locations are the same based on their coordinates excluding rotations.
     *
     * @param l1 first location
     * @param l2 second location
     * @return true if the locations are the same coordinates, false otherwise
     */
    private boolean isSamePosition(@NotNull Location l1, @NotNull Location l2) {
        return l1.getX() == l2.getX() && l1.getY() == l2.getY() && l1.getZ() == l2.getZ();
    }

    /**
     * Updates all the {@link MovementState}s and {@link MovementModifier}s for the current player.
     */
    public void tick() {
        // Independent states
        gliding = player.isGliding();
        swimming = player.isSwimming();

        // Modifiers
        sprinting = player.isSprinting();
        sneaking = player.isSneaking();
        onClimbable = player.isClimbing();
        inWater = player.isInWater();
        underwater = player.isUnderWater();
        inVehicle = player.isInsideVehicle();
        inFlight = player.isFlying() && !inVehicle; // do not trigger IN_FLIGHT when IN_VEHICLE
        inCrawlingPose = !gliding && !inWater && player.getBoundingBox().getHeight() < 1.5D;

        // Jumping state
        {
            double currentVelocityY = player.getVelocity().getY();
            float fallDistance = player.getFallDistance();
            boolean ascending = currentVelocityY > 0.0 || Math.abs(currentVelocityY) > 0.1;
            boolean falling = fallDistance > 0.0f;
            boolean apex = lastTickVelocityY > 0.0 && currentVelocityY < 0.0 && fallDistance == 0.0f;
            jumping = !onClimbable && !underwater && !inFlight && (ascending || falling || apex);
            lastTickVelocityY = currentVelocityY;
        }

        // Standing state
        {
            Location currentLocation = player.getLocation();
            boolean samePosition = isSamePosition(currentLocation, lastTickLocation);
            standing = samePosition && wasStanding;
            lastTickLocation = currentLocation;
            wasStanding = samePosition;
        }

        // Other states
        crawling = inCrawlingPose;
        riding = inVehicle;
        flying = inFlight;
        climbing = onClimbable;
        walking = !inVehicle;
    }

    // START TEST_MESSAGE
    public void sendMessage(@NotNull MovementState state, @NotNull List<MovementModifier> modifiers, double spray) {
        shot++;
        player.sendMessage(Component.newline().append(noItalic(shot + ". State: " + state, WHITE)));
        if (!modifiers.isEmpty()) player.sendMessage(getModifiersMessage(shot, modifiers));
        player.sendMessage(noItalic(shot + ". Spray: " + df.format(spray), WHITE));
    }

    private Component getModifiersMessage(int shot, @NotNull List<MovementModifier> modifiers) {
        TextComponent.Builder base = Component.text()
            .content(shot + ". Modifiers: ")
            .color(WHITE)
            .decoration(TextDecoration.ITALIC, false);

        modifiers.forEach(modifier -> base.append(noItalic(modifier + " ", modifier.color)));
        return base.build();
    }
    // END TEST_MESSAGE
}