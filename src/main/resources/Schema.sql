CREATE TABLE IF NOT EXISTS location (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    pointOfContact TEXT,
    contactNumber TEXT,
    email TEXT,
    address1 TEXT,
    address2 TEXT,
    state TEXT,
    district TEXT NOT NULL,
    taluka TEXT NOT NULL,
    city TEXT,
    pincode VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS party_location (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    pointOfContact TEXT,
    contactNumber TEXT,
    email TEXT,
    addressLine1 TEXT,
    addressLine2 TEXT,
    state TEXT,
    district TEXT,
    taluka TEXT,
    city TEXT,
    pincode INTEGER,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS employees (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    email TEXT UNIQUE NOT NULL,
    contactNumber TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS roles (
    id TEXT PRIMARY KEY,
    role_name TEXT UNIQUE NOT NULL
);

CREATE TABLE IF NOT EXISTS employee_roles (
    id TEXT NOT NULL,
    role_id TEXT NOT NULL,
    PRIMARY KEY (id, role_id),
    FOREIGN KEY (id) REFERENCES employees(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

