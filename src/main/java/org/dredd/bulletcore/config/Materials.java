package org.dredd.bulletcore.config;

import org.bukkit.Material;
import org.bukkit.block.BlockType;
import org.dredd.bulletcore.BulletCore;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public final class Materials {

    private Materials() {}

    public static final int TOTAL;

    public static final Set<Material> ITEMS;
    public static final Set<Material> NO_COLLISION_BLOCKS;
    public static final Set<Material> COLLISION_BLOCKS;

    static {
        Material[] allMaterials = Material.values();
        TOTAL = allMaterials.length;

        Set<Material> items = new LinkedHashSet<>();
        Set<Material> noCollision = new LinkedHashSet<>();
        Set<Material> collision = new LinkedHashSet<>();

        for (var mat : allMaterials) {
            BlockType blockType = mat.asBlockType();
            if (blockType == null)
                items.add(mat);
            else if (blockType.hasCollision())
                collision.add(mat);
            else
                noCollision.add(mat);
        }

        ITEMS = Collections.unmodifiableSet(items);
        NO_COLLISION_BLOCKS = Collections.unmodifiableSet(noCollision);
        COLLISION_BLOCKS = Collections.unmodifiableSet(collision);

        writeToFile(new File(BulletCore.getInstance().getDataFolder(), "all-materials.yml"));
    }

    private static void writeToFile(@NotNull File file) {
        StringBuilder sb = new StringBuilder();

        sb.append("total: ").append(TOTAL).append("\n");
        appendAsYamlList(sb, "items", ITEMS);
        appendAsYamlList(sb, "noCollisionBlocks", NO_COLLISION_BLOCKS);
        appendAsYamlList(sb, "collisionBlocks", COLLISION_BLOCKS);

        try {
            Files.writeString(file.toPath(), sb.toString());
        } catch (IOException e) {
            BulletCore.getInstance().getLogger().severe(
                "Failed to write " + file.getName() + ": " + e.getMessage()
            );
        }
    }

    private static void appendAsYamlList(StringBuilder sb, String name, Set<Material> materials) {
        sb.append("\n")
            .append(name).append(": ").append(materials.size()).append("\n")
            .append(name).append(":\n");

        materials.forEach(material ->
            sb.append("  - ").append(material.name()).append("\n")
        );
    }
}