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

CREATE TABLE IF NOT EXISTS employee (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    email TEXT UNIQUE NOT NULL,
    contactNumber TEXT,
    role TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS material (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS Users (
    id TEXT PRIMARY KEY,
    username TEXT NOT NULL,
    email TEXT,
    passwordHash TEXT NOT NULL,
    role TEXT DEFAULT 'User' NOT NULL,
    refreshToken TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);