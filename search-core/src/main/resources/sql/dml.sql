SET search_path TO flight_search;

-- ========================================================
-- 1. Insert Master Data: Cities
-- ========================================================
INSERT INTO cities (code, name, created_by, updated_by, created_at, updated_at, deleted, version)
VALUES
    ('DEL', 'New Delhi', 'SYSTEM', 'SYSTEM', NOW(), NOW(), FALSE, 0),
    ('BOM', 'Mumbai',    'SYSTEM', 'SYSTEM', NOW(), NOW(), FALSE, 0),
    ('JAI', 'Jaipur',    'SYSTEM', 'SYSTEM', NOW(), NOW(), FALSE, 0),
    ('AMD', 'Ahmedabad', 'SYSTEM', 'SYSTEM', NOW(), NOW(), FALSE, 0);

-- ========================================================
-- 2. Insert Master Data: Flights
-- ========================================================
INSERT INTO flights (flight_number, operator, capacity, created_by, updated_by, created_at, updated_at, deleted, version)
VALUES
    ('AI-101', 'Air India', 180, 'SYSTEM', 'SYSTEM', NOW(), NOW(), FALSE, 0),
    ('6E-202', 'IndiGo',    180, 'SYSTEM', 'SYSTEM', NOW(), NOW(), FALSE, 0),
    ('AI-303', 'Air India', 150, 'SYSTEM', 'SYSTEM', NOW(), NOW(), FALSE, 0),
    ('SG-404', 'SpiceJet',  180, 'SYSTEM', 'SYSTEM', NOW(), NOW(), FALSE, 0),
    ('UK-505', 'Vistara',   160, 'SYSTEM', 'SYSTEM', NOW(), NOW(), FALSE, 0),
    ('LUF-505', 'Luftansa',   6, 'SYSTEM', 'SYSTEM', NOW(), NOW(), FALSE, 0);

-- ========================================================
-- 3. Insert Transaction Data: Physical Routes (Legs)
-- ========================================================
-- Timestamps are in Milliseconds (13 digits)

-- Leg 1: DEL -> BOM (Direct)
INSERT INTO physical_routes (id, source_city_id, destination_city_id, flight_id, start_time_epoch, duration_millis, created_by, updated_by, created_at, updated_at, deleted, version)
VALUES ('PHY_DEL_BOM_01', 'DEL', 'BOM', 'AI-101', 1700000000000, 7200000, 'SYSTEM', 'SYSTEM', NOW(), NOW(), FALSE, 0);

-- Leg 2: DEL -> JAI
INSERT INTO physical_routes (id, source_city_id, destination_city_id, flight_id, start_time_epoch, duration_millis, created_by, updated_by, created_at, updated_at, deleted, version)
VALUES ('PHY_DEL_JAI_01', 'DEL', 'JAI', '6E-202', 1700000000000, 3600000, 'SYSTEM', 'SYSTEM', NOW(), NOW(), FALSE, 0);

-- Leg 3: JAI -> BOM
INSERT INTO physical_routes (id, source_city_id, destination_city_id, flight_id, start_time_epoch, duration_millis, created_by, updated_by, created_at, updated_at, deleted, version)
VALUES ('PHY_JAI_BOM_01', 'JAI', 'BOM', 'AI-303', 1700010800000, 5400000, 'SYSTEM', 'SYSTEM', NOW(), NOW(), FALSE, 0);

-- Leg 4: JAI -> AMD
INSERT INTO physical_routes (id, source_city_id, destination_city_id, flight_id, start_time_epoch, duration_millis, created_by, updated_by, created_at, updated_at, deleted, version)
VALUES ('PHY_JAI_AMD_01', 'JAI', 'AMD', 'SG-404', 1700010800000, 3600000, 'SYSTEM', 'SYSTEM', NOW(), NOW(), FALSE, 0);

