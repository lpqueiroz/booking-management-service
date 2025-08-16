# Booking Management Service

Booking management service for houses. The system is designed to handle booking creation requests, check property availability for specified dates, and notify about booking conflicts. The main goal is to ensure correct handling of booking dates, prevent overlapping reservations, and provide users with up-to-date occupancy information.

### Prerequisites

- Java 11+
- sbt 1.7+
- Docker

### Running the Project

1. To start dependencies (PostgreSQL, Kafka) via Docker, run from the root of the project:


```bash
docker-compose up --build
```

2. Then to run the application, run from the root of the project:

```bash
sbt run
```

### Running Tests

For running the tests, run from the root of the project:

```bash
sbt test
```

### Example GraphQL Requests

#### Create a Booking (Mutation)

```
curl --location 'http://localhost:9000/api/v1/graphql' \
--header 'Content-Type: application/json' \
--data-raw '{"query":"mutation {\n  createBooking(\n    homeId: \"67f240fa-6a3b-4472-b230-42757b2caf8f\",\n    fromDate: \"2025-11-29\",\n    toDate: \"2025-11-30\",\n    guestEmail: \"guest@example.com\",\n    source: \"Website\"\n  ) {\n        success,\n        message,\n        alternativeDates {\n            from,\n            to\n        }\n  }\n}\n","variables":{}}'
```

#### List Bookings by Home (Query)

```
curl --location 'http://localhost:9000/api/v1/graphql' \
--header 'Content-Type: application/json' \
--data '{"query":"query {\n  bookings(homeId: \"67f240fa-6a3b-4472-b230-42757b2caf8f\") {\n    id\n    homeId\n    fromDate\n    toDate\n    guestEmail\n    source\n    createdAt\n  }\n}\n","variables":{}}'
```
