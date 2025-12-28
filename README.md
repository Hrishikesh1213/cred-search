# Airline Search & Aggregator Service

A high-performance search engine backend for an airline aggregator. This service handles flight inventory, route discovery (direct & connecting), and advanced filtering/sorting capabilities for flight bookings.

## 🚀 System Overview

The system is designed to handle high-read throughput for flight searches while maintaining eventual consistency with the flight inventory.

### Key Features
* **Smart Route Discovery:** Automatically detects connecting flights (up to 2 stops) using Graph Traversal (BFS).
* **Pre-computed Routes:** Uses a "Write-Model / Read-Model" separation. When physical flight legs are added, the system pre-computes valid logical routes and stores them.
* **Dynamic Filtering:** Filter by Source, Destination, Date, Passenger Count, and Seat Tier.
* **Sorting:** Options to sort by **Cheapest** (Price) or **Fastest** (Duration).

---

## 🛠 Prerequisites

* **Java 17+**
* **Maven**
* **PostgreSQL**

---

## Assumptions and mocking

1) When a physical Route is added to inventory system, the write to DB triggers an event to notify "cred-search" of the new Route.
since we can't replicate that here, we have exposed and API from which we can call to simulate that event.



2) We will be using ElasticSearch for storing our Calculated routes but Here we are using PostgreSQL instead of ElasticSearch for simplicity.


---

## ⚙️ Setup & Installation

### 1. Database Configuration
Ensure your PostgreSQL instance is running. Initialize the schema and seed test data using the provided SQL scripts:

1.  Run the **DDL** script to create tables:
    `search-core/src/main/resources/sql/ddl.sql`
2.  Run the **DML** script to insert mock cities and flight legs:
    `search-core/src/main/resources/sql/dml.sql`
3. update DB useranme and password in `search-core/src/main/resources/application.properties` file.

### 2. Build and Run
```bash
mvn clean install
```

---

## 🔌 API Reference & Testing

### 1. Fetch flights(Main API)
```
curl --location 'http://localhost:9393/search/v1/' \
--header 'Accept: application/json' \
--header 'Content-Type: application/json' \
--data '{
    "source": "DEL",
    "destination": "BOM",
    "travelDate": "2023-11-14T22:00:00.000Z",
    "numberOfPassengers": 1,
    "tier": "ECONOMY",
    "sortBy": "CHEAPEST"
}'
```

### 2. Simulate a Physical Route Addition
```
curl --location 'http://localhost:9393/inventory/v1/addPhysicalRoute' \
--header 'Accept: application/json' \
--header 'Content-Type: application/json' \
--data '{
    "sourceCityId": "DEL",
    "destinationCityId": "AMD",
    "flightId": "LUF-505",
    "departureTimeEpoch": 1700011000000,
    "arrivalTimeEpoch": 1700047000000,
    "durationMinutes": 90,
    "seats": [
        {
            "seatId": "2A",
            "tier": "ECONOMY",
            "isBooked": false,
            "price": 4500.0
        },
        {
            "seatId": "2B",
            "tier": "ECONOMY",
            "isBooked": true,
            "price": 4500.0
        },
        {
            "seatId": "2C",
            "tier": "ECONOMY",
            "isBooked": false,
            "price": 4500.0
        },
        {
            "seatId": "1A",
            "tier": "BUSINESS",
            "isBooked": false,
            "price": 10000.0
        },
        {
            "seatId": "1B",
            "tier": "BUSINESS",
            "isBooked": true,
            "price": 10000.0
        },
        {
            "seatId": "1C",
            "tier": "BUSINESS",
            "isBooked": false,
            "price": 10000.0
        }
    ]
}'
```

### 3. Now if you call the Fetch Flights API again, you should see the new route(DEL -> AMD, AMD -> BOM) reflected in the results.
