package com.mj.portfolio.model;

/**
 * Operational status of a device.
 * Stored as VARCHAR in PostgreSQL using the enum name (e.g. "ACTIVE").
 */
public enum DeviceStatus {

    ACTIVE("Active"),
    INACTIVE("Inactive"),
    MAINTENANCE("Maintenance");

    private final String displayName;

    DeviceStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Case-insensitive lookup by enum name or display name.
     */
    public static DeviceStatus fromInput(String input) {
        if (input == null || input.isBlank()) {
            return ACTIVE;
        }
        for (DeviceStatus status : values()) {
            if (status.name().equalsIgnoreCase(input.trim())
                    || status.displayName.equalsIgnoreCase(input.trim())) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown device status: '" + input + "'");
    }

    @Override
    public String toString() {
        return displayName;
    }
}
