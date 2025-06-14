package org.dredd.bulletCore.models;

/**
 * Represents different types of custom items.<br>
 * Used to categorize and manage custom items.
 *
 * @author dredd
 * @since 1.0.0
 */
public enum CustomItemType {
    AMMO("ammo", "Ammo"),
    ARMOR("armor", "Armor"),
    GRENADE("grenades", "Grenade"),
    WEAPON("weapons", "Weapon");

    /**
     * Base directory path where all custom item type folders are located.
     */
    private static final String BASE_FOLDER = "custom-items/";

    /**
     * The subfolder name where items of this type are stored.
     */
    private final String subfolder;

    /**
     * The label for this item type shown in logs during the loading process.
     */
    private final String label;

    CustomItemType(String subfolder, String label) {
        this.subfolder = subfolder;
        this.label = label;
    }

    /**
     * Gets the complete folder path for this item type inside the plugin's data folder.
     *
     * @return The full path combining the base folder and type-specific subfolder
     */
    public String getFolderPath() {
        return BASE_FOLDER + subfolder;
    }

    /**
     * Gets the display label for this item type.
     *
     * @return The human-readable label for the item type
     */
    public String getLabel() {
        return label;
    }
}