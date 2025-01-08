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
    status TEXT NOT NULL DEFAULT 'active',
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
    status TEXT NOT NULL DEFAULT 'active',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS employee (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    email TEXT UNIQUE NOT NULL,
    contactNumber TEXT,
    role TEXT NOT NULL,
    status TEXT NOT NULL DEFAULT 'active',
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
    role TEXT DEFAULT 'admin' NOT NULL,
    refreshToken TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS DeliveryOrder (
    id VARCHAR(255) PRIMARY KEY,
    contractId TEXT,
    partyId TEXT,
    dateOfContract BIGINT,
    status TEXT NOT NULL,
    created_at BIGINT DEFAULT EXTRACT(EPOCH FROM CURRENT_TIMESTAMP) * 1000 NOT NULL
);

CREATE TABLE IF NOT EXISTS DeliveryOrderItem (
    id VARCHAR(255)  PRIMARY KEY,
    deliveryOrderId VARCHAR(255)  NOT NULL,
    district TEXT,
    taluka TEXT,
    locationId TEXT,
    materialId TEXT,
    quantity INT NOT NULL,
    status TEXT NOT NULL DEFAULT 'pending',
    rate INT,
    unit TEXT,
    dueDate BIGINT,
    FOREIGN KEY (deliveryOrderId) REFERENCES DeliveryOrder(id)
);

CREATE TABLE IF NOT EXISTS deliveryChallan (
    id VARCHAR(255) PRIMARY KEY,
    deliveryOrderId VARCHAR(255) NOT NULL REFERENCES DeliveryOrder(id) ON DELETE CASCADE,
    status VARCHAR(255),
    created_at BIGINT,
    updated_at BIGINT,
    dateOfChallan BIGINT,
    totaldeliveringquantity DOUBLE PRECISION DEFAULT 0.0,
    transportationCompanyId TEXT REFERENCES transportationcompany(id) ON DELETE RESTRICT,
    vehicleId TEXT REFERENCES vehicles(id) ON DELETE RESTRICT,
    driverId TEXT REFERENCES drivers(id) ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS deliveryChallanItem (
    id VARCHAR(255) PRIMARY KEY,
    deliveryChallanId VARCHAR(255) REFERENCES deliveryChallan(id) ON DELETE CASCADE,
    deliveryOrderItemId VARCHAR(255) REFERENCES deliveryorderitem(id) ON DELETE CASCADE,
    deliveringQuantity DOUBLE PRECISION DEFAULT 0.0
);

CREATE TABLE IF NOT EXISTS transportationcompany (
    id VARCHAR(255) PRIMARY KEY,
    company_name TEXT NOT NULL,
    point_of_contact TEXT,
    contact_number TEXT,
    email TEXT,
    address_line_1 TEXT,
    address_line_2 TEXT,
    state TEXT,
    city TEXT,
    pin_code TEXT,
    status TEXT NOT NULL DEFAULT 'active',
    created_at BIGINT NOT NULL,
    updated_at BIGINT NOT NULL
);

CREATE TABLE IF NOT EXISTS vehicles (
    id VARCHAR(255) PRIMARY KEY NOT NULL,
    vehicle_number TEXT NOT NULL,
    vehicle_type TEXT,
    rc_book_url TEXT,
    transportationCompanyId VARCHAR(255) NOT NULL REFERENCES transportationcompany(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS drivers (
    id VARCHAR(255) PRIMARY KEY NOT NULL,
    name TEXT NOT NULL,
    contact_number TEXT,
    driving_license_url TEXT,
    transportationCompanyId VARCHAR(255) NOT NULL REFERENCES transportationcompany(id) ON DELETE CASCADE
);