-- Leg 5: AMD -> BOM
INSERT INTO physical_routes (id, source_city_id, destination_city_id, flight_id, start_time_epoch, duration_millis, created_by, updated_by, created_at, updated_at, deleted, version)
VALUES ('PHY_AMD_BOM_01', 'AMD', 'BOM', 'UK-505', 1700020000000, 3600000, 'SYSTEM', 'SYSTEM', NOW(), NOW(), FALSE, 0);


-- ========================================================
-- 4. Insert Transaction Data: Logical Routes
-- ========================================================

-- Case 1: Direct (DEL -> BOM)
-- Price: 5000 (Eco) / 12000 (Bus)
INSERT INTO logical_routes (
    id, source_city_id, destination_city_id, start_time_epoch, total_duration_millis, number_of_stops,
    route_legs, available_seats_min, prices,
    created_by, updated_by, created_at, updated_at, deleted, version
)
VALUES (
           'LOG_DEL_BOM_DIRECT', 'DEL', 'BOM', 1700000000000, 7200000, 0,
           -- Legs JSON
           '[
             {
               "flightNumber": "AI-101", "operator": "Air India",
               "source": "DEL", "destination": "BOM",
               "departureTimeEpoch": 1700000000000, "arrivalTimeEpoch": 1700007200000, "durationMinutes": 120
             }
           ]'::jsonb,
           -- Seats JSON (3 Unbooked in Seats Table)
           '{ "ECONOMY": 3, "BUSINESS": 3 }'::jsonb,
           -- Prices JSON
           '{ "ECONOMY": 5000.0, "BUSINESS": 12000.0 }'::jsonb,
           'SYSTEM', 'SYSTEM', NOW(), NOW(), FALSE, 0
       );

-- Case 2: 1 Stop (DEL -> JAI -> BOM)
-- Price: 3000+4000 = 7000 (Eco) | 8000+9000 = 17000 (Bus)
INSERT INTO logical_routes (
    id, source_city_id, destination_city_id, start_time_epoch, total_duration_millis, number_of_stops,
    route_legs, available_seats_min, prices,
    created_by, updated_by, created_at, updated_at, deleted, version
)
VALUES (
           'LOG_DEL_BOM_1STOP', 'DEL', 'BOM', 1700000000000, 16200000, 1,
           -- Legs JSON
           '[
             {
               "flightNumber": "6E-202", "operator": "IndiGo",
               "source": "DEL", "destination": "JAI",
               "departureTimeEpoch": 1700000000000, "arrivalTimeEpoch": 1700003600000, "durationMinutes": 60
             },
             {
               "flightNumber": "AI-303", "operator": "Air India",
               "source": "JAI", "destination": "BOM",
               "departureTimeEpoch": 1700010800000, "arrivalTimeEpoch": 1700016200000, "durationMinutes": 90
             }
           ]'::jsonb,
           -- Seats JSON (Min availability across legs is 3)
           '{ "ECONOMY": 3, "BUSINESS": 3 }'::jsonb,
           -- Prices JSON (Sum of legs)
           '{ "ECONOMY": 7000.0, "BUSINESS": 17000.0 }'::jsonb,
           'SYSTEM', 'SYSTEM', NOW(), NOW(), FALSE, 0
       );

