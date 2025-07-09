package org.dredd.bulletcore.models.weapons.skins;

import net.kyori.adventure.text.Component;

/**
 * Represents a weapon skin.
 *
 * @param skinModelData   the custom model data used to render the skin in-game
 * @param skinDisplayName the styled display name of the skin
 * @author dredd
 * @since 1.0.0
 */
public record WeaponSkin(
    int skinModelData,
    Component skinDisplayName
) {}