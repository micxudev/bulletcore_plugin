package org.dredd.bulletcore.tiers;

/**
 * Tier kit definition.
 *
 * @param name unique kit identifier
 * @param icon icon string (e.g., unicode, optionally replaced via resource pack)
 * @param displayName formatted display name
 * @param topSize maximum number of entries in the top list
 *
 * @author dredd
 * @since 1.0.0
 */
public record TierKit(
    String name,
    String icon,
    String displayName,
    int topSize
) {}