# Device Inventory CLI

Java command-line application for managing a device inventory with PostgreSQL database.

## Project 1 - Learning Goal

Build foundational Java skills:
- JDBC and database connectivity (Connection, PreparedStatement, ResultSet)
- DAO pattern for data access
- HikariCP connection pooling
- CLI user interface with Scanner
- Clean code principles and package structure

## Tech Stack

- **Language:** Java 17
- **Build:** Maven
- **Database:** PostgreSQL 16
- **Connection Pool:** HikariCP
- **JDBC Driver:** org.postgresql

## Quick Start

### Prerequisites

- Java 17+
- Maven 3.8+
- PostgreSQL 16 running with `portfolio` user
- Database: `devicedb`

### IDE Setup

**Eclipse:** File → Import → Existing Maven Projects → select project root

**IntelliJ IDEA:** File → Open → select `pom.xml` → Open as Project

### Build

```bash
mvn clean package
```

### Run

```bash
java -jar target/device-inventory-cli-1.0.0-jar-with-dependencies.jar
```

## Database Setup

Create the schema:

```sql
CREATE TABLE devices (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    ip_address VARCHAR(15),
    location VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE device_logs (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    device_id UUID NOT NULL REFERENCES devices(id) ON DELETE CASCADE,
    event_type VARCHAR(50),
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

## Features

- **View Devices:** List all devices with status
- **Add Device:** Create new device in inventory
- **Update Device:** Modify device details
- **Delete Device:** Remove device from inventory
- **Search:** Find devices by name or IP
- **Filter:** Filter devices by status or type
- **Logs:** View device activity logs

## Project Structure

```
device-inventory-cli/
├── src/
│   ├── main/
│   │   └── java/com/mj/portfolio/
│   │       ├── model/
│   │       │   ├── Device.java
│   │       │   └── DeviceLog.java
│   │       ├── dao/
│   │       │   ├── DeviceDAO.java
│   │       │   └── DeviceLogDAO.java
│   │       ├── service/
│   │       │   └── DeviceService.java
│   │       ├── cli/
│   │       │   └── DeviceInventoryCLI.java
│   │       └── db/
│   │           └── DatabaseConfig.java
│   └── test/
│       └── java/com/mj/portfolio/
│           └── dao/
│               └── DeviceDAOTest.java
├── src/main/resources/
│   └── db.properties
├── pom.xml
├── README.md
└── LEARNING.md
```

## Configuration

Create `src/main/resources/db.properties`:

```properties
db.url=jdbc:postgresql://localhost:5432/devicedb
db.user=portfolio
db.password=portfolio_dev_password
db.pool.size=10
db.pool.max.lifetime=1800000
```

## Development Workflow

This project was developed using an agile, issue-driven workflow:

- **Issue tracking** via GitHub Issues and GitLab Boards (Kanban-style)
- **Conventional commits** (`feat:`, `fix:`, `test:`, `refactor:`, `docs:`)
- **Dual-remote** repository (GitHub + GitLab)
- **AI-assisted development** with Claude Code (~80% implementation, human architecture decisions)

---

## Learning Documentation

See `LEARNING.md` for detailed explanations of:
- JDBC fundamentals
- DAO pattern
- Connection pooling
- SQL basics

## Next Steps

After this project, you'll have:
- ✓ Working JDBC/SQL skills
- ✓ Understanding of data persistence
- ✓ CLI application development
- ✓ Foundation for Project 2 (REST API)
