# Food Delivery App (Comp-350-Project)

A simple Java Swing-based Food Delivery Login application with SQLite database support.

---

## Project Structure
comp-350-project/
├─ .vscode/ # VS Code configuration files
│ └─ launch.json # Run configuration
├─ Project/ # MainApp project folder
│ ├─ MainApp.java # Entry point of the application
│ ├─ FoodDeliveryLoginUI.java # UI class
│ ├─ UserDataBase.java # SQLite database helper
│ ├─ sqlite-jdbc-3.42.0.0.jar # SQLite JDBC driver
│ └─ users.db # SQLite database (created automatically)
├─ .gitignore # Git ignore rules
└─ MANIFEST.MF # Optional manifest file

---

## Prerequisites

- Java JDK 17 or later installed
- [Visual Studio Code](https://code.visualstudio.com/) with **Java Extension Pack**
- SQLite JDBC driver included (`sqlite-jdbc-3.42.0.0.jar` is already in `Project/`)

---

## How to Run in VS Code

1. **Open the project folder** in VS Code:


2. **Ensure launch configuration is set** in `.vscode/launch.json`:

```json
{
  "version": "0.2.0",
  "configurations": [
    {
      "type": "java",
      "name": "Run FoodDelivery App",
      "request": "launch",
      "mainClass": "MainApp",
      "projectName": "Project",
      "classPaths": [
        "Project/sqlite-jdbc-3.42.0.0.jar"
      ]
    }
  ]
}

You should be able to run the code with no errors after!