package org.dredd.bulletcore.config;

import org.bukkit.Material;
import org.dredd.bulletcore.BulletCore;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Utility class for storing all materials.
 *
 * @author dredd
 * @since 1.0.0
 */
public final class Materials {

    /**
     * Private constructor to prevent instantiation.
     */
    private Materials() {}

    // ----------< Attributes >----------

    public static final int TOTAL_MATERIALS;
    public static final Set<Material> ITEMS_ONLY;
    public static final Set<Material> BLOCKS_ONLY;
    public static final Set<Material> BLOCKS_COLLIDABLE;
    public static final Set<Material> BLOCKS_NON_COLLIDABLE;

    // ----------< Initialization >----------

    static {
        Material[] allMaterials = Material.values();
        TOTAL_MATERIALS = allMaterials.length;

        final Set<Material> itemsOnly = new LinkedHashSet<>();
        final Set<Material> blocksOnly = new LinkedHashSet<>();
        final Set<Material> blocksCollidable = new LinkedHashSet<>();
        final Set<Material> blocksNonCollidable = new LinkedHashSet<>();

        for (Material material : allMaterials) {
            boolean isItem = material.isItem();
            boolean isBlock = material.isBlock();

            if (isItem && !isBlock) {
                itemsOnly.add(material);
                continue;
            }

            if (!isItem && isBlock) {
                blocksOnly.add(material);
                continue;
            }

            // Both Item and Block
            if (material.isCollidable())
                blocksCollidable.add(material);
            else
                blocksNonCollidable.add(material);
        }

        ITEMS_ONLY = Collections.unmodifiableSet(itemsOnly);
        BLOCKS_ONLY = Collections.unmodifiableSet(blocksOnly);
        BLOCKS_COLLIDABLE = Collections.unmodifiableSet(blocksCollidable);
        BLOCKS_NON_COLLIDABLE = Collections.unmodifiableSet(blocksNonCollidable);

        writeToFile(new File(BulletCore.instance().getDataFolder(), "all-materials.yml"));
    }

    // ----------< Saving Utilities >----------

    private static void writeToFile(@NotNull File file) {
        StringBuilder sb = new StringBuilder();

        sb.append("totalMaterials: ").append(TOTAL_MATERIALS).append('\n');
        appendAsYamlList(sb, "itemsOnly", ITEMS_ONLY);
        appendAsYamlList(sb, "blocksOnly", BLOCKS_ONLY);
        appendAsYamlList(sb, "blocksCollidable", BLOCKS_COLLIDABLE);
        appendAsYamlList(sb, "blocksNonCollidable", BLOCKS_NON_COLLIDABLE);

        try {
            Files.writeString(file.toPath(), sb.toString());
        } catch (IOException e) {
            BulletCore.logError("Failed to write " + file.getName() + ": " + e.getMessage());
        }
    }

    private static void appendAsYamlList(@NotNull StringBuilder sb,
                                         @NotNull String name,
                                         @NotNull Set<Material> materials) {
        sb.append('\n')
            .append("# ").append(name).append(": ").append(materials.size()).append('\n')
            .append(name).append(":\n");

        materials.forEach(material ->
            sb.append("  - ").append(material.name()).append('\n')
        );
    }
}