package org.dredd.bulletcore.models.weapons.shooting.spray;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import static net.kyori.adventure.text.Component.*;
import static net.kyori.adventure.text.format.NamedTextColor.AQUA;
import static net.kyori.adventure.text.format.NamedTextColor.GOLD;
import static org.dredd.bulletcore.models.weapons.shooting.spray.MovementModifier.*;
import static org.dredd.bulletcore.models.weapons.shooting.spray.MovementState.*;
import static org.dredd.bulletcore.utils.ComponentUtils.WHITE;
import static org.dredd.bulletcore.utils.ComponentUtils.plainWhite;

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

    /**
     * Whether to send a spray info message to the player after the shot.
     */
    private boolean sendMessage;

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
    private boolean underwater;
    private boolean inWater;
    private boolean inVehicle;
    private boolean inFlight;
    private boolean inCrawlingPose;
    private boolean onClimbable;

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
        if (underwater) mods.add(UNDERWATER);
        if (inWater) mods.add(IN_WATER);
        if (inVehicle) mods.add(IN_VEHICLE);
        if (inFlight) mods.add(IN_FLIGHT);
        if (inCrawlingPose) mods.add(IN_CRAWLING_POSE);
        if (onClimbable) mods.add(ON_CLIMBABLE);
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
        sprinting = player.isSprinting() && !swimming; // do not trigger SPRINTING when SWIMMING
        sneaking = player.isSneaking();
        underwater = player.isUnderWater();
        inWater = player.isInWater() && !swimming && !underwater; // do not trigger IN_WATER when SWIMMING or UNDERWATER
        inVehicle = player.isInsideVehicle();
        inFlight = player.isFlying() && !inVehicle; // do not trigger IN_FLIGHT when IN_VEHICLE
        inCrawlingPose = !gliding && !swimming && player.getBoundingBox().getHeight() < 1.5D;
        onClimbable = player.isClimbing();

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

        // Post modifications to remove dependencies
        inVehicle = inVehicle && standing; // do not trigger IN_VEHICLE when RIDING
        inFlight = inFlight && standing; // do not trigger IN_FLIGHT when FLYING
        inCrawlingPose = inCrawlingPose && standing; // do not trigger IN_CRAWLING_POSE when CRAWLING
        onClimbable = onClimbable && standing; // do not trigger ON_CLIMBABLE when CLIMBING
    }

    /**
     * Sends a message to the player with the current state context.
     *
     * @param state     the movement state the player is currently in
     * @param modifiers the list of movement modifiers the player currently has
     * @param spray     the current spray value for the player
     */
    public void sendMessage(@NotNull MovementState state, @NotNull List<MovementModifier> modifiers, double spray) {
        if (!sendMessage) return;
        shot++;

        // Line 1: State
        Component stateLine = newline()
            .append(plainWhite(shot + ". State: "))
            .append(text(state.name(), GOLD));

        // Line 2: Modifiers (optional)
        Component modifiersLine = null;
        if (!modifiers.isEmpty()) {
            TextComponent.Builder base = text()
                .content(shot + ". Modifiers: ")
                .color(WHITE)
                .decoration(TextDecoration.ITALIC, false);

            var list = modifiers.stream()
                .map(modifier -> text(modifier.name(), modifier.color))
                .toList();

            modifiersLine = base.append(join(JoinConfiguration.arrayLike(), list)).build();
        }

        // Line 3: Spray
        Component sprayLine = plainWhite(shot + ". Spray: ")
            .append(text(df.format(spray), AQUA));

        // Combine all the lines into a single message
        Component fullMessage = join(
            JoinConfiguration.newlines(),
            modifiersLine != null
                ? List.of(stateLine, modifiersLine, sprayLine)
                : List.of(stateLine, sprayLine)
        );

        player.sendMessage(fullMessage);
    }

    /**
     * Sets whether to send a spray info message to the player after the shot.
     *
     * @param sendMessage whether to send a spray info message to the player
     */
    public void setSendMessage(boolean sendMessage) {
        this.sendMessage = sendMessage;
    }
}