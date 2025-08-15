# Booking Management Service

Booking management service for houses. The system is designed to handle booking creation requests, check property availability for specified dates, and notify about booking conflicts. The main goal is to ensure correct handling of booking dates, prevent overlapping reservations, and provide users with up-to-date occupancy information.

### Prerequisites

- Java 11+
- sbt 1.7+
- Docker

### Run the Project

From the root of the project, run:

```bash
docker-compose up --build
```

Then run the application, run the command below from the root of the project:

```bash
sbt run
```

For running the tests, run the command below from the root of the project:

```bash
sbt test
```
