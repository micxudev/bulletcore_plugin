package org.dredd.bulletcore.tiers;

import net.kyori.adventure.text.Component;

public record Tier(
    String name,
    int points,
    Component displayName
) {}