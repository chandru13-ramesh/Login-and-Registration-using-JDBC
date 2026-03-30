# Login and Registration System (JDBC)

## Overview

This project is a Java-based user authentication system that provides both login and registration functionality using JDBC and a MySQL database. It demonstrates a structured approach to building a secure and modular authentication system, incorporating input validation, password hashing, and layered architecture.

The application includes both backend logic and a basic web interface, making it suitable as an intermediate-level project for understanding full-stack integration using core Java technologies.

---

## Features

* User registration with validation
* Secure login authentication
* Password hashing using BCrypt
* Duplicate username and email prevention
* Input validation (username, email, password strength)
* Layered architecture (DAO, Service, Model, Utility)
* Basic web interface for interaction
* JDBC-based database connectivity

---

## Technologies Used

* Java (JDK 8 or higher)
* JDBC (Java Database Connectivity)
* MySQL Database
* HTML (for frontend interface)

---

## Project Structure

```
Login and Registration (JDBC)/
│
├── src/
│   ├── app/
│   │   └── Main.java
│   ├── config/
│   │   └── DBConnection.java
│   ├── dao/
│   │   └── UserDAO.java
│   ├── model/
│   │   └── User.java
│   ├── service/
│   │   └── AuthService.java
│   ├── util/
│   │   ├── PasswordUtil.java
│   │   └── ValidationUtil.java
│   └── web/
│       └── WebServer.java
│
├── web/
│   └── index.html
│
└── README.md
```

---

## Setup Instructions

### 1. Prerequisites

Ensure the following are installed:

* Java Development Kit (JDK 8 or higher)
* MySQL Server
* Any Java IDE (e.g., Eclipse, IntelliJ IDEA)

---

### 2. Database Setup

Execute the following SQL commands:

```sql
CREATE DATABASE secure_user_db;

USE secure_user_db;

CREATE TABLE users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE,
    email VARCHAR(100) UNIQUE,
    password VARCHAR(255)
);
```

---

### 3. Configure Database Connection

Update the database credentials in `DBConnection.java`:

```java
private static final String URL = "jdbc:mysql://localhost:3306/secure_user_db?useSSL=false&serverTimezone=UTC";
private static final String USERNAME = "root";
private static final String PASSWORD = "yourpassword";
```

---

### 4. Add Dependencies

Ensure the following libraries are added to the project:

* MySQL Connector/J
* BCrypt library (for password hashing)

Add them to your project build path using your IDE.

---

### 5. Run the Application

* Run `Main.java` for console-based interaction
* Run `WebServer.java` to start the web interface
* Open `index.html` in a browser to interact with the system

---

## Application Flow

1. The user submits registration or login details
2. Input data is validated using utility classes
3. Passwords are securely hashed before storage
4. DAO layer interacts with the database via JDBC
5. Service layer manages authentication logic
6. The system returns success or failure responses

---

## Security Considerations

* Passwords are hashed using BCrypt before storage
* Duplicate user checks are enforced at both application and database levels
* Input validation reduces the risk of invalid or malicious data

---

## Limitations

* Database credentials are hardcoded
* No session management or token-based authentication
* Basic frontend without advanced UI/UX
* Not deployed as a production-grade web application

---

## Future Enhancements

* Externalize configuration using properties or environment variables
* Implement session handling or JWT-based authentication
* Improve frontend using modern frameworks
* Introduce logging and monitoring
* Migrate to a framework such as Spring Boot
* Implement role-based access control

---

## Author

This project was developed as part of learning Java, JDBC, and authentication system design.

---

## License

This project is intended for educational and demonstration purposes.
