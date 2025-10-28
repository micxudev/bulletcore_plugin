package org.dredd.bulletcore.models.weapons;

import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.dredd.bulletcore.config.sounds.ConfiguredSound;
import org.dredd.bulletcore.config.sounds.SoundManager;
import org.dredd.bulletcore.models.weapons.reloading.SingleReloadHandler;
import org.jetbrains.annotations.NotNull;

import static org.bukkit.SoundCategory.MASTER;
import static org.dredd.bulletcore.config.sounds.SoundPlaybackMode.WORLD;

/**
 * Holds and manages all weapon-related sounds.
 * <p>
 * This class provides default sounds for each action, which can be overridden by configuration.
 * Sounds are loaded via {@link SoundManager} and can be played at a player's location with natural positioning.
 *
 * @author dredd
 * @since 1.0.0
 */
public final class WeaponSounds {

    // ----------< Static >----------

    // -----< Defaults >-----

    /**
     * Default sound played when a player fires a shot.
     */
    private static final ConfiguredSound FIRE = new ConfiguredSound(
        "entity.generic.explode", MASTER, 0.2f, 2f, 0L, WORLD
    );

    /**
     * Default sound played when a player starts reloading.
     */
    private static final ConfiguredSound RELOAD_START = new ConfiguredSound(
        "block.piston.extend", MASTER, 1.0f, 1.5f, 0L, WORLD
    );

    /**
     * Default sound played when a reload finishes.
     */
    private static final ConfiguredSound RELOAD_END = new ConfiguredSound(
        "block.piston.contract", MASTER, 1.0f, 1.5f, 0L, WORLD
    );

    /**
     * Default sound played when attempting to fire, but the magazine is empty.
     */
    private static final ConfiguredSound EMPTY = new ConfiguredSound(
        "block.lever.click", MASTER, 1.0f, 1.5f, 0L, WORLD
    );

    /**
     * Default sound played when a single bullet is inserted; used by {@link SingleReloadHandler}.
     */
    private static final ConfiguredSound ADD_BULLET = new ConfiguredSound(
        "block.tripwire.attach", MASTER, 1f, 1.5f, 0L, WORLD
    );

    // -----< Loader >-----

    /**
     * Loads weapon sounds from a YAML config, falling back to internal defaults if loading fails.
     *
     * @param config the YAML configuration to load from
     * @return a fully initialized {@code WeaponSounds} object
     */
    public static @NotNull WeaponSounds load(@NotNull YamlConfiguration config) {
        return new WeaponSounds(config);
    }


    // ----------< Instance >----------

    // -----< Attributes >-----

    /**
     * Sound played when a shot is fired.
     */
    public final ConfiguredSound fire;

    /**
     * Sound played when reload begins.
     */
    public final ConfiguredSound reloadStart;

    /**
     * Sound played when reload ends.
     */
    public final ConfiguredSound reloadEnd;

    /**
     * Sound played when trying to shoot, but the magazine is empty.
     */
    public final ConfiguredSound empty;

    /**
     * Sound played when inserting a single bullet (for single reload logic).
     */
    public final ConfiguredSound addBullet;

    // -----< Construction >-----

    /**
     * Private constructor. Use {@link #load(YamlConfiguration)} instead.
     */
    private WeaponSounds(@NotNull YamlConfiguration config) {
        this.fire = SoundManager.loadSound(config, "fire", FIRE);
        this.reloadStart = SoundManager.loadSound(config, "reload_start", RELOAD_START);
        this.reloadEnd = SoundManager.loadSound(config, "reload_end", RELOAD_END);
        this.empty = SoundManager.loadSound(config, "empty", EMPTY);
        this.addBullet = SoundManager.loadSound(config, "add_bullet", ADD_BULLET);
    }

    // -----< Public API >-----

    /**
     * Plays the specified sound at the center of the given player to create a more immersive sound origin.
     *
     * @param player the player at whose location the sound should be played
     * @param sound  the configured sound to play
     */
    public void play(@NotNull Player player, @NotNull ConfiguredSound sound) {
        final Location location = player.getLocation();
        location.setY(location.getY() + player.getHeight() / 2);
        SoundManager.playSound(player, location, sound);
    }
}