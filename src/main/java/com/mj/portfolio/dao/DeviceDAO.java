package com.mj.portfolio.dao;

import com.mj.portfolio.model.Device;
import com.mj.portfolio.model.DeviceStatus;
import com.mj.portfolio.model.DeviceType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Data Access Object interface for {@link Device} persistence.
 *
 * <p>Defining an interface here allows the service layer to depend on an
 * abstraction rather than a concrete implementation — a principle known as
 * Dependency Inversion (the D in SOLID). In tests, a mock or stub can be
 * injected without touching the database at all.</p>
 */
public interface DeviceDAO {

    /** Returns all devices ordered by name. */
    List<Device> findAll();

    /** Returns the device with the given UUID, or empty if not found. */
    Optional<Device> findById(UUID id);

    /** Returns all devices of a given type, ordered by name. */
    List<Device> findByType(DeviceType type);

    /** Returns all devices with a given status, ordered by name. */
    List<Device> findByStatus(DeviceStatus status);

    /**
     * Full-text search across name, ip_address, and location.
     * The search is case-insensitive and uses a substring match.
     */
    List<Device> search(String keyword);

    /**
     * Inserts a new device and returns it with id and createdAt populated.
     *
     * @param device a Device whose id and createdAt may be null/empty
     * @return the same Device object, now with id and createdAt set
     */
    Device save(Device device);

    /**
     * Updates all mutable fields of an existing device.
     *
     * @return the updated Device
     */
    Device update(Device device);

    /**
     * Deletes the device with the given UUID.
     *
     * @return {@code true} if a row was deleted, {@code false} if not found
     */
    boolean delete(UUID id);
}
