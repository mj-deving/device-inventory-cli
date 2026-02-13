package com.mj.portfolio.cli;

import com.mj.portfolio.dao.DeviceDAOImpl;
import com.mj.portfolio.db.DatabaseConfig;
import com.mj.portfolio.exception.DAOException;
import com.mj.portfolio.model.Device;
import com.mj.portfolio.model.DeviceStatus;
import com.mj.portfolio.model.DeviceType;
import com.mj.portfolio.service.DeviceService;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.stream.Collectors;

/**
 * Entry point for the Device Inventory CLI application.
 *
 * <p>Architecture overview:
 * <pre>
 *   DeviceInventoryCLI  (I/O, user interaction)
 *         │ calls
 *   DeviceService       (validation, business rules)
 *         │ calls
 *   DeviceDAOImpl       (SQL via JDBC + HikariCP)
 *         │ connects
 *   PostgreSQL (devicedb)
 * </pre>
 * </p>
 */
public class DeviceInventoryCLI {

    // ── Table column widths ──────────────────────────────────────────────────
    private static final String ROW_FMT =
            "  %-36s  %-22s  %-14s  %-13s  %-17s  %-20s  %s%n";
    private static final String DIVIDER = "─".repeat(140);

    private final DeviceService service;
    private final Scanner       scanner;

    public DeviceInventoryCLI(DeviceService service) {
        this.service = service;
        this.scanner = new Scanner(System.in);
    }

    // ── Main ─────────────────────────────────────────────────────────────────

