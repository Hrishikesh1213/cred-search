-- ========================================================
-- 1. Setup Environment
-- ========================================================

CREATE SCHEMA IF NOT EXISTS flight_search;

-- Set the path so we don't have to repeat 'flight_search.'
SET search_path TO flight_search;

-- Enable pgcrypto for UUID generation
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- ========================================================
-- 2. Cleanup (Drop Old Objects)
-- ========================================================

-- Drop the aggregated tables we no longer need
DROP TABLE IF EXISTS physical_route_seat_counts CASCADE;
DROP TABLE IF EXISTS physical_route_tier_status CASCADE;
DROP TABLE IF EXISTS logical_route_seat_counts CASCADE;
DROP TABLE IF EXISTS logical_route_definition CASCADE;

-- Drop main tables
DROP TABLE IF EXISTS seats CASCADE;
DROP TABLE IF EXISTS logical_routes CASCADE;
DROP TABLE IF EXISTS physical_routes CASCADE;
DROP TABLE IF EXISTS flights CASCADE;
DROP TABLE IF EXISTS cities CASCADE;

-- ========================================================
-- 3. Master Tables
-- ========================================================

-- Table: Cities
CREATE TABLE cities
(
    code          VARCHAR(10)              NOT NULL,
    name          VARCHAR(255)             NOT NULL,

    -- Audit Fields
    created_by    VARCHAR(128)             NOT NULL,
    created_at    TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT timezone('UTC'::text, now()),
    updated_by    VARCHAR(128)             NOT NULL,
    updated_at    TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT timezone('UTC'::text, now()),
    deleted       BOOLEAN                  NOT NULL DEFAULT FALSE,
    deleted_at    TIMESTAMP WITH TIME ZONE,
    deleted_by    VARCHAR(128)             NULL,
    version       INT                      NULL,

    CONSTRAINT pk_cities PRIMARY KEY (code),
    CONSTRAINT uk_cities_name UNIQUE (name)
);

-- Table: Flights
CREATE TABLE flights
(
    flight_number VARCHAR(255)             NOT NULL,
    operator      VARCHAR(255)             NOT NULL,
    capacity      INTEGER                  NOT NULL,

    -- Audit Fields
    created_by    VARCHAR(128)             NOT NULL,
    created_at    TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT timezone('UTC'::text, now()),
    updated_by    VARCHAR(128)             NOT NULL,
    updated_at    TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT timezone('UTC'::text, now()),
    deleted       BOOLEAN                  NOT NULL DEFAULT FALSE,
    deleted_at    TIMESTAMP WITH TIME ZONE,
    deleted_by    VARCHAR(128)             NULL,
    version       INT                      NULL,

    CONSTRAINT pk_flights PRIMARY KEY (flight_number)
);

-- ========================================================
-- 4. Transaction Tables (Routes)
-- ========================================================

-- Table: Physical Routes (PHY_ prefix)
-- This is the parent for the Seats
CREATE TABLE physical_routes
(
    id                  VARCHAR(255)             NOT NULL DEFAULT 'PHY_' || gen_random_uuid()::text,
    source_city_id      VARCHAR(10)              NOT NULL,
    destination_city_id VARCHAR(10)              NOT NULL,
    flight_id           VARCHAR(255)             NOT NULL,
    start_time_epoch    BIGINT                   NOT NULL,
    duration_millis     BIGINT                   NOT NULL,

    -- Audit Fields
    created_by          VARCHAR(128)             NOT NULL,
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT timezone('UTC'::text, now()),
    updated_by          VARCHAR(128)             NOT NULL,
    updated_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT timezone('UTC'::text, now()),
    deleted             BOOLEAN                  NOT NULL DEFAULT FALSE,
    deleted_at          TIMESTAMP WITH TIME ZONE,
    deleted_by          VARCHAR(128)             NULL,
    version             INT                      NULL,

    CONSTRAINT pk_physical_routes PRIMARY KEY (id),
    CONSTRAINT fk_pr_source FOREIGN KEY (source_city_id) REFERENCES cities (code),
    CONSTRAINT fk_pr_dest FOREIGN KEY (destination_city_id) REFERENCES cities (code),
    CONSTRAINT fk_pr_flight FOREIGN KEY (flight_id) REFERENCES flights (flight_number)
);

-- Table: Logical Routes (LOG_ prefix)
-- Optimized for Read/Search with JSONB
CREATE TABLE logical_routes
(
    id                    VARCHAR(255)             NOT NULL,

    source_city_id        VARCHAR(10)              NOT NULL,
    destination_city_id   VARCHAR(10)              NOT NULL,

    start_time_epoch      BIGINT                   NOT NULL,
    total_duration_millis BIGINT                   NOT NULL,
    number_of_stops       INTEGER,

    -- JSONB Columns
    route_legs            JSONB,                   -- Stores List<LegData>
    available_seats_min   JSONB,                   -- Stores Map<Tier, Integer> (e.g., {"ECONOMY": 50})
    prices                JSONB,                   -- Stores Map<Tier, Double>  (e.g., {"ECONOMY": 5000.0})

    -- Audit Fields
    created_by            VARCHAR(128)             NOT NULL,
    created_at            TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT timezone('UTC'::text, now()),
    updated_by            VARCHAR(128)             NOT NULL,
    updated_at            TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT timezone('UTC'::text, now()),
    deleted               BOOLEAN                  NOT NULL DEFAULT FALSE,
    deleted_at            TIMESTAMP WITH TIME ZONE,
    deleted_by            VARCHAR(128)             NULL,
    version               INT                      NULL,

    CONSTRAINT pk_logical_routes PRIMARY KEY (id),
    CONSTRAINT fk_lr_source FOREIGN KEY (source_city_id) REFERENCES cities (code),
    CONSTRAINT fk_lr_dest FOREIGN KEY (destination_city_id) REFERENCES cities (code)
);

-- ========================================================
-- 5. Inventory Tables
-- ========================================================

-- Table: Seats
-- This is the SINGLE SOURCE OF TRUTH for inventory.
CREATE TABLE seats
(
    id                VARCHAR(255)             NOT NULL DEFAULT gen_random_uuid()::text,
    physical_route_id VARCHAR(255)             NOT NULL,
    tier              VARCHAR(50)              NOT NULL,
    seat_number       VARCHAR(10)              NOT NULL,
    is_booked         BOOLEAN                  NOT NULL DEFAULT FALSE,
    price             DOUBLE PRECISION         NOT NULL,

    -- Audit Fields
    created_by        VARCHAR(128)             NOT NULL,
    created_at        TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT timezone('UTC'::text, now()),
    updated_by        VARCHAR(128)             NOT NULL,
    updated_at        TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT timezone('UTC'::text, now()),
    deleted           BOOLEAN                  NOT NULL DEFAULT FALSE,
    deleted_at        TIMESTAMP WITH TIME ZONE,
    deleted_by        VARCHAR(128)             NULL,
    version           INT                      NULL,

    CONSTRAINT pk_seats PRIMARY KEY (id),
    CONSTRAINT fk_seat_pr FOREIGN KEY (physical_route_id) REFERENCES physical_routes (id),

    -- Ensure a specific seat number appears only once per route
    CONSTRAINT uk_seat_per_route UNIQUE (physical_route_id, seat_number)
);