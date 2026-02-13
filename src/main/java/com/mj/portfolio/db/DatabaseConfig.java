package com.mj.portfolio.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Manages the HikariCP connection pool as a lazily-initialised singleton.
 *
 * <p>Configuration is loaded in this order:
 * <ol>
 *   <li>A {@code db.properties} file in the current working directory
 *       (useful when running the fat-JAR from a folder that contains the file).</li>
 *   <li>A {@code /db.properties} resource on the classpath
 *       (placed under {@code src/main/resources/} during development).</li>
 * </ol>
 * Copy {@code db.properties.example} → {@code db.properties} and fill in
 * your credentials. The file is excluded from version control via .gitignore.
 * </p>
 *
 * <p>Why HikariCP?  Opening a new TCP connection to PostgreSQL for every
 * query is expensive (TLS handshake, authentication round-trip, etc.).
 * A pool keeps a fixed number of connections alive and hands them out to
 * callers — typically 10-20× faster than opening-and-closing each time.</p>
 */
public class DatabaseConfig {

    private static HikariDataSource dataSource;

    private DatabaseConfig() {}

    /**
     * Returns the shared {@link DataSource}.
     * The pool is created on first call and reused thereafter (lazy singleton).
     */
    public static synchronized DataSource getDataSource() {
        if (dataSource == null || dataSource.isClosed()) {
            dataSource = createPool();
        }
        return dataSource;
    }

    /** Closes the connection pool. Call once on application shutdown. */
    public static synchronized void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }

    // ── Private helpers ──────────────────────────────────────────────────────

    private static HikariDataSource createPool() {
        Properties props = loadProperties();

        HikariConfig cfg = new HikariConfig();
        cfg.setJdbcUrl(props.getProperty("db.url"));
        cfg.setUsername(props.getProperty("db.username"));
        cfg.setPassword(props.getProperty("db.password"));
        cfg.setMaximumPoolSize(Integer.parseInt(
                props.getProperty("db.pool.size", "10")));

        cfg.setPoolName("DeviceInventoryPool");
        cfg.setConnectionTestQuery("SELECT 1");
        cfg.setConnectionTimeout(30_000);   // 30 s to get a connection from pool
        cfg.setIdleTimeout(600_000);        // 10 min before idle connection is closed
        cfg.setMaxLifetime(1_800_000);      // 30 min max lifetime per connection

        return new HikariDataSource(cfg);
    }

    private static Properties loadProperties() {
        Properties props = new Properties();

        // 1. Try external file next to the JAR / in current directory
        File external = new File("db.properties");
        if (external.exists()) {
            try (InputStream in = new FileInputStream(external)) {
                props.load(in);
                return props;
            } catch (IOException ignored) {
                // fall through to classpath
            }
        }

        // 2. Try classpath (src/main/resources/db.properties)
        try (InputStream in = DatabaseConfig.class.getResourceAsStream("/db.properties")) {
            if (in == null) {
                throw new RuntimeException(
                        "db.properties not found!\n"
                        + "Copy src/main/resources/db.properties.example "
                        + "→ src/main/resources/db.properties and fill in your credentials.");
            }
            props.load(in);
            return props;
        } catch (IOException e) {
            throw new RuntimeException("Failed to read db.properties", e);
        }
    }
}
