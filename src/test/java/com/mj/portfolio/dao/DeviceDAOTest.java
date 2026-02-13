package com.mj.portfolio.dao;

import com.mj.portfolio.db.DatabaseConfig;
import com.mj.portfolio.model.Device;
import com.mj.portfolio.model.DeviceStatus;
import com.mj.portfolio.model.DeviceType;
import org.junit.*;
import org.junit.runners.MethodSorters;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.Assert.*;

/**
 * Integration tests for {@link DeviceDAOImpl}.
 *
 * <p><b>Requires a running PostgreSQL database.</b>
 * Place {@code db.properties} in the project root (or on the classpath)
 * before running these tests. They will be skipped automatically if the
 * database cannot be reached.</p>
 *
 * <p>Tests are ordered alphabetically to guarantee CRUD sequence:
 * {@code t1_save} → {@code t2_findAll} → {@code t3_findById} → ... → {@code t7_delete}
 * </p>
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DeviceDAOTest {

    private static DeviceDAO dao;
    private static UUID      testId;   // shared across tests

    @BeforeClass
    public static void setUpClass() {
        try {
            dao = new DeviceDAOImpl(DatabaseConfig.getDataSource());
        } catch (Exception e) {
            // If the DB is unavailable, all tests will be skipped via Assume
            System.err.println("⚠ Database unavailable — integration tests skipped: " + e.getMessage());
        }
    }

    @AfterClass
    public static void tearDownClass() {
        DatabaseConfig.close();
    }

    @Before
    public void assumeDbAvailable() {
        Assume.assumeNotNull("DAO must be initialised (DB reachable)", dao);
    }

    // ── Test methods (alphabetical = execution order) ─────────────────────────

    @Test
    public void t1_save_shouldCreateDeviceAndPopulateIdAndCreatedAt() {
        Device device = new Device("Test Laptop", DeviceType.LAPTOP, DeviceStatus.ACTIVE,
                "192.168.99.99", "Test Room");

        Device saved = dao.save(device);

        assertNotNull("Saved device must have an ID",         saved.getId());
        assertNotNull("Saved device must have a createdAt",   saved.getCreatedAt());
        assertEquals("Name should match",   "Test Laptop",    saved.getName());
        assertEquals("Type should match",   DeviceType.LAPTOP, saved.getType());
        assertEquals("Status should match", DeviceStatus.ACTIVE, saved.getStatus());

        testId = saved.getId(); // make available to subsequent tests
    }

    @Test
    public void t2_findAll_shouldReturnNonEmptyList() {
        Assume.assumeNotNull(testId);
        List<Device> devices = dao.findAll();
        assertFalse("findAll() must return at least the test device", devices.isEmpty());
    }

    @Test
    public void t3_findById_shouldReturnCorrectDevice() {
        Assume.assumeNotNull(testId);
        Optional<Device> result = dao.findById(testId);

        assertTrue("Device should be found by id", result.isPresent());
        assertEquals("Test Laptop", result.get().getName());
    }

    @Test
    public void t4_findById_unknownId_shouldReturnEmpty() {
        Optional<Device> result = dao.findById(UUID.randomUUID());
        assertFalse("Random UUID should not match any device", result.isPresent());
    }

    @Test
    public void t5_search_shouldMatchByName() {
        Assume.assumeNotNull(testId);
        List<Device> results = dao.search("Test Lap");
        assertTrue("search('Test Lap') should find the test device",
                results.stream().anyMatch(d -> d.getId().equals(testId)));
    }

    @Test
    public void t6_update_shouldPersistChanges() {
        Assume.assumeNotNull(testId);
        Device device = dao.findById(testId).orElseThrow();
        device.setStatus(DeviceStatus.MAINTENANCE);
        device.setLocation("Maintenance Bay");

        dao.update(device);

        Device reloaded = dao.findById(testId).orElseThrow();
        assertEquals(DeviceStatus.MAINTENANCE, reloaded.getStatus());
        assertEquals("Maintenance Bay",        reloaded.getLocation());
    }

    @Test
    public void t7_delete_shouldRemoveDevice() {
        Assume.assumeNotNull(testId);
        boolean deleted = dao.delete(testId);
        assertTrue("delete() should return true for existing id", deleted);

        Optional<Device> gone = dao.findById(testId);
        assertFalse("Deleted device should not be found", gone.isPresent());
    }

    @Test
    public void t8_filterByType_shouldReturnMatchingDevices() {
        // Seeds and then cleans up a SERVER device for this test
        Device server = dao.save(new Device("Temp Server", DeviceType.SERVER,
                DeviceStatus.ACTIVE, null, null));
        try {
            List<Device> servers = dao.findByType(DeviceType.SERVER);
            assertTrue(servers.stream().anyMatch(d -> d.getId().equals(server.getId())));
        } finally {
            dao.delete(server.getId());
        }
    }

    @Test
    public void t9_filterByStatus_shouldReturnMatchingDevices() {
        Device inactive = dao.save(new Device("Temp Inactive", DeviceType.OTHER,
                DeviceStatus.INACTIVE, null, null));
        try {
            List<Device> results = dao.findByStatus(DeviceStatus.INACTIVE);
            assertTrue(results.stream().anyMatch(d -> d.getId().equals(inactive.getId())));
        } finally {
            dao.delete(inactive.getId());
        }
    }
}
