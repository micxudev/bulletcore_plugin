package org.dredd.bulletcore.models.weapons.skins;

import net.kyori.adventure.text.Component;

/**
 * Represents a weapon skin.
 *
 * @param name            the unique identifier for the skin for the given weapon
 * @param customModelData the custom model data used to render the skin in-game
 * @param displayName     the styled display name of the skin
 * @author dredd
 * @since 1.0.0
 */
public record WeaponSkin(
    String name,
    int customModelData,
    Component displayName
) {}