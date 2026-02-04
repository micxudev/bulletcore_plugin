package org.dredd.bulletcore.config.materials;

import java.io.File;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.dredd.bulletcore.BulletCore;
import org.jetbrains.annotations.NotNull;

/**
 * Manages and loads materials.
 *
 * @author dredd
 * @since 1.0.0
 */
public final class MaterialsManager {

    // ----------< Static >----------

    private static final String ALL_MATERIALS_FILE_NAME = "all-materials.yml";

    private static final boolean GENERATE_ALL_MATERIALS_FILE = true;

    private static final String IGNORED_MATERIALS_FILE_NAME = "ignored-materials.yml";

    private static final List<String> IGNORED_MATERIALS_HEADER = List.of("Wiki: <link>");

    private static MaterialsManager instance;

    public static MaterialsManager instance() {
        return instance;
    }

    public static void load(@NotNull BulletCore plugin) {
        instance = new MaterialsManager(plugin);
    }

    // ----------< Instance >----------

    // -----< Attributes >-----

    private final BulletCore plugin;

    private final Set<Material> ignoredMaterials;

    // -----< Construction >-----

    private MaterialsManager(@NotNull BulletCore plugin) {
        this.plugin = plugin;

        final File ignoredMaterialsFile = new File(plugin.getDataFolder(), IGNORED_MATERIALS_FILE_NAME);
        final boolean isFirstLoading = !ignoredMaterialsFile.exists();

        this.ignoredMaterials = isFirstLoading
            ? initializeDefaults(ignoredMaterialsFile)
            : loadIgnoredMaterialsFromFile(ignoredMaterialsFile);

        plugin.logInfo("-Loaded " + ignoredMaterials.size() + " ignored materials");
    }

    // -----< Public API >-----

    /**
     * Determines whether bullets should pass through the given material.
     * <p>
     * Ignored materials do not stop bullets during ray tracing.
     *
     * @param material the material to test
     * @return {@code true} if bullets should pass through this material,
     * {@code false} if the material may stop the bullet
     */
    public boolean isIgnored(@NotNull Material material) {
        return ignoredMaterials.contains(material);
    }

    // -----< Utilities >-----

    private boolean matchesAny(@NotNull String name,
                               @NotNull Pattern[] patterns) {
        for (final Pattern pattern : patterns)
            if (pattern.matcher(name).matches())
                return true;
        return false;
    }

    private @NotNull Pattern[] compilePatterns(@NotNull List<String> patterns) {
        return patterns.stream()
            .map(pattern -> {
                try {
                    return Pattern.compile(pattern);
                } catch (PatternSyntaxException e) {
                    plugin.logError("Invalid regex pattern \"" + pattern + "\": " + e.getMessage());
                    return null;
                }
            })
            .filter(Objects::nonNull)
            .toArray(Pattern[]::new);
    }


    private @NotNull Set<Material> matchPatterns(@NotNull Set<Material> allMaterials,
                                                 boolean addAll,
                                                 @NotNull List<String> include,
                                                 @NotNull List<String> exclude) {
        final Set<Material> result = EnumSet.noneOf(Material.class);
        final Pattern[] includePatterns = compilePatterns(include);
        final Pattern[] excludePatterns = compilePatterns(exclude);

        for (final var material : allMaterials) {
            final String name = material.name();

            final boolean shouldInclude = addAll || matchesAny(name, includePatterns);
            final boolean isAllowed = !matchesAny(name, excludePatterns);

            if (shouldInclude && isAllowed)
                result.add(material);
        }

        return result;
    }


    // -----< First Loading >-----

    /**
     * Creates the default ignored materials file on the first startup and returns the default ignored materials.
     */
    private @NotNull Set<Material> initializeDefaults(@NotNull File file) {
        try {
            writeDefaultIgnoredMaterials(file);
            plugin.logInfo("Created default ignored materials file \"" + file + "\"");
        } catch (Exception e) {
            plugin.logError("Failed to create default ignored materials file \"" + file + "\": " + e.getMessage());
        }
        return loadDefaults();
    }

    /**
     * Returns default ignored materials for all ignored material groups.
     */
    private @NotNull Set<Material> loadDefaults() {
        if (GENERATE_ALL_MATERIALS_FILE)
            MaterialCategory.writeAllMaterials(new File(plugin.getDataFolder(), ALL_MATERIALS_FILE_NAME));

        final Set<Material> result = EnumSet.noneOf(Material.class);

        for (final var category : MaterialCategory.values()) {
            if (category.skipInIgnored) continue;

            result.addAll(
                matchPatterns(
                    category.allMaterials,
                    category.addAll,
                    category.includePatterns,
                    category.excludePatterns
                )
            );
        }
        return Collections.unmodifiableSet(result);
    }

    /**
     * Writes default ignored materials to the given file.
     */
    private void writeDefaultIgnoredMaterials(@NotNull File file) throws Exception {
        final var config = new YamlConfiguration();
        config.options().setHeader(IGNORED_MATERIALS_HEADER);

        config.set("generate-all-materials-file", GENERATE_ALL_MATERIALS_FILE);

        for (final var category : MaterialCategory.values()) {
            if (category.skipInIgnored) continue;

            final var section = config.createSection(category.label);
            section.set("addAll", category.addAll);
            section.set("includePatterns", category.includePatterns);
            section.set("excludePatterns", category.excludePatterns);
        }

        config.save(file);
    }

    // -----< Regular Loading >-----

    /**
     * Loads ignored materials from the given file or falls back to default on failure.
     */
    private @NotNull Set<Material> loadIgnoredMaterialsFromFile(@NotNull File file) {
        try {
            final var config = new YamlConfiguration();
            config.load(file);

            if (config.getBoolean("generate-all-materials-file", GENERATE_ALL_MATERIALS_FILE))
                MaterialCategory.writeAllMaterials(new File(plugin.getDataFolder(), ALL_MATERIALS_FILE_NAME));

            return parseIgnoredMaterials(config);
        } catch (Exception e) {
            plugin.logError("Failed to load ignored materials file \"" + file + "\":\n" + e.getMessage() + "\nusing default ignored materials.");
            return loadDefaults();
        }
    }

    /**
     * Parses ignored materials from the given configuration.
     */
    private @NotNull Set<Material> parseIgnoredMaterials(@NotNull YamlConfiguration config) {
        final Set<Material> result = EnumSet.noneOf(Material.class);

        for (final var category : MaterialCategory.values()) {
            if (category.skipInIgnored) continue;

            final var section = config.getConfigurationSection(category.label);
            if (section == null) {
                plugin.logError("Missing materials category \"" + category.label + "\"; skipping.");
                continue;
            }

            result.addAll(
                matchPatterns(
                    category.allMaterials,
                    section.getBoolean("addAll", false),
                    section.getStringList("includePatterns"),
                    section.getStringList("excludePatterns")
                )
            );
        }
        return Collections.unmodifiableSet(result);
    }
}