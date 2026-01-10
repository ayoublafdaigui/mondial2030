-- ===================================================================
-- Mondial 2030 - Database Initialization Script
-- This script runs automatically when the PostgreSQL container starts
-- ===================================================================

-- Create extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Grant privileges
GRANT ALL PRIVILEGES ON DATABASE mondial2030 TO mondial_user;

-- ===================================================================
-- CREATE TABLES (Hibernate will auto-create, but this is a fallback)
-- ===================================================================

-- Users table
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(100),
    email VARCHAR(100) UNIQUE,
    role VARCHAR(20) DEFAULT 'USER',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP
);

-- Stadiums table  
CREATE TABLE IF NOT EXISTS stadiums (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    city VARCHAR(100) NOT NULL,
    country VARCHAR(100) NOT NULL,
    capacity INTEGER NOT NULL,
    image_url VARCHAR(500),
    description VARCHAR(1000),
    year_built INTEGER,
    is_main_venue BOOLEAN DEFAULT FALSE
);

-- Matches table
CREATE TABLE IF NOT EXISTS matches (
    id BIGSERIAL PRIMARY KEY,
    home_team VARCHAR(100) NOT NULL,
    away_team VARCHAR(100) NOT NULL,
    match_date TIMESTAMP NOT NULL,
    stadium VARCHAR(200) NOT NULL,
    city VARCHAR(100) NOT NULL,
    phase VARCHAR(50) DEFAULT 'GROUP_STAGE',
    base_price DECIMAL(10,2),
    total_seats INTEGER,
    available_seats INTEGER
);

-- Tickets table
CREATE TABLE IF NOT EXISTS tickets (
    id BIGSERIAL PRIMARY KEY,
    seat_number VARCHAR(100) NOT NULL,
    seat_zone VARCHAR(50),
    price DECIMAL(10,2) NOT NULL,
    category VARCHAR(20) DEFAULT 'STANDARD',
    status VARCHAR(20) DEFAULT 'ACTIVE',
    blockchain_hash VARCHAR(255) UNIQUE,
    fraud_score DECIMAL(3,2) DEFAULT 0.00,
    transfer_count INTEGER DEFAULT 0,
    purchase_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_transfer_date TIMESTAMP,
    owner_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
    match_id BIGINT NOT NULL REFERENCES matches(id) ON DELETE CASCADE
);

-- Ticket transfers table
CREATE TABLE IF NOT EXISTS ticket_transfers (
    id BIGSERIAL PRIMARY KEY,
    ticket_id BIGINT NOT NULL REFERENCES tickets(id) ON DELETE CASCADE,
    from_user_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
    to_user_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
    transfer_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    transfer_price DECIMAL(10,2),
    blockchain_tx_hash VARCHAR(255)
);

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_tickets_owner ON tickets(owner_id);
CREATE INDEX IF NOT EXISTS idx_tickets_match ON tickets(match_id);
CREATE INDEX IF NOT EXISTS idx_matches_date ON matches(match_date);
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);

-- Log initialization
DO $$
BEGIN
    RAISE NOTICE 'Mondial 2030 database tables initialized successfully!';
END $$;
