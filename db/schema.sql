-- Device Inventory CLI - Database Schema
-- Run: psql -U portfolio -d devicedb < db/schema.sql

-- PostgreSQL 13+ has gen_random_uuid() built-in (no extension needed)
-- PostgreSQL 16 is used on the VPS, so this works fine.

CREATE TABLE IF NOT EXISTS devices (
    id          UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(100) NOT NULL,
    type        VARCHAR(50)  NOT NULL,
    status      VARCHAR(50)  NOT NULL DEFAULT 'ACTIVE',
    ip_address  VARCHAR(45),
    location    VARCHAR(100),
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- Indexes for common query patterns
CREATE INDEX IF NOT EXISTS idx_devices_type     ON devices(type);
CREATE INDEX IF NOT EXISTS idx_devices_status   ON devices(status);
CREATE INDEX IF NOT EXISTS idx_devices_name     ON devices(LOWER(name));

-- Sample data for quick testing
INSERT INTO devices (name, type, status, ip_address, location) VALUES
    ('Office Laptop 01',  'LAPTOP',         'ACTIVE',      '192.168.1.101', 'Office Floor 2'),
    ('Production Server', 'SERVER',         'ACTIVE',      '10.0.0.10',     'Server Room A'),
    ('HR Printer',        'PRINTER',        'ACTIVE',      '192.168.1.200', 'HR Department'),
    ('Core Router',       'ROUTER',         'ACTIVE',      '10.0.0.1',      'Server Room A'),
    ('Old Workstation',   'OTHER',          'INACTIVE',    '192.168.1.50',  'Storage Room'),
    ('Dev Server',        'SERVER',         'MAINTENANCE', '10.0.0.11',     'Server Room B')
ON CONFLICT DO NOTHING;
