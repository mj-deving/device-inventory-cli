package com.mj.portfolio.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Represents a single device record in the inventory.
 *
 * <p>Field names use Java conventions (camelCase) while the database uses
 * snake_case (e.g. {@code ip_address}). The mapping happens in
 * {@code DeviceDAOImpl#mapRow()}.</p>
 */
public class Device {

    private static final DateTimeFormatter DISPLAY_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private UUID          id;
    private String        name;
    private DeviceType    type;
    private DeviceStatus  status;
    private String        ipAddress;
    private String        location;
    private LocalDateTime createdAt;

    public Device() {}

    /** Constructor for creating a new device (id and createdAt set by DB). */
    public Device(String name, DeviceType type, DeviceStatus status,
                  String ipAddress, String location) {
        this.name      = name;
        this.type      = type;
        this.status    = status;
        this.ipAddress = ipAddress;
        this.location  = location;
    }

    /** Full constructor for reconstructing from a database row. */
    public Device(UUID id, String name, DeviceType type, DeviceStatus status,
                  String ipAddress, String location, LocalDateTime createdAt) {
        this.id        = id;
        this.name      = name;
        this.type      = type;
        this.status    = status;
        this.ipAddress = ipAddress;
        this.location  = location;
        this.createdAt = createdAt;
    }

    // ── Getters ─────────────────────────────────────────────────────────────

    public UUID          getId()        { return id; }
    public String        getName()      { return name; }
    public DeviceType    getType()      { return type; }
    public DeviceStatus  getStatus()    { return status; }
    public String        getIpAddress() { return ipAddress; }
    public String        getLocation()  { return location; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    // ── Setters ─────────────────────────────────────────────────────────────

    public void setId(UUID id)                { this.id = id; }
    public void setName(String name)          { this.name = name; }
    public void setType(DeviceType type)      { this.type = type; }
    public void setStatus(DeviceStatus status){ this.status = status; }
    public void setIpAddress(String ipAddress){ this.ipAddress = ipAddress; }
    public void setLocation(String location)  { this.location = location; }
    public void setCreatedAt(LocalDateTime t) { this.createdAt = t; }

    // ── Utility ─────────────────────────────────────────────────────────────

    /** Formatted creation timestamp for CLI display. */
    public String getFormattedCreatedAt() {
        return createdAt != null ? createdAt.format(DISPLAY_FORMAT) : "—";
    }

    @Override
    public String toString() {
        return String.format(
                "Device{id=%s, name='%s', type=%s, status=%s, ip='%s', location='%s', created=%s}",
                id, name, type, status, ipAddress, location, getFormattedCreatedAt()
        );
    }
}
