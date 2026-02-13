package com.mj.portfolio.dao;

import com.mj.portfolio.exception.DAOException;
import com.mj.portfolio.model.Device;
import com.mj.portfolio.model.DeviceStatus;
import com.mj.portfolio.model.DeviceType;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * PostgreSQL implementation of {@link DeviceDAO} using plain JDBC.
 *
 * <p>Key JDBC safety practices used throughout:
 * <ul>
 *   <li><b>PreparedStatement</b> – parameters are bound via {@code setXxx()},
 *       preventing SQL injection entirely.</li>
 *   <li><b>try-with-resources</b> – Connection, Statement and ResultSet are
 *       all {@link AutoCloseable}; the JVM closes them even if an exception is
 *       thrown halfway through.</li>
 *   <li><b>Connection from pool</b> – we call {@code dataSource.getConnection()}
 *       which borrows a connection from HikariCP and returns it to the pool
 *       when the try-block ends (not actually closed, just returned).</li>
 * </ul>
 * </p>
 */
public class DeviceDAOImpl implements DeviceDAO {

    private final DataSource dataSource;

    public DeviceDAOImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    // ── Queries ──────────────────────────────────────────────────────────────

    @Override
    public List<Device> findAll() {
        String sql = "SELECT * FROM devices ORDER BY name";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            return collectRows(rs);

        } catch (SQLException e) {
            throw new DAOException("Failed to fetch all devices", e);
        }
    }

    @Override
    public Optional<Device> findById(UUID id) {
        String sql = "SELECT * FROM devices WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setObject(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }

        } catch (SQLException e) {
            throw new DAOException("Failed to find device by id: " + id, e);
        }
    }

    @Override
    public List<Device> findByType(DeviceType type) {
        String sql = "SELECT * FROM devices WHERE type = ? ORDER BY name";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, type.name());
            try (ResultSet rs = ps.executeQuery()) {
                return collectRows(rs);
            }

        } catch (SQLException e) {
            throw new DAOException("Failed to filter by type: " + type, e);
        }
    }

    @Override
    public List<Device> findByStatus(DeviceStatus status) {
        String sql = "SELECT * FROM devices WHERE status = ? ORDER BY name";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status.name());
            try (ResultSet rs = ps.executeQuery()) {
                return collectRows(rs);
            }

        } catch (SQLException e) {
            throw new DAOException("Failed to filter by status: " + status, e);
        }
    }

    @Override
    public List<Device> search(String keyword) {
        String like = "%" + keyword.toLowerCase() + "%";
        String sql = """
                SELECT * FROM devices
                WHERE LOWER(name)       LIKE ?
                   OR LOWER(ip_address) LIKE ?
                   OR LOWER(location)   LIKE ?
                ORDER BY name
                """;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, like);
            ps.setString(2, like);
            ps.setString(3, like);
            try (ResultSet rs = ps.executeQuery()) {
                return collectRows(rs);
            }

        } catch (SQLException e) {
            throw new DAOException("Search failed for keyword: " + keyword, e);
        }
    }

    // ── Mutations ────────────────────────────────────────────────────────────

    @Override
    public Device save(Device device) {
        // gen_random_uuid() is PostgreSQL 13+ built-in — no extension required.
        // RETURNING lets us get DB-generated values without a second SELECT.
        String sql = """
                INSERT INTO devices (name, type, status, ip_address, location)
                VALUES (?, ?, ?, ?, ?)
                RETURNING id, created_at
                """;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, device.getName());
            ps.setString(2, device.getType().name());
            ps.setString(3, device.getStatus().name());
            ps.setString(4, device.getIpAddress());
            ps.setString(5, device.getLocation());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    device.setId((UUID) rs.getObject("id"));
                    device.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                }
            }
            return device;

        } catch (SQLException e) {
            throw new DAOException("Failed to save device: " + device.getName(), e);
        }
    }

    @Override
    public Device update(Device device) {
        String sql = """
                UPDATE devices
                SET name       = ?,
                    type       = ?,
                    status     = ?,
                    ip_address = ?,
                    location   = ?
                WHERE id = ?
                """;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, device.getName());
            ps.setString(2, device.getType().name());
            ps.setString(3, device.getStatus().name());
            ps.setString(4, device.getIpAddress());
            ps.setString(5, device.getLocation());
            ps.setObject(6, device.getId());

            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new DAOException("No device found with id: " + device.getId());
            }
            return device;

        } catch (SQLException e) {
            throw new DAOException("Failed to update device: " + device.getId(), e);
        }
    }

    @Override
    public boolean delete(UUID id) {
        String sql = "DELETE FROM devices WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setObject(1, id);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new DAOException("Failed to delete device: " + id, e);
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    /** Maps the current row of a ResultSet to a Device object. */
    private Device mapRow(ResultSet rs) throws SQLException {
        return new Device(
                (UUID) rs.getObject("id"),
                rs.getString("name"),
                DeviceType.valueOf(rs.getString("type")),
                DeviceStatus.valueOf(rs.getString("status")),
                rs.getString("ip_address"),
                rs.getString("location"),
                rs.getTimestamp("created_at").toLocalDateTime()
        );
    }

    /** Collects all rows from a ResultSet into a List. */
    private List<Device> collectRows(ResultSet rs) throws SQLException {
        List<Device> list = new ArrayList<>();
        while (rs.next()) {
            list.add(mapRow(rs));
        }
        return list;
    }
}
