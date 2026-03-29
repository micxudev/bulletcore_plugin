package org.dredd.bulletcore.tiers;

/**
 * Tier definition.
 *
 * @param name unique tier identifier
 * @param points points granted by this tier
 * @param displayName formatted display name
 *
 * @author dredd
 * @since 1.0.0
 */
public record Tier(
    String name,
    int points,
    String displayName
) {}