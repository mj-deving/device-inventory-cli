package com.mj.portfolio.model;

/**
 * Represents the physical or functional type of a device in the inventory.
 * Stored as VARCHAR in PostgreSQL using the enum name (e.g. "LAPTOP").
 */
public enum DeviceType {

    LAPTOP("Laptop"),
    SERVER("Server"),
    PRINTER("Printer"),
    NETWORK_SWITCH("Network Switch"),
    ROUTER("Router"),
    OTHER("Other");

    private final String displayName;

    DeviceType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Case-insensitive lookup by enum name or display name.
     * E.g. "laptop", "LAPTOP", "Laptop" all resolve to LAPTOP.
     */
    public static DeviceType fromInput(String input) {
        if (input == null || input.isBlank()) {
            return OTHER;
        }
        for (DeviceType type : values()) {
            if (type.name().equalsIgnoreCase(input.trim())
                    || type.displayName.equalsIgnoreCase(input.trim())) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown device type: '" + input + "'");
    }

    @Override
    public String toString() {
        return displayName;
    }
}
