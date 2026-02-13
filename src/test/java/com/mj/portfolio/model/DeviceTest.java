package com.mj.portfolio.model;

import org.junit.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.Assert.*;

/**
 * Unit tests for the Device model and its associated enums.
 * No database required — pure logic tests.
 */
public class DeviceTest {

    // ── Device POJO ──────────────────────────────────────────────────────────

    @Test
    public void testDeviceGettersAndSetters() {
        Device device = new Device();
        UUID id = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        device.setId(id);
        device.setName("Test Server");
        device.setType(DeviceType.SERVER);
        device.setStatus(DeviceStatus.ACTIVE);
        device.setIpAddress("10.0.0.1");
        device.setLocation("Server Room A");
        device.setCreatedAt(now);

        assertEquals(id, device.getId());
        assertEquals("Test Server", device.getName());
        assertEquals(DeviceType.SERVER, device.getType());
        assertEquals(DeviceStatus.ACTIVE, device.getStatus());
        assertEquals("10.0.0.1", device.getIpAddress());
        assertEquals("Server Room A", device.getLocation());
        assertEquals(now, device.getCreatedAt());
    }

    @Test
    public void testConvenienceConstructor() {
        Device device = new Device("Laptop A", DeviceType.LAPTOP, DeviceStatus.ACTIVE,
                "192.168.1.1", "Office 1");

        assertNull("id should be null before DB insert", device.getId());
        assertEquals("Laptop A", device.getName());
        assertEquals(DeviceType.LAPTOP, device.getType());
    }

    @Test
    public void testFullConstructor() {
        UUID id = UUID.randomUUID();
        LocalDateTime ts = LocalDateTime.of(2026, 2, 13, 10, 0);

        Device device = new Device(id, "Router", DeviceType.ROUTER, DeviceStatus.ACTIVE,
                "10.0.0.1", "DC", ts);

        assertEquals(id, device.getId());
        assertEquals("2026-02-13 10:00", device.getFormattedCreatedAt());
    }

    @Test
    public void testFormattedCreatedAtWhenNull() {
        Device device = new Device();
        assertEquals("—", device.getFormattedCreatedAt());
    }

    // ── DeviceType enum ──────────────────────────────────────────────────────

    @Test
    public void testDeviceTypeFromInputCaseInsensitive() {
        assertEquals(DeviceType.LAPTOP,         DeviceType.fromInput("laptop"));
        assertEquals(DeviceType.LAPTOP,         DeviceType.fromInput("LAPTOP"));
        assertEquals(DeviceType.LAPTOP,         DeviceType.fromInput("Laptop"));
        assertEquals(DeviceType.NETWORK_SWITCH, DeviceType.fromInput("NETWORK_SWITCH"));
        assertEquals(DeviceType.SERVER,         DeviceType.fromInput("Server"));
    }

    @Test
    public void testDeviceTypeFromInputByDisplayName() {
        assertEquals(DeviceType.NETWORK_SWITCH, DeviceType.fromInput("Network Switch"));
        assertEquals(DeviceType.PRINTER,        DeviceType.fromInput("Printer"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDeviceTypeFromInputUnknown() {
        DeviceType.fromInput("SUPERMACHINE");
    }

    @Test
    public void testDeviceTypeFromInputBlankReturnsOther() {
        assertEquals(DeviceType.OTHER, DeviceType.fromInput(""));
        assertEquals(DeviceType.OTHER, DeviceType.fromInput(null));
    }

    @Test
    public void testDeviceTypeDisplayNames() {
        assertEquals("Laptop",         DeviceType.LAPTOP.getDisplayName());
        assertEquals("Network Switch", DeviceType.NETWORK_SWITCH.getDisplayName());
        assertEquals("Other",          DeviceType.OTHER.getDisplayName());
    }

    // ── DeviceStatus enum ────────────────────────────────────────────────────

    @Test
    public void testDeviceStatusFromInputCaseInsensitive() {
        assertEquals(DeviceStatus.ACTIVE,      DeviceStatus.fromInput("active"));
        assertEquals(DeviceStatus.INACTIVE,    DeviceStatus.fromInput("INACTIVE"));
        assertEquals(DeviceStatus.MAINTENANCE, DeviceStatus.fromInput("Maintenance"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDeviceStatusFromInputUnknown() {
        DeviceStatus.fromInput("BROKEN");
    }

    @Test
    public void testDeviceStatusFromInputBlankReturnsActive() {
        assertEquals(DeviceStatus.ACTIVE, DeviceStatus.fromInput(""));
        assertEquals(DeviceStatus.ACTIVE, DeviceStatus.fromInput(null));
    }
}
