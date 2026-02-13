package com.mj.portfolio.service;

import com.mj.portfolio.dao.DeviceDAO;
import com.mj.portfolio.model.Device;
import com.mj.portfolio.model.DeviceStatus;
import com.mj.portfolio.model.DeviceType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Business logic layer for device management.
 *
 * <p>The service layer sits between the CLI and the DAO. Its responsibilities:
 * <ul>
 *   <li>Input validation (not SQL-injection prevention — that's the DAO's job)</li>
 *   <li>Business rules (e.g. required fields, string trimming)</li>
 *   <li>Delegation to the DAO for actual persistence</li>
 * </ul>
 * The CLI should never call the DAO directly.</p>
 */
public class DeviceService {

    private final DeviceDAO deviceDAO;

    public DeviceService(DeviceDAO deviceDAO) {
        this.deviceDAO = deviceDAO;
    }

    // ── Read operations ──────────────────────────────────────────────────────

    public List<Device> getAllDevices() {
        return deviceDAO.findAll();
    }

    public Optional<Device> findById(String rawId) {
        UUID id = parseUUID(rawId);
        return deviceDAO.findById(id);
    }

    public List<Device> filterByType(DeviceType type) {
        return deviceDAO.findByType(type);
    }

    public List<Device> filterByStatus(DeviceStatus status) {
        return deviceDAO.findByStatus(status);
    }

    public List<Device> search(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            throw new IllegalArgumentException("Search keyword must not be empty.");
        }
        return deviceDAO.search(keyword.trim());
    }

    // ── Write operations ─────────────────────────────────────────────────────

    /**
     * Validates input and persists a new device.
     *
     * @param name      required, 1-100 characters
     * @param type      required
     * @param status    required
     * @param ipAddress optional (null or empty means "none recorded")
     * @param location  optional
     * @return the saved device with id and createdAt populated
     */
    public Device addDevice(String name, DeviceType type, DeviceStatus status,
                            String ipAddress, String location) {
        validateName(name);

        Device device = new Device(
                name.trim(),
                type,
                status,
                nullIfBlank(ipAddress),
                nullIfBlank(location)
        );
        return deviceDAO.save(device);
    }

    /**
     * Updates an existing device. Pass {@code null} for any field to leave it unchanged.
     *
     * @param rawId UUID of the device to update
     */
    public Device updateDevice(String rawId, String name, DeviceType type,
                               DeviceStatus status, String ipAddress, String location) {
        UUID id = parseUUID(rawId);
        Device existing = deviceDAO.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No device found with ID: " + rawId));

        if (name     != null && !name.isBlank())     existing.setName(name.trim());
        if (type     != null)                         existing.setType(type);
        if (status   != null)                         existing.setStatus(status);
        if (ipAddress != null)                        existing.setIpAddress(nullIfBlank(ipAddress));
        if (location != null)                         existing.setLocation(nullIfBlank(location));

        return deviceDAO.update(existing);
    }

    /**
     * Deletes a device by UUID string.
     *
     * @return {@code true} if deleted, {@code false} if not found
     */
    public boolean removeDevice(String rawId) {
        UUID id = parseUUID(rawId);
        return deviceDAO.delete(id);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Device name is required.");
        }
        if (name.trim().length() > 100) {
            throw new IllegalArgumentException("Device name must be 100 characters or fewer.");
        }
    }

    private UUID parseUUID(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("ID must not be empty.");
        }
        try {
            return UUID.fromString(raw.trim());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("'" + raw + "' is not a valid UUID.");
        }
    }

    private String nullIfBlank(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }
}
