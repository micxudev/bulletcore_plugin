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

    public static final int TOTAL_MATERIALS;
    public static final Set<Material> OBTAINABLE_ITEMS;
    public static final Set<Material> NOT_OBTAINABLE_MATERIALS;
    public static final Set<Material> COLLISION_BLOCKS;
    public static final Set<Material> NO_COLLISION_BLOCKS;

    static {
        Material[] allMaterials = Material.values();
        TOTAL_MATERIALS = allMaterials.length;

        Set<Material> obtainableItems = new LinkedHashSet<>();
        Set<Material> notObtainableMaterials = new LinkedHashSet<>();
        Set<Material> collisionBlocks = new LinkedHashSet<>();
        Set<Material> noCollisionBlocks = new LinkedHashSet<>();

        for (Material material : allMaterials) {
            categorizeMaterial(
                material,
                obtainableItems,
                notObtainableMaterials,
                collisionBlocks,
                noCollisionBlocks
            );
        }

        OBTAINABLE_ITEMS = Collections.unmodifiableSet(obtainableItems);
        NOT_OBTAINABLE_MATERIALS = Collections.unmodifiableSet(notObtainableMaterials);
        COLLISION_BLOCKS = Collections.unmodifiableSet(collisionBlocks);
        NO_COLLISION_BLOCKS = Collections.unmodifiableSet(noCollisionBlocks);

        writeToFile(new File(BulletCore.instance().getDataFolder(), "all-materials.yml"));
    }

    private static void categorizeMaterial(
        Material material,
        Set<Material> obtainable,
        Set<Material> notObtainable,
        Set<Material> collision,
        Set<Material> noCollision) {

        if (!material.isItem()) {
            notObtainable.add(material);
            return;
        }

        BlockType blockType = material.asBlockType();
        if (blockType == null) {
            obtainable.add(material);
            return;
        }

        if (blockType.hasCollision()) {
            collision.add(material);
        } else {
            noCollision.add(material);
        }
    }

    private static void writeToFile(@NotNull File file) {
        StringBuilder sb = new StringBuilder();

        sb.append("totalMaterials: ").append(TOTAL_MATERIALS).append("\n");
        appendAsYamlList(sb, "obtainableItems", OBTAINABLE_ITEMS);
        appendAsYamlList(sb, "notObtainableMaterials", NOT_OBTAINABLE_MATERIALS);
        appendAsYamlList(sb, "collisionBlocks", COLLISION_BLOCKS);
        appendAsYamlList(sb, "noCollisionBlocks", NO_COLLISION_BLOCKS);

        try {
            Files.writeString(file.toPath(), sb.toString());
        } catch (IOException e) {
            BulletCore.logError("Failed to write " + file.getName() + ": " + e.getMessage());
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