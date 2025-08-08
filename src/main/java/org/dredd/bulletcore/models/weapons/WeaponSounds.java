package org.dredd.bulletcore.models.weapons;

import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.dredd.bulletcore.config.sounds.ConfiguredSound;
import org.dredd.bulletcore.config.sounds.SoundManager;
import org.dredd.bulletcore.models.weapons.reloading.SingleReloadHandler;
import org.jetbrains.annotations.NotNull;

import static org.bukkit.SoundCategory.MASTER;

/**
 * Holds and manages all weapon-related sounds.
 * <p>
 * This class provides default sounds for each action, which can be overridden by configuration.
 * Sounds are loaded via {@link SoundManager} and can be played at a player's location with natural positioning.
 *
 * @author dredd
 * @since 1.0.0
 */
public class WeaponSounds {

    // -----< Defaults >-----

    /**
     * Default sound played when a player fires a shot.
     */
    private static final ConfiguredSound FIRE = new ConfiguredSound("entity.generic.explode", MASTER, 0.2f, 2f, 0L);

    /**
     * Default sound played when a player starts reloading.
     */
    private static final ConfiguredSound RELOAD_START = new ConfiguredSound("block.piston.extend", MASTER, 1.0f, 1.5f, 0L);

    /**
     * Default sound played when a reload finishes.
     */
    private static final ConfiguredSound RELOAD_END = new ConfiguredSound("block.piston.contract", MASTER, 1.0f, 1.5f, 0L);

    /**
     * Default sound played when attempting to fire, but the magazine is empty.
     */
    private static final ConfiguredSound EMPTY = new ConfiguredSound("block.lever.click", MASTER, 1.0f, 1.5f, 0L);

    /**
     * Default sound played when a single bullet is inserted; used by {@link SingleReloadHandler}.
     */
    private static final ConfiguredSound ADD_BULLET = new ConfiguredSound("block.tripwire.attach", MASTER, 1f, 1.5f, 0L);


    // -----< Per weapon >-----

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

    /**
     * Constructs a new {@code WeaponSounds} instance.
     */
    private WeaponSounds(ConfiguredSound fire,
                         ConfiguredSound reloadStart,
                         ConfiguredSound reloadEnd,
                         ConfiguredSound empty,
                         ConfiguredSound addBullet) {
        this.fire = fire;
        this.reloadStart = reloadStart;
        this.reloadEnd = reloadEnd;
        this.empty = empty;
        this.addBullet = addBullet;
    }

    /**
     * Loads weapon sounds from a YAML config, falling back to internal defaults if loading fails.
     *
     * @param weaponConfig the YAML configuration to load from
     * @return a fully initialized {@code WeaponSounds} object
     */
    public static @NotNull WeaponSounds load(@NotNull YamlConfiguration weaponConfig) {
        return new WeaponSounds(
            SoundManager.loadSound(weaponConfig, "fire", FIRE),
            SoundManager.loadSound(weaponConfig, "reload_start", RELOAD_START),
            SoundManager.loadSound(weaponConfig, "reload_end", RELOAD_END),
            SoundManager.loadSound(weaponConfig, "empty", EMPTY),
            SoundManager.loadSound(weaponConfig, "add_bullet", ADD_BULLET)
        );
    }

    /**
     * Plays the specified sound at the center of the given player to create a more immersive sound origin.
     *
     * @param player the player at whose location the sound should be played
     * @param sound  the configured sound to play
     */
    public void play(@NotNull Player player, @NotNull ConfiguredSound sound) {
        Location location = player.getLocation();
        location.setY(location.getY() + player.getHeight() / 2);
        SoundManager.playSound(player.getWorld(), location, sound);
    }
}