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
```

## Run
```bash
mvn clean test
mvn exec:java
```

## Features in GUI
- Create/Update/Delete student module records
- Table view with refresh
- Stream analytics panel
- Networking panel (HTTP fetch)
- CSV export to `exports/records.csv`
- Automatic serialization backups every 60 sec