-- Case 3: 2 Stops (DEL -> JAI -> AMD -> BOM)
-- Price: 3000+2500+3500 = 9000 (Eco) | 8000+6000+7500 = 21500 (Bus)
INSERT INTO logical_routes (
    id, source_city_id, destination_city_id, start_time_epoch, total_duration_millis, number_of_stops,
    route_legs, available_seats_min, prices,
    created_by, updated_by, created_at, updated_at, deleted, version
)
VALUES (
           'LOG_DEL_BOM_2STOP', 'DEL', 'BOM', 1700000000000, 23600000, 2,
           -- Legs JSON
           '[
             {
               "flightNumber": "6E-202", "operator": "IndiGo",
               "source": "DEL", "destination": "JAI",
               "departureTimeEpoch": 1700000000000, "arrivalTimeEpoch": 1700003600000, "durationMinutes": 60
             },
             {
               "flightNumber": "SG-404", "operator": "SpiceJet",
               "source": "JAI", "destination": "AMD",
               "departureTimeEpoch": 1700010800000, "arrivalTimeEpoch": 1700014400000, "durationMinutes": 60
             },
             {
               "flightNumber": "UK-505", "operator": "Vistara",
               "source": "AMD", "destination": "BOM",
               "departureTimeEpoch": 1700020000000, "arrivalTimeEpoch": 1700023600000, "durationMinutes": 60
             }
           ]'::jsonb,
           -- Seats JSON (Min availability across legs is 3)
           '{ "ECONOMY": 3, "BUSINESS": 3 }'::jsonb,
           -- Prices JSON (Sum of legs)
           '{ "ECONOMY": 9000.0, "BUSINESS": 21500.0 }'::jsonb,
           'SYSTEM', 'SYSTEM', NOW(), NOW(), FALSE, 0
       );

-- ========================================================
-- 5. Insert Inventory (Seats)
-- ========================================================
-- ========================================================
-- 5. Insert Inventory (Seats)
-- ========================================================
-- Rules applied:
-- 1. All seats are UNBOOKED (FALSE).
-- 2. 3 Economy Seats (Row 1) per flight.
-- 3. 3 Business Seats (Row 2) per flight.
-- 4. Business Price > Economy Price.

