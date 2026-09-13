-- ParkNow DDL Database Migration Script (Standard MySQL & ANSI SQL Compatible)

-- 1. Roles Table
CREATE TABLE IF NOT EXISTS roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(30) NOT NULL UNIQUE
);
CREATE INDEX IF NOT EXISTS idx_role_name ON roles(name);

-- 2. Users Table
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    phone_number VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_user_email ON users(email);

-- 3. User Roles Join Table
CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_ur_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_ur_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

-- 4. Vehicles Table
CREATE TABLE IF NOT EXISTS vehicles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    license_plate VARCHAR(20) NOT NULL UNIQUE,
    vehicle_type VARCHAR(30) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_veh_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
CREATE INDEX IF NOT EXISTS idx_vehicle_user ON vehicles(user_id);
CREATE INDEX IF NOT EXISTS idx_vehicle_plate ON vehicles(license_plate);

-- 5. Parking Lots Table
CREATE TABLE IF NOT EXISTS parking_lots (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    address VARCHAR(255) NOT NULL,
    city VARCHAR(50) NOT NULL,
    total_capacity INT NOT NULL,
    available_capacity INT NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_lot_city ON parking_lots(city);

-- 6. Parking Slots Table
CREATE TABLE IF NOT EXISTS parking_slots (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    lot_id BIGINT NOT NULL,
    slot_number VARCHAR(20) NOT NULL,
    floor_number INT NOT NULL,
    vehicle_type VARCHAR(30) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'AVAILABLE',
    version INT DEFAULT 0,
    CONSTRAINT fk_slot_lot FOREIGN KEY (lot_id) REFERENCES parking_lots(id) ON DELETE CASCADE,
    CONSTRAINT uq_lot_slot UNIQUE (lot_id, slot_number)
);
CREATE INDEX IF NOT EXISTS idx_slot_lot_status ON parking_slots(lot_id, status);
CREATE INDEX IF NOT EXISTS idx_slot_vehicle_type ON parking_slots(vehicle_type);

-- 7. Pricing Rules Table
CREATE TABLE IF NOT EXISTS pricing_rules (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    lot_id BIGINT NOT NULL,
    vehicle_type VARCHAR(30) NOT NULL,
    base_hourly_rate DECIMAL(10, 2) NOT NULL,
    peak_hour_multiplier DECIMAL(3, 2) DEFAULT 1.00,
    overstay_penalty_rate DECIMAL(10, 2) DEFAULT 20.00,
    CONSTRAINT fk_pricing_lot FOREIGN KEY (lot_id) REFERENCES parking_lots(id) ON DELETE CASCADE
);
CREATE INDEX IF NOT EXISTS idx_pricing_lot_vehicle ON pricing_rules(lot_id, vehicle_type);

-- 8. Reservations Table
CREATE TABLE IF NOT EXISTS reservations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ticket_code VARCHAR(36) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    slot_id BIGINT NOT NULL,
    vehicle_id BIGINT NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'CONFIRMED',
    estimated_amount DECIMAL(10, 2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_res_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_res_slot FOREIGN KEY (slot_id) REFERENCES parking_slots(id),
    CONSTRAINT fk_res_vehicle FOREIGN KEY (vehicle_id) REFERENCES vehicles(id)
);
CREATE INDEX IF NOT EXISTS idx_reservation_ticket ON reservations(ticket_code);
CREATE INDEX IF NOT EXISTS idx_reservation_user ON reservations(user_id);
CREATE INDEX IF NOT EXISTS idx_reservation_slot_dates ON reservations(slot_id, start_time, end_time);

-- 9. Parking Sessions Table
CREATE TABLE IF NOT EXISTS parking_sessions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    reservation_id BIGINT UNIQUE,
    slot_id BIGINT NOT NULL,
    vehicle_id BIGINT NOT NULL,
    entry_time TIMESTAMP NOT NULL,
    exit_time TIMESTAMP,
    status VARCHAR(30) NOT NULL DEFAULT 'IN_PROGRESS',
    total_fee DECIMAL(10, 2),
    CONSTRAINT fk_sess_res FOREIGN KEY (reservation_id) REFERENCES reservations(id),
    CONSTRAINT fk_sess_slot FOREIGN KEY (slot_id) REFERENCES parking_slots(id),
    CONSTRAINT fk_sess_vehicle FOREIGN KEY (vehicle_id) REFERENCES vehicles(id)
);
CREATE INDEX IF NOT EXISTS idx_session_status ON parking_sessions(status);
CREATE INDEX IF NOT EXISTS idx_session_slot ON parking_sessions(slot_id);

-- 10. Payments Table
CREATE TABLE IF NOT EXISTS payments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    reservation_id BIGINT,
    session_id BIGINT,
    user_id BIGINT NOT NULL,
    transaction_id VARCHAR(64) NOT NULL UNIQUE,
    amount DECIMAL(10, 2) NOT NULL,
    payment_status VARCHAR(30) NOT NULL,
    payment_method VARCHAR(30) NOT NULL,
    payment_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_pay_res FOREIGN KEY (reservation_id) REFERENCES reservations(id),
    CONSTRAINT fk_pay_sess FOREIGN KEY (session_id) REFERENCES parking_sessions(id),
    CONSTRAINT fk_pay_user FOREIGN KEY (user_id) REFERENCES users(id)
);
CREATE INDEX IF NOT EXISTS idx_payment_txn ON payments(transaction_id);
CREATE INDEX IF NOT EXISTS idx_payment_user ON payments(user_id);
