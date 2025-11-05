package org.dredd.bulletcore.config.materials;

import org.bukkit.Material;
import org.dredd.bulletcore.BulletCore;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.nio.file.Files;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static org.dredd.bulletcore.utils.ServerUtils.EMPTY_LIST;

/**
 * Represents categories of {@link Material}.
 *
 * @author dredd
 * @since 1.0.0
 */
public enum MaterialCategory {

    // ----------< Enum Fields >----------

    ITEMS_ONLY(AllMaterials.ITEMS_ONLY, true, false, EMPTY_LIST, EMPTY_LIST),
    BLOCKS_ONLY(AllMaterials.BLOCKS_ONLY, false, true, EMPTY_LIST, ExcludePatterns.BLOCKS_ONLY),
    BLOCKS_COLLIDABLE(AllMaterials.BLOCKS_COLLIDABLE, false, false, IncludePatterns.BLOCKS_COLLIDABLE, ExcludePatterns.BLOCKS_COLLIDABLE),
    BLOCKS_NON_COLLIDABLE(AllMaterials.BLOCKS_NON_COLLIDABLE, false, true, EMPTY_LIST, EMPTY_LIST);


    // ----------< Static >----------

    private static final class AllMaterials {

        private static final int TOTAL_MATERIALS;
        private static final Set<Material> ITEMS_ONLY;
        private static final Set<Material> BLOCKS_ONLY;
        private static final Set<Material> BLOCKS_COLLIDABLE;
        private static final Set<Material> BLOCKS_NON_COLLIDABLE;

        static {
            final Material[] allMaterials = Material.values();
            TOTAL_MATERIALS = allMaterials.length;

            ITEMS_ONLY = new LinkedHashSet<>();
            BLOCKS_ONLY = new LinkedHashSet<>();
            BLOCKS_COLLIDABLE = new LinkedHashSet<>();
            BLOCKS_NON_COLLIDABLE = new LinkedHashSet<>();

            for (final Material material : allMaterials) {
                final boolean isItem = material.isItem();
                final boolean isBlock = material.isBlock();

                if (isItem && !isBlock) {
                    ITEMS_ONLY.add(material);
                    continue;
                }

                if (!isItem && isBlock) {
                    BLOCKS_ONLY.add(material);
                    continue;
                }

                // Both Item and Block
                if (material.isCollidable())
                    BLOCKS_COLLIDABLE.add(material);
                else
                    BLOCKS_NON_COLLIDABLE.add(material);
            }
        }
    }

    private static final class IncludePatterns {

        private static final List<String> BLOCKS_COLLIDABLE;

        static {
            BLOCKS_COLLIDABLE = List.of(
                "^.*FENCE(?:_GATE)?$",
                "^.*(?:TRAP)?DOOR$",
                "^.*GLASS(?:_PANE)?$",
                "^.*CARPET$",
                "^.*CANDLE$",
                "^.*BED$",
                "^.*CORAL_BLOCK$",
                "^.*LEAVES$",
                "^.*EGG$",
                "^.*LANTERN$",
                "^.*SKULL$",
                "^.*HEAD$",
                "^.*AMETHYST_BUD$",
                "^.*ICE$",
                "^IRON_BARS$",
                "^MANGROVE_ROOTS$",
                "^MUSHROOM_STEM$",
                "^MOSS_BLOCK$",
                "^CACTUS$",
                "^BIG_DRIPLEAF$",
                "^BAMBOO$",
                "^LILY_PAD$",
                "^SEA_PICKLE$",
                "^DRIED_KELP_BLOCK$",
                "^END_ROD$",
                "^REDSTONE_LAMP$",
                "^GLOWSTONE$",
                "^SHROOMLIGHT$",
                "^REPEATER$",
                "^COMPARATOR$",
                "^DAYLIGHT_DETECTOR$",
                "^CAKE$",
                "^MELON$",
                "^HAY_BLOCK$",
                "^SLIME_BLOCK$",
                "^LADDER$",
                "^BREWING_STAND$",
                "^CONDUIT$",
                "^COMPOSTER$",
                "^NOTE_BLOCK$",
                "^JUKEBOX$",
                "^AMETHYST_CLUSTER$",
                "^POINTED_DRIPSTONE$",
                "^LIGHT$",
                "^BARRIER$",
                "^VAULT$",
                "^(?:FLOWERING_)?AZALEA$",
                "^CHORUS_(?:PLANT|FLOWER)$",
                "^(?:NETHER|WARPED)_WART_BLOCK$",
                "^(?:BROWN|RED)_MUSHROOM_BLOCK$",
                "^(?:FLOWER|DECORATED)_POT$",
                "^(?:WET_)?SPONGE$",
                "^(CARVED_)?PUMPKIN$",
                "^HONEY(?:COMB)?_BLOCK$",
                "^SNOW(?:_BLOCK)?$",
                "^(?:TRIAL_)?SPAWNER$"
            );
        }
    }

    private static final class ExcludePatterns {

        private static final List<String> BLOCKS_ONLY;
        private static final List<String> BLOCKS_COLLIDABLE;

        static {
            BLOCKS_ONLY = List.of(
                "^PISTON_HEAD$",
                "^.*_CAULDRON$"
            );

            BLOCKS_COLLIDABLE = List.of(
                "^.*(?:IRON|COPPER)_(?:TRAP)?DOOR$"
            );
        }
    }


    // ----------< Instance >----------

    final String label;

    final Set<Material> allMaterials;

    final boolean skipInIgnored;

    final boolean addAll;

    final List<String> includePatterns;

    final List<String> excludePatterns;

    MaterialCategory(@NotNull Set<Material> allMaterials,
                     boolean skipInIgnored,
                     boolean addAll,
                     @NotNull List<String> includePatterns,
                     @NotNull List<String> excludePatterns) {
        this.label = name().toLowerCase(Locale.ROOT);
        this.allMaterials = Collections.unmodifiableSet(allMaterials);
        this.skipInIgnored = skipInIgnored;

        this.addAll = addAll;
        this.includePatterns = includePatterns;
        this.excludePatterns = excludePatterns;
    }

    // -----< Saving Utilities >-----

    static void writeAllMaterials(@NotNull File file) {
        final StringBuilder sb = new StringBuilder();

        sb.append("total_materials: ").append(AllMaterials.TOTAL_MATERIALS).append('\n');

        for (final var category : values())
            appendAsYamlList(sb, category.label, category.allMaterials);

        try {
            Files.writeString(file.toPath(), sb.toString());
        } catch (Exception e) {
            BulletCore.logError("Failed to save all materials file \"" + file + "\": " + e.getMessage());
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