INSERT INTO seats (physical_route_id, tier, seat_number, is_booked, price, created_by, updated_by, created_at, updated_at, deleted, version)
VALUES
    -- 1. Leg: DEL -> BOM (PHY_DEL_BOM_01)
    ('PHY_DEL_BOM_01', 'ECONOMY', '1A', FALSE, 5000.00,  'SYSTEM', 'SYSTEM', NOW(), NOW(), FALSE, 0),
    ('PHY_DEL_BOM_01', 'ECONOMY', '1B', FALSE, 5000.00,  'SYSTEM', 'SYSTEM', NOW(), NOW(), FALSE, 0),
    ('PHY_DEL_BOM_01', 'ECONOMY', '1C', FALSE, 5000.00,  'SYSTEM', 'SYSTEM', NOW(), NOW(), FALSE, 0),
    ('PHY_DEL_BOM_01', 'BUSINESS', '2A', FALSE, 12000.00, 'SYSTEM', 'SYSTEM', NOW(), NOW(), FALSE, 0),
    ('PHY_DEL_BOM_01', 'BUSINESS', '2B', FALSE, 12000.00, 'SYSTEM', 'SYSTEM', NOW(), NOW(), FALSE, 0),
    ('PHY_DEL_BOM_01', 'BUSINESS', '2C', FALSE, 12000.00, 'SYSTEM', 'SYSTEM', NOW(), NOW(), FALSE, 0),

    -- 2. Leg: DEL -> JAI (PHY_DEL_JAI_01)
    ('PHY_DEL_JAI_01', 'ECONOMY', '1A', FALSE, 3000.00,  'SYSTEM', 'SYSTEM', NOW(), NOW(), FALSE, 0),
    ('PHY_DEL_JAI_01', 'ECONOMY', '1B', FALSE, 3000.00,  'SYSTEM', 'SYSTEM', NOW(), NOW(), FALSE, 0),
    ('PHY_DEL_JAI_01', 'ECONOMY', '1C', FALSE, 3000.00,  'SYSTEM', 'SYSTEM', NOW(), NOW(), FALSE, 0),
    ('PHY_DEL_JAI_01', 'BUSINESS', '2A', FALSE, 8000.00,  'SYSTEM', 'SYSTEM', NOW(), NOW(), FALSE, 0),
    ('PHY_DEL_JAI_01', 'BUSINESS', '2B', FALSE, 8000.00,  'SYSTEM', 'SYSTEM', NOW(), NOW(), FALSE, 0),
    ('PHY_DEL_JAI_01', 'BUSINESS', '2C', FALSE, 8000.00,  'SYSTEM', 'SYSTEM', NOW(), NOW(), FALSE, 0),

    -- 3. Leg: JAI -> BOM (PHY_JAI_BOM_01)
    ('PHY_JAI_BOM_01', 'ECONOMY', '1A', FALSE, 4000.00,  'SYSTEM', 'SYSTEM', NOW(), NOW(), FALSE, 0),
    ('PHY_JAI_BOM_01', 'ECONOMY', '1B', FALSE, 4000.00,  'SYSTEM', 'SYSTEM', NOW(), NOW(), FALSE, 0),
    ('PHY_JAI_BOM_01', 'ECONOMY', '1C', FALSE, 4000.00,  'SYSTEM', 'SYSTEM', NOW(), NOW(), FALSE, 0),
    ('PHY_JAI_BOM_01', 'BUSINESS', '2A', FALSE, 9000.00,  'SYSTEM', 'SYSTEM', NOW(), NOW(), FALSE, 0),
    ('PHY_JAI_BOM_01', 'BUSINESS', '2B', FALSE, 9000.00,  'SYSTEM', 'SYSTEM', NOW(), NOW(), FALSE, 0),
    ('PHY_JAI_BOM_01', 'BUSINESS', '2C', FALSE, 9000.00,  'SYSTEM', 'SYSTEM', NOW(), NOW(), FALSE, 0),

    -- 4. Leg: JAI -> AMD (PHY_JAI_AMD_01)
    ('PHY_JAI_AMD_01', 'ECONOMY', '1A', FALSE, 2500.00,  'SYSTEM', 'SYSTEM', NOW(), NOW(), FALSE, 0),
    ('PHY_JAI_AMD_01', 'ECONOMY', '1B', FALSE, 2500.00,  'SYSTEM', 'SYSTEM', NOW(), NOW(), FALSE, 0),
    ('PHY_JAI_AMD_01', 'ECONOMY', '1C', FALSE, 2500.00,  'SYSTEM', 'SYSTEM', NOW(), NOW(), FALSE, 0),
    ('PHY_JAI_AMD_01', 'BUSINESS', '2A', FALSE, 6000.00,  'SYSTEM', 'SYSTEM', NOW(), NOW(), FALSE, 0),
    ('PHY_JAI_AMD_01', 'BUSINESS', '2B', FALSE, 6000.00,  'SYSTEM', 'SYSTEM', NOW(), NOW(), FALSE, 0),
    ('PHY_JAI_AMD_01', 'BUSINESS', '2C', FALSE, 6000.00,  'SYSTEM', 'SYSTEM', NOW(), NOW(), FALSE, 0),

    -- 5. Leg: AMD -> BOM (PHY_AMD_BOM_01)
    ('PHY_AMD_BOM_01', 'ECONOMY', '1A', FALSE, 3500.00,  'SYSTEM', 'SYSTEM', NOW(), NOW(), FALSE, 0),
    ('PHY_AMD_BOM_01', 'ECONOMY', '1B', FALSE, 3500.00,  'SYSTEM', 'SYSTEM', NOW(), NOW(), FALSE, 0),
    ('PHY_AMD_BOM_01', 'ECONOMY', '1C', FALSE, 3500.00,  'SYSTEM', 'SYSTEM', NOW(), NOW(), FALSE, 0),
    ('PHY_AMD_BOM_01', 'BUSINESS', '2A', FALSE, 7500.00,  'SYSTEM', 'SYSTEM', NOW(), NOW(), FALSE, 0),
    ('PHY_AMD_BOM_01', 'BUSINESS', '2B', FALSE, 7500.00,  'SYSTEM', 'SYSTEM', NOW(), NOW(), FALSE, 0),
    ('PHY_AMD_BOM_01', 'BUSINESS', '2C', FALSE, 7500.00,  'SYSTEM', 'SYSTEM', NOW(), NOW(), FALSE, 0);