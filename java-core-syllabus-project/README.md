# Core Java Syllabus Manager (100% Java + MySQL + Swing GUI)

This desktop project is built exactly for the syllabus topics you shared.

## Syllabus Coverage
- **Unit I:** OOP (classes, constructors, inheritance-ready model design), packages, encapsulation.
- **Unit II:** Exception handling, generics/collections, Stream API.
- **Unit III:** File I/O (NIO), serialization/deserialization, multithreading.
- **Unit IV:** GUI in Swing/AWT event model + Java networking (`URL`, `HttpURLConnection`).

## Tech Stack
- Java 17
- Swing (GUI)
- Core Java HTTP Server API (`com.sun.net.httpserver.HttpServer`)
- JDBC + MySQL
- Maven
- JUnit 5

## Project Structure
```text
java-core-syllabus-project/
├── db/schema.sql
├── src/main/java/com/syllabus/app/
│   ├── Main.java
│   ├── model/StudentRecord.java
│   ├── db/DatabaseConfig.java, DatabaseManager.java
│   ├── dao/...
│   ├── service/...
│   ├── util/FileManager.java
│   ├── network/HttpPingClient.java
│   ├── api/hotel/HotelApiServer.java
│   └── ui/MainFrame.java, BackgroundPanel.java
└── src/main/resources/images/
    └── README.txt  (put your image as syllabus-bg.png)
```

## Use Your Syllabus Image
1. Save your provided image as:
   `src/main/resources/images/syllabus-bg.png`
2. App automatically uses it as background.

## MySQL Setup
```sql
SOURCE db/schema.sql;
```

## Environment Variables (optional)
```bash
export JAVA_DB_URL="jdbc:mysql://localhost:3306/java_syllabus_db"
export JAVA_DB_USER="root"
export JAVA_DB_PASSWORD="root"
export HOTEL_API_PORT="8081"
```

## Run
```bash
mvn clean test
mvn exec:java
```

## Run Hotel Management API
```bash
mvn -Dexec.mainClass=com.syllabus.app.api.hotel.HotelApiServer exec:java
```

### API Endpoints
- `GET /api/health` : service health check
- `GET /api/rooms` : list all rooms
- `POST /api/rooms` : create room
- `GET /api/bookings` : list all bookings
- `POST /api/bookings` : create booking

#### Sample JSON payloads
```json
{
  "roomNumber": "A-101",
  "type": "DELUXE",
  "pricePerNight": "3500",
  "available": "true"
}
```

```json
{
  "guestName": "John",
  "guestEmail": "john@example.com",
  "roomId": "1",
  "checkInDate": "2026-01-10",
  "checkOutDate": "2026-01-12",
  "bookingStatus": "CONFIRMED"
}
```

## Features in GUI
- Create/Update/Delete student module records
- Table view with refresh
- Stream analytics panel
- Networking panel (HTTP fetch)
- CSV export to `exports/records.csv`
- Automatic serialization backups every 60 sec
