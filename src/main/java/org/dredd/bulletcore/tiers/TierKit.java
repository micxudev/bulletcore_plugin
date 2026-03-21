package org.dredd.bulletcore.tiers;

import net.kyori.adventure.text.Component;

public record TierKit(
    String name,
    String icon,
    Component displayName
) {}