# Food Delivery App

This is a Java-based Food Delivery Application, Where users order food

---

## Project Structure

- `Project/` — Contains your compiled `.class` files (optional, source preferred)
- `lib/` — Contains external libraries (e.g., `sqlite-jdbc-3.42.0.0.jar`)
- `src/` — Contains the Java source files (your `.java` files)
- `users.db` — SQLite database file

---

## Prerequisites

- Java Development Kit (JDK) installed (Java 8 or later)
- SQLite JDBC driver located in `lib/sqlite-jdbc-3.42.0.0.jar`

---

## How to Build and Run

### Using precompiled JAR

If you have the `FoodDeliveryApp.jar`:

```bash


java -cp "FoodDeliveryApp.jar:lib/sqlite-jdbc-3.42.0.0.jar" FoodDeliveryLoginUI
