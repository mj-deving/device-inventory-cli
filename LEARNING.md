# LEARNING.md — device-inventory-cli

Concepts practised and learned while building this project.

---

## JDBC (Java Database Connectivity)

JDBC is Java's standard API for communicating with relational databases.
Think of it as a universal adapter plug: your Java code always uses the same
`Connection / Statement / ResultSet` API regardless of whether the database is
PostgreSQL, MySQL, or SQLite — only the JDBC *driver* (a `.jar`) changes.

### Key classes
| Class | Purpose |
|-------|---------|
| `DriverManager` | Opens raw connections (not used here — see HikariCP) |
| `Connection` | A single session with the database |
| `PreparedStatement` | Pre-compiled SQL with `?` placeholders |
| `ResultSet` | Cursor over returned rows |

### Why PreparedStatement instead of Statement?
```java
// DANGEROUS — SQL Injection risk:
String sql = "SELECT * FROM devices WHERE name = '" + input + "'";

// SAFE — PreparedStatement:
String sql = "SELECT * FROM devices WHERE name = ?";
ps.setString(1, input);   // input is escaped automatically
```
A malicious input like `'; DROP TABLE devices; --` becomes harmless data
when bound via `setString()` because the driver escapes it before sending.

---

## HikariCP — Connection Pooling

Opening a database connection is expensive:
1. TCP handshake
2. TLS negotiation (on secure servers)
3. PostgreSQL authentication round-trip

HikariCP keeps a **pool** of open connections (default: 10) and lends them to
threads on demand. When your code calls `dataSource.getConnection()` it gets a
connection from the pool; when the `try-with-resources` block ends the
connection is *returned* to the pool (not actually closed).

```
Application Thread 1 ──┐
Application Thread 2 ──┤──> HikariCP Pool [conn1, conn2, ... conn10] ──> PostgreSQL
Application Thread 3 ──┘
```

Result: ~20× faster than opening a new connection per query.

---

## DAO Pattern (Data Access Object)

The DAO pattern isolates all database code behind an interface:

```
CLI  ──>  DeviceService  ──>  DeviceDAO (interface)
                                    │
                              DeviceDAOImpl  ──>  PostgreSQL
```

Benefits:
- The service layer knows no SQL, only method calls
- You can swap PostgreSQL for H2 (in-memory) in tests by providing a different
  `DeviceDAO` implementation
- Single Responsibility: each class does one thing

---

## UUID Primary Keys

Instead of auto-increment integers (`1, 2, 3, ...`), this project uses UUIDs
(`550e8400-e29b-41d4-a716-446655440000`).

Why UUIDs?
- **No coordination needed**: any server can generate a unique ID independently
- **No information leakage**: users can't guess sequential IDs
- **Merge-friendly**: IDs from two databases never collide

PostgreSQL 13+ generates UUIDs natively with `gen_random_uuid()`.

---

## try-with-resources

Java's automatic resource management — guarantees `close()` is called even
if an exception occurs:

```java
try (Connection conn = dataSource.getConnection();
     PreparedStatement ps = conn.prepareStatement(sql);
     ResultSet rs = ps.executeQuery()) {

    // use rs...

} // conn, ps, rs all closed automatically here
```

This prevents connection leaks, which would eventually exhaust the pool.

---

## PostgreSQL-specific: RETURNING

Standard SQL uses two separate statements for insert-then-read:
```sql
INSERT INTO devices (...) VALUES (...);
SELECT id, created_at FROM devices WHERE ...;
```

PostgreSQL supports `RETURNING` to do it in one round-trip:
```sql
INSERT INTO devices (...) VALUES (...) RETURNING id, created_at;
```

This is more efficient and avoids a race condition between insert and read.