    public static void main(String[] args) {
        // Register shutdown hook so the connection pool closes cleanly on Ctrl+C
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            DatabaseConfig.close();
            System.out.println("\nConnection pool closed. Goodbye!");
        }));

        DeviceService service = new DeviceService(
                new DeviceDAOImpl(DatabaseConfig.getDataSource()));

        new DeviceInventoryCLI(service).run();
    }

    // ── Main loop ────────────────────────────────────────────────────────────

    public void run() {
        printBanner();

        boolean running = true;
        while (running) {
            printMenu();
            String choice = prompt("> ").trim();
            System.out.println();

            try {
                running = handleChoice(choice);
            } catch (IllegalArgumentException e) {
                printError(e.getMessage());
            } catch (DAOException e) {
                printError("Database error: " + e.getMessage());
            }
        }
    }

    // ── Menu routing ─────────────────────────────────────────────────────────

    private boolean handleChoice(String choice) {
        switch (choice) {
            case "1" -> listAll();
            case "2" -> addDevice();
            case "3" -> findById();
            case "4" -> updateDevice();
            case "5" -> deleteDevice();
            case "6" -> searchDevices();
            case "7" -> filterByType();
            case "8" -> filterByStatus();
            case "0" -> { return false; }
            default  -> printError("Unknown option '" + choice + "'. Enter 0–8.");
        }
        return true;
    }

    // ── Menu actions ─────────────────────────────────────────────────────────

    private void listAll() {
        List<Device> devices = service.getAllDevices();
        if (devices.isEmpty()) {
            System.out.println("  No devices found.\n");
            return;
        }
        printTable(devices);
    }

    private void addDevice() {
        System.out.println("  ── Add New Device ──────────────────────────────");

        String name     = promptRequired("  Name      : ");
        DeviceType type = promptType();
        DeviceStatus st = promptStatus();
        String ip       = prompt("  IP Address: (optional, press Enter to skip) ");
        String location = prompt("  Location  : (optional, press Enter to skip) ");

        Device saved = service.addDevice(name, type, st, ip, location);
        System.out.printf("%n  ✔ Device created with ID: %s%n%n", saved.getId());
    }

    private void findById() {
        String id = promptRequired("  Enter device UUID: ");
        Optional<Device> result = service.findById(id);

        if (result.isEmpty()) {
            System.out.println("  No device found with that ID.\n");
        } else {
            printTable(List.of(result.get()));
        }
    }

    private void updateDevice() {
        String id = promptRequired("  Enter UUID of device to update: ");
        Optional<Device> found = service.findById(id);

        if (found.isEmpty()) {
            System.out.println("  No device found with that ID.\n");
            return;
        }

        Device current = found.get();
        System.out.println("\n  Current values (press Enter to keep each value):");
        System.out.printf("  Name      [%s]: ", current.getName());
        String name = scanner.nextLine().trim();

        System.out.printf("  Type      [%s]: ", current.getType().getDisplayName());
        String typeRaw = scanner.nextLine().trim();

        System.out.printf("  Status    [%s]: ", current.getStatus().getDisplayName());
        String statusRaw = scanner.nextLine().trim();

        System.out.printf("  IP Address[%s]: ", orDash(current.getIpAddress()));
        String ip = scanner.nextLine().trim();

        System.out.printf("  Location  [%s]: ", orDash(current.getLocation()));
        String location = scanner.nextLine().trim();

        // Resolve new enum values (null means "keep existing")
        DeviceType   newType   = typeRaw.isEmpty()   ? null : DeviceType.fromInput(typeRaw);
        DeviceStatus newStatus = statusRaw.isEmpty() ? null : DeviceStatus.fromInput(statusRaw);
        String       newName   = name.isEmpty()     ? null : name;
        String       newIp     = ip.isEmpty()       ? null : ip;
        String       newLoc    = location.isEmpty() ? null : location;

        service.updateDevice(id, newName, newType, newStatus, newIp, newLoc);
        System.out.println("\n  ✔ Device updated.\n");
    }

    private void deleteDevice() {
        String id = promptRequired("  Enter UUID of device to delete: ");

        // Confirm before deleting
        Optional<Device> found = service.findById(id);
        if (found.isEmpty()) {
            System.out.println("  No device found with that ID.\n");
            return;
        }
        System.out.printf("  Delete '%s'? (yes/no): ", found.get().getName());
        String confirm = scanner.nextLine().trim();

        if ("yes".equalsIgnoreCase(confirm)) {
            boolean deleted = service.removeDevice(id);
            System.out.println(deleted
                    ? "  ✔ Device deleted.\n"
                    : "  Device not found (may have been already deleted).\n");
        } else {
            System.out.println("  Cancelled.\n");
        }
    }

    private void searchDevices() {
        String keyword = promptRequired("  Search keyword: ");
        List<Device> results = service.search(keyword);

        if (results.isEmpty()) {
            System.out.printf("  No devices match '%s'.%n%n", keyword);
        } else {
            System.out.printf("  %d result(s) for '%s':%n", results.size(), keyword);
            printTable(results);
        }
    }

    private void filterByType() {
        System.out.println("  Available types: " + typeOptions());
        String input = promptRequired("  Type: ");
        DeviceType type = DeviceType.fromInput(input);
        List<Device> results = service.filterByType(type);

        if (results.isEmpty()) {
            System.out.printf("  No devices of type '%s' found.%n%n", type.getDisplayName());
        } else {
            printTable(results);
        }
    }

    private void filterByStatus() {
        System.out.println("  Available statuses: " + statusOptions());
        String input = promptRequired("  Status: ");
        DeviceStatus status = DeviceStatus.fromInput(input);
        List<Device> results = service.filterByStatus(status);

        if (results.isEmpty()) {
            System.out.printf("  No devices with status '%s' found.%n%n", status.getDisplayName());
        } else {
            printTable(results);
        }
    }

    // ── Display helpers ───────────────────────────────────────────────────────

    private void printBanner() {
        System.out.println("""

          ╔══════════════════════════════════════════╗
          ║       DEVICE INVENTORY SYSTEM  v1.0      ║
          ╚══════════════════════════════════════════╝
        """);
    }

    private void printMenu() {
        System.out.println("""
          ┌─────────────────────────────────┐
          │  [1] List all devices           │
          │  [2] Add device                 │
          │  [3] Find device by ID          │
          │  [4] Update device              │
          │  [5] Delete device              │
          │  [6] Search devices             │
          │  [7] Filter by type             │
          │  [8] Filter by status           │
          │  [0] Exit                       │
          └─────────────────────────────────┘""");
    }

    private void printTable(List<Device> devices) {
        System.out.println();
        System.out.printf(ROW_FMT,
                "ID", "Name", "Type", "Status", "IP Address", "Location", "Created");
        System.out.println("  " + DIVIDER);

        for (Device d : devices) {
            System.out.printf(ROW_FMT,
                    d.getId(),
                    truncate(d.getName(), 22),
                    d.getType().getDisplayName(),
                    d.getStatus().getDisplayName(),
                    orDash(d.getIpAddress()),
                    orDash(d.getLocation()),
                    d.getFormattedCreatedAt());
        }
        System.out.println();
    }

    private void printError(String message) {
        System.out.printf("%n  ✗ %s%n%n", message);
    }

    // ── Input helpers ─────────────────────────────────────────────────────────

    private String prompt(String label) {
        System.out.print(label);
        return scanner.nextLine();
    }

    private String promptRequired(String label) {
        String value;
        do {
            value = prompt(label).trim();
            if (value.isEmpty()) {
                System.out.println("  This field is required. Please enter a value.");
            }
        } while (value.isEmpty());
        return value;
    }

    private DeviceType promptType() {
        while (true) {
            System.out.println("  Types: " + typeOptions());
            String input = prompt("  Type      : ").trim();
            try {
                return DeviceType.fromInput(input);
            } catch (IllegalArgumentException e) {
                System.out.println("  Invalid type. Try again.");
            }
        }
    }

    private DeviceStatus promptStatus() {
        while (true) {
            System.out.println("  Statuses: " + statusOptions());
            String input = prompt("  Status    : ").trim();
            try {
                return DeviceStatus.fromInput(input);
            } catch (IllegalArgumentException e) {
                System.out.println("  Invalid status. Try again.");
            }
        }
    }

    private String typeOptions() {
        return Arrays.stream(DeviceType.values())
                .map(t -> t.name() + " (" + t.getDisplayName() + ")")
                .collect(Collectors.joining(", "));
    }

    private String statusOptions() {
        return Arrays.stream(DeviceStatus.values())
                .map(s -> s.name() + " (" + s.getDisplayName() + ")")
                .collect(Collectors.joining(", "));
    }

    private String truncate(String s, int max) {
        if (s == null) return "—";
        return s.length() <= max ? s : s.substring(0, max - 1) + "…";
    }

    private String orDash(String s) {
        return (s == null || s.isBlank()) ? "—" : s;
    }
}
