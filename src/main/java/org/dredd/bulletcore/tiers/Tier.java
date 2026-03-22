package org.dredd.bulletcore.tiers;

import net.kyori.adventure.text.Component;

public record Tier(
    String name,
    int points,
    Component displayName
) {

    public static final Tier EMPTY = new Tier("EMPTY", -1, Component.empty());
}