# device-inventory-cli — Vollständige Dokumentation

> **Für wen ist dieses Dokument?**
> Für Entwickler, die Java kennen, aber noch keine Erfahrung mit JDBC, PostgreSQL,
> Maven, Connection Pooling oder Schichtenarchitektur haben. Jedes Konzept wird
> von Grund auf erklärt — mit Analogien, Diagrammen und Codebeispielen.

---

## Inhaltsverzeichnis

1. [Was macht dieses Projekt?](#1-was-macht-dieses-projekt)
2. [Tech Stack im Überblick](#2-tech-stack-im-überblick)
3. [Projektstruktur verstehen](#3-projektstruktur-verstehen)
4. [Maven — Das Build-System](#4-maven--das-build-system)
5. [PostgreSQL — Die Datenbank](#5-postgresql--die-datenbank)
6. [JDBC — Java spricht mit der Datenbank](#6-jdbc--java-spricht-mit-der-datenbank)
7. [HikariCP — Connection Pooling](#7-hikaricp--connection-pooling)
8. [Schichtenarchitektur (Layered Architecture)](#8-schichtenarchitektur-layered-architecture)
9. [Das DAO-Pattern](#9-das-dao-pattern)
10. [Jede Klasse im Detail](#10-jede-klasse-im-detail)
11. [Wichtige Java-Konzepte im Code](#11-wichtige-java-konzepte-im-code)
12. [SQL-Injection: Was ist das und wie schützt man sich?](#12-sql-injection-was-ist-das-und-wie-schützt-man-sich)
13. [UUID — Warum nicht einfach 1, 2, 3?](#13-uuid--warum-nicht-einfach-1-2-3)
14. [Das Projekt lokal aufsetzen](#14-das-projekt-lokal-aufsetzen)
15. [Tests verstehen](#15-tests-verstehen)
16. [GitHub Actions CI/CD](#16-github-actions-cicd)
17. [Häufige Fehler und Lösungen](#17-häufige-fehler-und-lösungen)
18. [Zusammenfassung: Was du gelernt hast](#18-zusammenfassung-was-du-gelernt-hast)

---

## 1. Was macht dieses Projekt?

**device-inventory-cli** ist ein Kommandozeilen-Programm (CLI = Command Line Interface)
zur Verwaltung von Geräten in einem Unternehmensnetzwerk.

```
┌─────────────────────────────────────────────────────┐
│              DEVICE INVENTORY SYSTEM                 │
├─────────────────────────────────────────────────────┤
│  Du kannst damit...                                  │
│                                                      │
│  • Geräte anlegen   (Laptop, Server, Drucker, ...)   │
│  • Geräte auflisten (mit Status, IP, Standort)       │
│  • Geräte suchen    (nach Name, IP, Standort)        │
│  • Geräte filtern   (nach Typ oder Status)           │
│  • Geräte updaten   (IP ändern, Status ändern, ...)  │
│  • Geräte löschen   (mit Bestätigungsdialog)         │
└─────────────────────────────────────────────────────┘
```

**Technische Kernfunktion:** Alle Daten werden dauerhaft in einer
PostgreSQL-Datenbank gespeichert — auch nach Programmneustart sind sie noch da.

---

## 2. Tech Stack im Überblick

```
┌─────────────────────────────────────────────────────────────────┐
│                        TECH STACK                               │
├───────────────┬─────────────────────────────────────────────────┤
│ Java 17       │ Programmiersprache. Objektorientiert, typsicher, │
│               │ läuft auf der JVM (plattformunabhängig).         │
├───────────────┼─────────────────────────────────────────────────┤
│ Maven 3.8     │ Build-Tool. Verwaltet Dependencies, kompiliert   │
│               │ den Code, führt Tests aus, baut die JAR-Datei.   │
├───────────────┼─────────────────────────────────────────────────┤
│ PostgreSQL 16 │ Relationale Datenbank. Speichert Daten dauerhaft │
│               │ auf der Festplatte. Queries in SQL.              │
├───────────────┼─────────────────────────────────────────────────┤
│ JDBC          │ Java-Standard-API für Datenbankzugriff.          │
│               │ (Java Database Connectivity)                      │
├───────────────┼─────────────────────────────────────────────────┤
│ HikariCP 5.1  │ Connection-Pool-Bibliothek. Verwaltet geöffnete  │
│               │ DB-Verbindungen effizient.                        │
├───────────────┼─────────────────────────────────────────────────┤
│ JUnit 4       │ Test-Framework. Automatisierte Tests in Java.    │
├───────────────┼─────────────────────────────────────────────────┤
│ GitHub Actions│ CI/CD-Pipeline. Baut und testet automatisch      │
│               │ bei jedem Git-Push.                              │
└───────────────┴─────────────────────────────────────────────────┘
```

---

## 3. Projektstruktur verstehen

```
device-inventory-cli/
│
├── pom.xml                          ← Maven Konfiguration (Dependencies, Build)
├── db.properties.example            ← Vorlage für Datenbank-Credentials
│
├── db/
│   └── schema.sql                   ← SQL: Erstellt die Datenbanktabelle
│
├── .github/
│   └── workflows/
│       └── ci.yml                   ← GitHub Actions Pipeline
│
└── src/
    ├── main/
    │   ├── java/com/mj/portfolio/
    │   │   ├── cli/
    │   │   │   └── DeviceInventoryCLI.java     ← Hauptprogramm (main-Methode)
    │   │   │
    │   │   ├── service/
    │   │   │   └── DeviceService.java          ← Business-Logik
    │   │   │
    │   │   ├── dao/
    │   │   │   ├── DeviceDAO.java              ← Interface (Vertrag)
    │   │   │   └── DeviceDAOImpl.java          ← SQL-Implementierung
    │   │   │
    │   │   ├── model/
    │   │   │   ├── Device.java                 ← Datenmodell (ein Gerät)
    │   │   │   ├── DeviceType.java             ← Enum: LAPTOP, SERVER, ...
    │   │   │   └── DeviceStatus.java           ← Enum: ACTIVE, INACTIVE, ...
    │   │   │
    │   │   ├── db/
    │   │   │   └── DatabaseConfig.java         ← HikariCP Connection Pool
    │   │   │
    │   │   └── exception/
    │   │       └── DAOException.java           ← Eigene Exception-Klasse
    │   │
    │   └── resources/
    │       ├── db.properties                   ← Deine DB-Zugangsdaten (gitignored!)
    │       └── db.properties.example           ← Vorlage (wird committed)
    │
    └── test/
        └── java/com/mj/portfolio/
            ├── model/
            │   └── DeviceTest.java             ← Tests ohne Datenbank
            └── dao/
                └── DeviceDAOTest.java          ← Tests mit Datenbank
```

---

## 4. Maven — Das Build-System

### Was ist Maven?

**Analogie:** Stell dir vor, du baust ein Haus. Maven ist wie ein Baumeister, der:
- weiß, welche Materialien (Dependencies) du brauchst und sie automatisch besorgt
- in der richtigen Reihenfolge baut (erst Fundament, dann Wände, dann Dach)
- Tests durchführt bevor er das Haus übergibt
- alles in ein transportierbares Paket verpackt (JAR-Datei)

### Die pom.xml — Das Herzstück

Die `pom.xml` (Project Object Model) ist die Konfigurationsdatei von Maven.

```xml
<!-- Wer bin ich? -->
<groupId>com.mj.portfolio</groupId>        <!-- Firmenname/Namespace -->
<artifactId>device-inventory-cli</artifactId>  <!-- Projektname -->
<version>1.0.0</version>                   <!-- Version -->

<!-- Was brauche ich? (Dependencies) -->
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <version>42.7.1</version>
    <!-- Maven lädt diese JAR automatisch von Maven Central herunter -->
</dependency>
```

### Wichtige Maven-Befehle

```bash
mvn clean compile        # Kompiliert den Quellcode
mvn test                 # Führt Tests aus
mvn clean package        # Kompiliert + Tests + JAR bauen
mvn clean package -DskipTests  # JAR bauen ohne Tests

# Das Resultat: target/device-inventory-cli-1.0.0-jar-with-dependencies.jar
# Das ist eine "Fat JAR" — alle Dependencies sind darin enthalten
```

### Was ist eine Fat JAR?

Eine normale JAR enthält nur deinen Code. Eine **Fat JAR** (auch "Uber JAR") enthält:
- Deinen Code
- ALLE Dependencies (HikariCP, PostgreSQL-Treiber, etc.)

→ Du kannst sie auf jedem Server mit `java -jar app.jar` starten, ohne etwas zu installieren.

---

## 5. PostgreSQL — Die Datenbank

### Was ist eine relationale Datenbank?

**Analogie:** Eine Datenbank ist wie ein Excel-Dokument, das auf einem Server läuft:
- **Tabelle** = Excel-Tabellenblatt
- **Spalte** = Excel-Spalte (mit festem Datentyp!)
- **Zeile** = ein Datensatz (ein Gerät)
- **SQL** = die Sprache um Daten abzufragen und zu verändern

### Die Geräte-Tabelle (`devices`)

```sql
CREATE TABLE IF NOT EXISTS devices (
    id          UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(100) NOT NULL,
    type        VARCHAR(50)  NOT NULL,
    status      VARCHAR(50)  NOT NULL DEFAULT 'ACTIVE',
    ip_address  VARCHAR(45),          -- Optional (kann NULL sein)
    location    VARCHAR(100),         -- Optional
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);
```

**Erklärung der Spalten:**

| Spalte | Typ | Bedeutung |
|--------|-----|-----------|
| `id` | UUID | Eindeutiger Bezeichner (automatisch generiert) |
| `name` | VARCHAR(100) | Gerätename, max. 100 Zeichen, Pflichtfeld |
| `type` | VARCHAR(50) | Gerätetyp (LAPTOP, SERVER, etc.) |
| `status` | VARCHAR(50) | Betriebsstatus, Default: 'ACTIVE' |
| `ip_address` | VARCHAR(45) | IPv4 oder IPv6, optional |
| `location` | VARCHAR(100) | Standort, optional |
| `created_at` | TIMESTAMP | Erstellungszeitpunkt, automatisch gesetzt |

### Grundlegende SQL-Befehle

```sql
-- Alle Geräte abrufen
SELECT * FROM devices ORDER BY name;

-- Ein Gerät nach ID suchen
SELECT * FROM devices WHERE id = '550e8400-e29b-41d4-a716-446655440000';

-- Neues Gerät einfügen
INSERT INTO devices (name, type, status, ip_address, location)
VALUES ('Office Laptop', 'LAPTOP', 'ACTIVE', '192.168.1.101', 'Büro 2.OG')
RETURNING id, created_at;  -- PostgreSQL gibt uns die generierten Werte zurück

-- Gerät aktualisieren
UPDATE devices
SET status = 'MAINTENANCE', location = 'Reparaturwerkstatt'
WHERE id = '550e8400-e29b-41d4-a716-446655440000';

-- Gerät löschen
DELETE FROM devices WHERE id = '550e8400-e29b-41d4-a716-446655440000';

-- Suche (case-insensitiv, Teilstring)
SELECT * FROM devices
WHERE LOWER(name) LIKE '%laptop%'
   OR LOWER(ip_address) LIKE '%192.168%';
```

### Indexes — Warum sind sie wichtig?

```sql
CREATE INDEX idx_devices_type   ON devices(type);
CREATE INDEX idx_devices_status ON devices(status);
```

**Analogie:** Ein Datenbankindex ist wie das Register am Ende eines Buchs.
Ohne Register: Du musst das ganze Buch durchlesen um "Laptop" zu finden.
Mit Register: Du schaust auf Seite 347 und findest es sofort.

Bei 10 Geräten merkst du keinen Unterschied. Bei 10 Millionen Geräten ist
der Unterschied zwischen 0,001 Sekunden und 30 Sekunden Abfragezeit.

---

## 6. JDBC — Java spricht mit der Datenbank

### Was ist JDBC?

JDBC (Java Database Connectivity) ist das Standard-API in Java für
Datenbankzugriff. Es ist wie eine universelle Fernbedienung:

```
Java-Code                  JDBC API               Datenbank
    │                          │                      │
    │  "Gib mir alle Geräte"   │                      │
    │ ──────────────────────→  │                      │
    │                          │  SELECT * FROM ...   │
    │                          │ ───────────────────→ │
    │                          │                      │
    │                          │   [Ergebnisse]       │
    │                          │ ←─────────────────── │
    │   Liste von Geräten      │                      │
    │ ←──────────────────────  │                      │
```

Du schreibst immer gegen die JDBC-API — egal ob PostgreSQL, MySQL oder SQLite.
Nur der **Treiber** (eine JAR-Datei) ist datenbankspezifisch.

### Die 3 wichtigsten JDBC-Klassen

#### 1. `Connection` — Die Telefonleitung zur Datenbank

```java
// Öffne eine Verbindung zur Datenbank
Connection conn = dataSource.getConnection();

// Führe eine SQL-Abfrage durch
// ...

// Schließe die Verbindung wieder
conn.close();
```

#### 2. `PreparedStatement` — Vorgefertigte SQL-Anfragen

```java
// Das "?" ist ein Platzhalter — wird später sicher befüllt
String sql = "SELECT * FROM devices WHERE type = ?";
PreparedStatement ps = conn.prepareStatement(sql);

// Parameter setzen (Index beginnt bei 1, nicht 0!)
ps.setString(1, "LAPTOP");

// Ausführen
ResultSet rs = ps.executeQuery();
```

#### 3. `ResultSet` — Die Ergebnistabelle

```java
// ResultSet ist wie ein Cursor der durch die Ergebniszeilen wandert
while (rs.next()) {  // next() bewegt den Cursor zur nächsten Zeile
    String name = rs.getString("name");
    String type = rs.getString("type");
    UUID id = (UUID) rs.getObject("id");
    // ...
}
```

### try-with-resources — Ressourcen sicher schließen

**Das Problem:** Was passiert, wenn eine Exception geworfen wird?

```java
// FALSCH: conn bleibt offen wenn eine Exception geworfen wird!
Connection conn = dataSource.getConnection();
PreparedStatement ps = conn.prepareStatement(sql);
ResultSet rs = ps.executeQuery();
// → Exception hier → conn, ps, rs werden NIE geschlossen → Memory Leak!
conn.close();
```

```java
// RICHTIG: try-with-resources schließt IMMER, auch bei Exception
try (Connection conn = dataSource.getConnection();
     PreparedStatement ps = conn.prepareStatement(sql);
     ResultSet rs = ps.executeQuery()) {

    // Verarbeitung...

} // conn, ps, rs werden hier AUTOMATISCH geschlossen — garantiert!
```

`Connection`, `PreparedStatement` und `ResultSet` implementieren alle das
`AutoCloseable` Interface. `try-with-resources` ruft `close()` am Ende
automatisch auf — wie ein `finally`-Block, aber lesbarer.

---

## 7. HikariCP — Connection Pooling

### Das Problem ohne Connection Pool

Stell dir vor, jedes Mal wenn ein Benutzer etwas im System macht:

```
Benutzer-Aktion
    ↓
1. TCP-Verbindung zum DB-Server aufbauen     (~5ms)
2. TLS-Handshake (Verschlüsselung)           (~10ms)
3. Authentifizierung (Benutzername/Passwort) (~3ms)
4. SQL-Query ausführen                       (~1ms)
5. Verbindung schließen                      (~2ms)
                                          ────────
Gesamt für eine Query:                     ~21ms
Davon ist der eigentliche Query:            ~1ms
```

95% der Zeit wird für Verbindungsaufbau/-abbau verschwendet!

### Die Lösung: Connection Pool

**Analogie:** Ein Connection Pool ist wie ein Fuhrpark.
- Ohne Pool: Jedes Mal ein neues Auto kaufen, fahren, verkaufen.
- Mit Pool: 10 Autos stehen bereit. Du leihst eines, fährst, gibst es zurück.

```
Anwendung                HikariCP Pool                PostgreSQL
    │                         │                            │
    │  getConnection()        │                            │
    │ ──────────────────────→ │                            │
    │                         │  (gibt conn1 aus dem Pool) │
    │ ←────────────────────── │                            │
    │                         │                            │
    │  [SQL ausführen]        │                            │
    │ ──────────────────────────────────────────────────→  │
    │ ←────────────────────────────────────────────────── │
    │                         │                            │
    │  conn.close()           │                            │
    │ ──────────────────────→ │                            │
    │                    conn1 zurück in Pool              │
    │                    (NICHT wirklich geschlossen!)     │
```

`conn.close()` schließt die Verbindung NICHT wirklich — sie wird an den Pool
zurückgegeben und wartet auf den nächsten Aufruf.

### HikariCP Konfiguration im Projekt

```java
HikariConfig cfg = new HikariConfig();
cfg.setJdbcUrl("jdbc:postgresql://localhost:5432/devicedb");
cfg.setUsername("portfolio");
cfg.setPassword("portfolio_dev_password");
cfg.setMaximumPoolSize(10);        // Maximal 10 gleichzeitige Verbindungen
cfg.setConnectionTimeout(30_000);  // 30 Sek warten auf freie Verbindung
cfg.setIdleTimeout(600_000);       // Idle-Verbindung nach 10 Min schließen
cfg.setMaxLifetime(1_800_000);     // Verbindung max. 30 Min am Leben lassen
```

### Warum HikariCP?

HikariCP ist der **schnellste** Connection Pool für Java. Er ist:
- ~10x schneller als ältere Alternativen (C3P0, DBCP)
- Standard in Spring Boot (wird automatisch verwendet)
- Minimal: nur eine einzige JAR-Datei

---

## 8. Schichtenarchitektur (Layered Architecture)

### Warum Schichten?

**Ohne Schichten** — alles in einer Klasse:
```java
// BAD: CLI + Business-Logik + SQL alles zusammen
public class Main {
    public static void main(String[] args) {
        // Scanner für Benutzereingabe
        // SQL-Query direkt hier
        // Validierung hier
        // Ausgabe hier
        // → Unlesbar, untestbar, unwartbar
    }
}
```

**Mit Schichten** — jede Klasse hat eine einzige Verantwortung:

```
┌─────────────────────────────────────────────────────┐
│              CLI (DeviceInventoryCLI)                │
│  Verantwortung: Benutzereingabe lesen, Ausgabe       │
│  Kennt: DeviceService                                │
│  Kennt NICHT: SQL, Datenbank                         │
├─────────────────────────────────────────────────────┤
│              Service (DeviceService)                 │
│  Verantwortung: Validierung, Business-Regeln         │
│  Kennt: DeviceDAO Interface                          │
│  Kennt NICHT: Scanner, SQL, HikariCP                 │
├─────────────────────────────────────────────────────┤
│              DAO (DeviceDAOImpl)                     │
│  Verantwortung: SQL-Queries, Datenbankzugriff        │
│  Kennt: JDBC, HikariCP, SQL                          │
│  Kennt NICHT: Scanner, Business-Logik                │
├─────────────────────────────────────────────────────┤
│              Datenbank (PostgreSQL)                  │
│  Verantwortung: Daten speichern und abrufen          │
└─────────────────────────────────────────────────────┘
```

**Vorteile:**
- **Testbarkeit:** DAO kann ohne CLI getestet werden
- **Austauschbarkeit:** PostgreSQL kann durch H2 (In-Memory-DB) ersetzt werden
- **Lesbarkeit:** Du weißt sofort wo du suchen musst
- **Single Responsibility Principle:** Jede Klasse hat einen Grund zur Änderung

### Datenfluss — Ein Beispiel

**Szenario:** Benutzer gibt "1" ein (Liste alle Geräte)

```
Benutzer tippt "1"
        │
        ▼
DeviceInventoryCLI.handleChoice("1")
  → ruft listAll() auf
        │
        ▼
DeviceInventoryCLI.listAll()
  → ruft service.getAllDevices() auf
        │
        ▼
DeviceService.getAllDevices()
  → ruft deviceDAO.findAll() auf
        │
        ▼
DeviceDAOImpl.findAll()
  → öffnet Connection aus HikariCP Pool
  → führt aus: SELECT * FROM devices ORDER BY name
  → mappt jede Zeile zu einem Device-Objekt
  → gibt List<Device> zurück
        │
        ▼
DeviceService
  → gibt List<Device> unverändert zurück
        │
        ▼
DeviceInventoryCLI.listAll()
  → ruft printTable(devices) auf
        │
        ▼
Ausgabe auf dem Terminal:
  ID          | Name              | Type   | Status | ...
  ────────────┼───────────────────┼────────┼────────┼────
  550e8400... | Office Laptop 01  | Laptop | Active | ...
```

---

## 9. Das DAO-Pattern

### Was bedeutet DAO?

**DAO = Data Access Object** — ein Designmuster (Pattern) das den
Datenbankzugriff hinter einem Interface versteckt.

### Das Interface — Der Vertrag

```java
public interface DeviceDAO {
    List<Device> findAll();
    Optional<Device> findById(UUID id);
    List<Device> findByType(DeviceType type);
    List<Device> findByStatus(DeviceStatus status);
    List<Device> search(String keyword);
    Device save(Device device);
    Device update(Device device);
    boolean delete(UUID id);
}
```

Das Interface definiert **was** getan wird — nicht **wie**.

### Die Implementierung — Das Wie

```java
public class DeviceDAOImpl implements DeviceDAO {
    // Hier steht das konkrete SQL für PostgreSQL
    @Override
    public List<Device> findAll() {
        String sql = "SELECT * FROM devices ORDER BY name";
        // JDBC Code...
    }
}
```

### Warum Interface + Implementierung trennen?

```
DeviceService kennt nur:        DeviceDAO (Interface)
                                      ↑
                            implementiert von:
                                      │
                          ┌───────────┴───────────┐
                          │                       │
                   DeviceDAOImpl           MockDeviceDAO
                  (echte Datenbank)        (für Tests)
```

Im Produktivbetrieb:
```java
DeviceDAO dao = new DeviceDAOImpl(dataSource);  // echte DB
DeviceService service = new DeviceService(dao);
```

In Tests:
```java
DeviceDAO dao = new MockDeviceDAO();  // keine DB nötig!
DeviceService service = new DeviceService(dao);
// → Tests laufen ohne Datenbankverbindung
```

Das nennt sich **Dependency Injection** und ist das "D" in SOLID.

---

## 10. Jede Klasse im Detail

### Device.java — Das Datenmodell

```java
public class Device {
    private UUID          id;        // Eindeutige ID (wie Personalausweisnummer)
    private String        name;      // "Office Laptop 01"
    private DeviceType    type;      // LAPTOP, SERVER, PRINTER, ...
    private DeviceStatus  status;    // ACTIVE, INACTIVE, MAINTENANCE
    private String        ipAddress; // "192.168.1.101" (optional)
    private String        location;  // "Büro 2. OG" (optional)
    private LocalDateTime createdAt; // Wann wurde das Gerät angelegt?
}
```

**Warum `LocalDateTime` und nicht `String`?**
`LocalDateTime` ist ein Java-Typ für Datum+Uhrzeit. Er verhindert Fehler
wie `"2026-13-45"` (ungültiges Datum als String). PostgreSQL's `TIMESTAMP`
wird automatisch zu `LocalDateTime` konvertiert.

### DeviceType.java — Der Enum

```java
public enum DeviceType {
    LAPTOP("Laptop"),
    SERVER("Server"),
    PRINTER("Printer"),
    NETWORK_SWITCH("Network Switch"),
    ROUTER("Router"),
    OTHER("Other");

    private final String displayName;  // Anzeigename für das UI
}
```

**Was ist ein Enum?**
Ein Enum (Enumeration) ist eine Klasse mit einer festen Anzahl von Werten.
`DeviceType.LAPTOP` ist typsicher — du kannst nicht versehentlich
`"Laptopp"` (mit Tippfehler) übergeben.

In der Datenbank wird der Enum-Name gespeichert: `"LAPTOP"` (nicht `"Laptop"`).
Die `displayName` ist nur für die CLI-Anzeige.

**`fromInput()` — Robuste Eingabeverarbeitung:**
```java
DeviceType.fromInput("laptop")  // → DeviceType.LAPTOP
DeviceType.fromInput("LAPTOP")  // → DeviceType.LAPTOP
DeviceType.fromInput("Laptop")  // → DeviceType.LAPTOP
// Alle drei funktionieren! Case-insensitiv.
```

### DatabaseConfig.java — Der Connection Pool Manager

```java
public class DatabaseConfig {
    private static HikariDataSource dataSource;  // static = nur eine Instanz

    public static synchronized DataSource getDataSource() {
        if (dataSource == null || dataSource.isClosed()) {
            dataSource = createPool();  // Nur beim ersten Aufruf erstellen
        }
        return dataSource;
    }
}
```

**Singleton-Pattern:** `static` + `synchronized` stellt sicher, dass
der Pool nur EINMAL erstellt wird — egal wie viele Threads gleichzeitig
`getDataSource()` aufrufen.

**Konfiguration laden:**
```java
private static Properties loadProperties() {
    // 1. Versuche: db.properties neben der JAR-Datei
    File external = new File("db.properties");
    if (external.exists()) { /* lade sie */ }

    // 2. Fallback: db.properties im Classpath (src/main/resources/)
    InputStream in = DatabaseConfig.class.getResourceAsStream("/db.properties");
}
```

Diese Reihenfolge ist sinnvoll: Auf dem Server kannst du einfach eine
`db.properties` neben die JAR legen ohne sie neu zu bauen.

### DeviceDAOImpl.java — Der SQL-Spezialist

**Das RETURNING Keyword (PostgreSQL-spezifisch):**

```sql
INSERT INTO devices (name, type, status, ip_address, location)
VALUES (?, ?, ?, ?, ?)
RETURNING id, created_at
```

Ohne `RETURNING` müsste man zwei Queries machen:
1. INSERT
2. SELECT ... WHERE name = ? (gefährlich bei gleichem Namen!)

Mit `RETURNING` bekommt man die DB-generierten Werte in einem Schritt zurück.

**`mapRow()` — ResultSet zu Java-Objekt:**
```java
private Device mapRow(ResultSet rs) throws SQLException {
    return new Device(
        (UUID) rs.getObject("id"),           // UUID-Spalte
        rs.getString("name"),                 // String-Spalte
        DeviceType.valueOf(rs.getString("type")),    // String → Enum
        DeviceStatus.valueOf(rs.getString("status")), // String → Enum
        rs.getString("ip_address"),           // kann NULL sein
        rs.getString("location"),             // kann NULL sein
        rs.getTimestamp("created_at").toLocalDateTime() // Timestamp → LocalDateTime
    );
}
```

`DeviceType.valueOf("LAPTOP")` konvertiert den String `"LAPTOP"` aus der
Datenbank zurück zum Java-Enum `DeviceType.LAPTOP`.

### DeviceService.java — Die Business-Regeln

```java
public Device addDevice(String name, DeviceType type, DeviceStatus status,
                        String ipAddress, String location) {
    validateName(name);    // Ist der Name leer? Zu lang?

    Device device = new Device(
        name.trim(),           // Whitespace entfernen
        type,
        status,
        nullIfBlank(ipAddress), // "" → null (DB-Konvention)
        nullIfBlank(location)
    );
    return deviceDAO.save(device);
}
```

**Warum `nullIfBlank`?**
In der Datenbank bedeutet `NULL` "kein Wert". Ein leerer String `""` ist
technisch ein Wert. Wir konvertieren leere Strings zu `NULL` damit
Datenbank-Abfragen wie `WHERE ip_address IS NULL` korrekt funktionieren.

### DeviceInventoryCLI.java — Das User Interface

```java
public static void main(String[] args) {
    // Shutdown Hook: wird beim Programmende aufgerufen
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
        DatabaseConfig.close();  // Connection Pool sauber schließen
        System.out.println("Connection pool closed. Goodbye!");
    }));

    // Dependency-Kette aufbauen (von innen nach außen):
    DeviceService service = new DeviceService(
            new DeviceDAOImpl(DatabaseConfig.getDataSource()));

    new DeviceInventoryCLI(service).run();
}
```

**Shutdown Hook:** Dieser Code läuft auch wenn der Benutzer `Ctrl+C` drückt.
Er stellt sicher, dass alle DB-Verbindungen ordnungsgemäß geschlossen werden.

---

## 11. Wichtige Java-Konzepte im Code

### Optional\<T\> — Kein NullPointerException mehr

```java
// Ohne Optional:
Device device = dao.findById(id);  // Kann null zurückgeben!
System.out.println(device.getName());  // NullPointerException wenn null!

// Mit Optional:
Optional<Device> result = dao.findById(id);
if (result.isEmpty()) {
    System.out.println("Gerät nicht gefunden.");
} else {
    System.out.println(result.get().getName());
}
```

`Optional` ist ein Container der entweder einen Wert enthält oder leer ist.
Er **zwingt** den Aufrufer, den "nicht gefunden"-Fall zu behandeln.

### Java Text Blocks (Java 15+)

```java
// Alt (unleserlich):
String sql = "SELECT * FROM devices\n" +
             "WHERE LOWER(name) LIKE ?\n" +
             "   OR LOWER(ip_address) LIKE ?\n" +
             "ORDER BY name";

// Neu mit Text Block (viel lesbarer):
String sql = """
        SELECT * FROM devices
        WHERE LOWER(name) LIKE ?
           OR LOWER(ip_address) LIKE ?
        ORDER BY name
        """;
```

Text Blocks (mit `"""`) sind ein Feature seit Java 15. Sie ermöglichen
mehrzeilige Strings ohne `\n` und String-Konkatenation.

### Switch Expression (Java 14+)

```java
// Alt:
switch (choice) {
    case "1": listAll(); break;
    case "2": addDevice(); break;
    default: printError("Unbekannte Option");
}

// Neu (kompakter, kein break nötig):
switch (choice) {
    case "1" -> listAll();
    case "2" -> addDevice();
    default  -> printError("Unbekannte Option");
}
```

---

## 12. SQL-Injection: Was ist das und wie schützt man sich?

### Was ist SQL-Injection?

**SQL-Injection** ist einer der gefährlichsten Angriffe auf Datenbanken.

Stell dir vor, du suchst nach Geräten nach Name:

```java
// GEFÄHRLICHER Code (niemals so schreiben!):
String userInput = scanner.nextLine();
String sql = "SELECT * FROM devices WHERE name = '" + userInput + "'";
```

Was passiert wenn der Benutzer das eingibt?
```
' OR '1'='1
```

Der resultierende SQL-String:
```sql
SELECT * FROM devices WHERE name = '' OR '1'='1'
```

`'1'='1'` ist immer wahr → alle Geräte werden zurückgegeben!

Noch schlimmer:
```
'; DROP TABLE devices; --
```

→ Löscht die gesamte `devices`-Tabelle!

### Die Lösung: PreparedStatement

```java
// SICHER:
String sql = "SELECT * FROM devices WHERE name = ?";
PreparedStatement ps = conn.prepareStatement(sql);
ps.setString(1, userInput);  // userInput wird IMMER als Daten behandelt, nie als SQL
```

Der JDBC-Treiber sorgt dafür, dass `userInput` escaped wird:
- `'` wird zu `''` (doppeltes Hochkomma, kein SQL-Zeichen)
- Sonderzeichen werden harmlos gemacht

**Merksatz:** Niemals String-Konkatenation für SQL. Immer `PreparedStatement`.

---

## 13. UUID — Warum nicht einfach 1, 2, 3?

### Das Problem mit Auto-Increment IDs

```
Datenbank auf Server A:    Datenbank auf Server B:
  id=1: Laptop 01            id=1: Server 01
  id=2: Server 01            id=2: Laptop 01
```

Wenn beide Datenbanken zusammengeführt werden: **ID-Konflikt!**

Außerdem:
- `GET /devices/5` → Angreifer kann alle IDs von 1 bis n durchprobieren
- ID 6 existiert nicht → System gibt Hinweis auf Datenbankstruktur preis

### UUID — Universal Unique Identifier

```
550e8400-e29b-41d4-a716-446655440000
```

- 128-Bit Zufallszahl
- 340.282.366.920.938.463.463.374.607.431.768.211.456 mögliche Werte
- Wahrscheinlichkeit einer Kollision: praktisch null

**In PostgreSQL** (v13+):
```sql
id UUID PRIMARY KEY DEFAULT gen_random_uuid()
```

**In Java:**
```java
UUID.randomUUID()  // Generiert eine zufällige UUID
```

**Nachteil:** UUIDs sind länger (36 Zeichen vs. ein int). Für kleine Projekte
ist das irrelevant. Bei sehr großen Tabellen gibt es optimierte Varianten (UUIDv7).

---

## 14. Das Projekt lokal aufsetzen

### Voraussetzungen

```bash
java -version   # Java 17 oder höher
mvn -version    # Maven 3.8 oder höher
psql --version  # PostgreSQL Client
```

### Schritt 1: Repository klonen

```bash
git clone https://github.com/mj-deving/device-inventory-cli.git
cd device-inventory-cli
```

### Schritt 2: Datenbank einrichten

```bash
# PostgreSQL muss laufen. Dann:
psql -h localhost -U portfolio -d devicedb < db/schema.sql

# Schema prüfen:
psql -h localhost -U portfolio -d devicedb
\d devices       # Zeigt Tabellstruktur
SELECT * FROM devices;  # Zeigt Beispieldaten
\q               # Beenden
```

### Schritt 3: db.properties erstellen

```bash
cp src/main/resources/db.properties.example src/main/resources/db.properties
# Öffne db.properties und trage deine Zugangsdaten ein
```

```properties
db.url=jdbc:postgresql://localhost:5432/devicedb
db.username=portfolio
db.password=DEIN_PASSWORT
db.pool.size=10
```

### Schritt 4: Bauen und starten

```bash
# JAR bauen
mvn clean package -DskipTests

# Starten
java -jar target/device-inventory-cli-1.0.0-jar-with-dependencies.jar
```

---

## 15. Tests verstehen

### Zwei Arten von Tests

```
┌──────────────────────────────────────────────────────┐
│  DeviceTest.java — Unit Tests                         │
│                                                      │
│  • Testet: Model, Enums, Validierungslogik           │
│  • Braucht: Keine Datenbank, keine externe Abhängigk.│
│  • Läuft: Überall, immer (auch ohne DB)              │
│  • Geschwindigkeit: Sehr schnell (~0.1 Sekunden)     │
├──────────────────────────────────────────────────────┤
│  DeviceDAOTest.java — Integrationstests              │
│                                                      │
│  • Testet: Echte SQL-Queries gegen echte Datenbank   │
│  • Braucht: Laufende PostgreSQL-Datenbank            │
│  • Läuft: Nur mit DB (wird automatisch übersprungen) │
│  • Geschwindigkeit: Langsamer (~0.6 Sekunden)        │
└──────────────────────────────────────────────────────┘
```

### Wie werden Tests übersprungen?

```java
@BeforeClass
public static void setUpClass() {
    try {
        dao = new DeviceDAOImpl(DatabaseConfig.getDataSource());
    } catch (Exception e) {
        // DB nicht erreichbar → dao bleibt null
        System.err.println("DB unavailable — tests will be skipped");
    }
}

@Before
public void assumeDbAvailable() {
    // Wenn dao null ist → Test wird als "Skipped" markiert (kein Fehler!)
    Assume.assumeNotNull("DAO must be initialised", dao);
}
```

`Assume.assumeNotNull` ist JUnit's Mechanismus um Tests zu überspringen.
Wenn die Bedingung nicht erfüllt ist, wird der Test als **Skipped** (nicht Fehler)
markiert. Die Pipeline bleibt grün.

### Testreihenfolge mit @FixMethodOrder

```java
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DeviceDAOTest {
    // Alphabetische Reihenfolge:
    // t1_save → t2_findAll → t3_findById → ... → t9_filterByStatus
```

Warum ist Reihenfolge wichtig? `t7_delete` löscht das Gerät das `t1_save`
erstellt hat. Wenn delete vor save läuft → Fehler!

JUnit 4 garantiert keine Reihenfolge ohne `@FixMethodOrder`. Mit dem Prefix
`t1_`, `t2_`, ... und alphabetischer Sortierung erzwingen wir die richtige
Reihenfolge.

### DAOException — Warum eine eigene Exception?

```java
// Option A: SQLException direkt werfen (checked exception)
public List<Device> findAll() throws SQLException { ... }

// Nachteil: Service muss SQLException fangen, obwohl es kein SQL kennen soll
public List<Device> getAllDevices() throws SQLException {  // ← SQL-Abhängigkeit!
    return deviceDAO.findAll();
}
```

```java
// Option B: DAOException (unchecked/RuntimeException)
public List<Device> findAll() {
    try { ... }
    catch (SQLException e) {
        throw new DAOException("Failed to fetch devices", e);
    }
}

// Service muss nichts fangen:
public List<Device> getAllDevices() {  // ← kein SQL-Bezug
    return deviceDAO.findAll();
}
```

`DAOException extends RuntimeException` → kein `throws` im Interface nötig.
Die SQLException ist als `cause` gesetzt → Stack Trace geht nicht verloren.

---

## 16. GitHub Actions CI/CD

### Was ist CI/CD?

- **CI** (Continuous Integration): Automatisch bauen und testen bei jedem Commit
- **CD** (Continuous Delivery): Automatisch deployen nach erfolgreichem CI

**Ziel:** Fehler werden sofort entdeckt, nicht erst Wochen später.

### Der Workflow

```yaml
on:
  push:
    branches: [ master ]   # Trigger: bei jedem Push auf master
```

**Service Container:**
```yaml
services:
  postgres:
    image: postgres:16     # Docker Image (PostgreSQL 16)
    env:
      POSTGRES_USER: portfolio
      POSTGRES_PASSWORD: portfolio_dev_password
      POSTGRES_DB: devicedb
    options: >-
      --health-cmd pg_isready    # Warte bis DB bereit ist
      --health-interval 10s
```

**Ablauf bei jedem Push:**
```
1. Code auschecken          (git checkout)
2. Java 17 installieren     (+ Maven Cache laden)
3. db.properties erstellen  (für den Service Container)
4. DB-Schema ausführen      (schema.sql)
5. Kompilieren              (mvn clean compile)
6. Tests ausführen          (mvn test) — alle 21!
7. JAR bauen                (mvn clean package)
8. JAR hochladen            (GitHub Artifact, 30 Tage)
```

### Maven Dependency Caching

```yaml
- uses: actions/setup-java@v4
  with:
    cache: maven  # ← Dieser Parameter aktiviert das Caching
```

Beim ersten Lauf werden alle Dependencies von Maven Central heruntergeladen
(~50 MB). Bei allen folgenden Läufen werden sie aus dem Cache geladen
(~0.5 Sekunden statt ~30 Sekunden).

---

## 17. Häufige Fehler und Lösungen

### Fehler: "db.properties not found"

```
RuntimeException: db.properties not found!
Copy src/main/resources/db.properties.example → db.properties
```

**Lösung:**
```bash
cp src/main/resources/db.properties.example src/main/resources/db.properties
# Dann Passwort eintragen
```

### Fehler: "Connection refused" / "FATAL: authentication failed"

```
DAOException: Failed to fetch all devices
Caused by: java.sql.SQLException: Connection to localhost:5432 refused.
```

**Mögliche Ursachen:**
1. PostgreSQL läuft nicht → `sudo systemctl start postgresql`
2. Falsches Passwort in db.properties
3. Falscher Port (Standard: 5432)
4. Peer-Authentication-Problem → `psql -h localhost` statt `psql`

### Fehler: "Peer authentication failed"

```
psql: error: connection failed: FATAL: Peer authentication failed for user "portfolio"
```

**Lösung:** `-h localhost` hinzufügen:
```bash
# Falsch:
psql -U portfolio -d devicedb

# Richtig:
psql -h localhost -U portfolio -d devicedb
```

**Erklärung:** Ohne `-h localhost` versucht `psql` eine Unix-Socket-Verbindung
("Peer Authentication" = Linux-Username muss PostgreSQL-Username entsprechen).
Mit `-h localhost` wird TCP verwendet (Passwort-Authentifizierung).

### Fehler: "UnicodeDecodeError" in Tests (Windows)

```
UnicodeEncodeError: 'charmap' codec can't encode character
```

**Lösung:** System-Property setzen:
```bash
java -Dfile.encoding=UTF-8 -jar target/device-inventory-cli-*.jar
```

Oder in der JVM-Konfiguration:
```bash
export JAVA_TOOL_OPTIONS="-Dfile.encoding=UTF-8"
```

---

## 18. Zusammenfassung: Was du gelernt hast

Nach diesem Projekt beherrschst du folgende Konzepte:

### Datenbank-Grundlagen
- ✅ Relationale Tabellen mit Constraints (NOT NULL, PRIMARY KEY)
- ✅ CRUD-Operationen in SQL (SELECT, INSERT, UPDATE, DELETE)
- ✅ Indexes für Performance-Optimierung
- ✅ PostgreSQL-spezifische Features (gen_random_uuid(), RETURNING)
- ✅ UUID vs. Auto-Increment als Primary Keys

### JDBC & Datenbankzugriff
- ✅ JDBC als universelle DB-Abstraktionsschicht
- ✅ PreparedStatement für SQL-Injection-Prävention
- ✅ ResultSet auslesen und zu Java-Objekten mappen
- ✅ try-with-resources für sicheres Ressourcenmanagement
- ✅ Connection Pooling mit HikariCP

### Java-Architektur
- ✅ Schichtenarchitektur (CLI → Service → DAO → DB)
- ✅ DAO-Pattern mit Interface + Implementierung
- ✅ Dependency Injection (kein `new` im Service)
- ✅ Singleton-Pattern für den Connection Pool
- ✅ Eigene Exception-Hierarchie (DAOException)

### Modernes Java (Java 14-17)
- ✅ `Optional<T>` statt null-Rückgabewerte
- ✅ Text Blocks (`"""..."""`)
- ✅ Switch Expressions (`case x ->`)
- ✅ `var` für lokale Typinferenz
- ✅ Records (Konzept, nicht implementiert)

### Testing
- ✅ Unit Tests vs. Integrationstests
- ✅ JUnit 4 Annotations (@Test, @Before, @BeforeClass)
- ✅ Test-Isolation mit @FixMethodOrder
- ✅ Graceful Skip mit Assume

### DevOps & Toolchain
- ✅ Maven Build-Lifecycle (compile → test → package)
- ✅ Fat JAR mit maven-assembly-plugin
- ✅ GitHub Actions CI/CD Pipeline
- ✅ PostgreSQL Service Container in CI
- ✅ Maven Dependency Caching

---

*Dokumentation erstellt für device-inventory-cli v1.0.0 — 2026-02-13*
