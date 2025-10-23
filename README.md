# Car Rental

A small Spring WebFlux-based simulation of a car rental reservation system.

Features
- Reserve a car of type Sedan, SUV or VAN for a given start date/time and number of days.
- Inventory limits per car type.
- Reactive controller and a simple in-memory inventory.

## High level design

![CarRental-sequence.drawio.png](docs/CarRental-sequence.drawio.png)

## Model
![CarRental-Entities.drawio.png](docs/CarRental-Entities.drawio.png)

Run tests

On Windows (cmd.exe) with Gradle installed:

```
gradle test
```

Or generate Gradle wrapper and use it locally:

```
gradle wrapper
 .\gradlew test
```

Build Docker image and run with compose:

```
docker-compose up --build
```

API
POST /reservation
Body: JSON {"carType":"SEDAN","start":"2025-10-25T10:00:00","days":3}

Example:

curl -d '{"carType":"SEDAN","start":"2025-10-25T10:00:00","days":3}' -H "Content-Type: application/json" -X POST http://localhost:8080/reservation

Response: {"id":"f51bd597-6c9a-4074-b1cb-94e572e5ae8a","carType":"SEDAN","start":"2025-10-25T10:00:00","days":3,"registrationNumber":"ABC-123"}

Notes
- This project includes sample capacities defined in CarRepository (Sedan 1, SUV 2, Van 1).

