# EventSaathi — Event Planning & Vendor Booking Platform

<p align="center">
  <strong>Plan smarter. Book better. Celebrate more.</strong>
</p>

<p align="center">
  A full-stack event management platform that connects customers with event service providers through vendor discovery, event planning, booking management, and AI-assisted recommendations.
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Java-21-ED8B00?style=flat-square&logo=openjdk&logoColor=white" alt="Java 21" />
  <img src="https://img.shields.io/badge/Spring%20Boot-3.3.5-6DB33F?style=flat-square&logo=springboot&logoColor=white" alt="Spring Boot" />
  <img src="https://img.shields.io/badge/MySQL-Database-4479A1?style=flat-square&logo=mysql&logoColor=white" alt="MySQL" />
  <img src="https://img.shields.io/badge/Maven-Build%20Tool-C71A36?style=flat-square&logo=apachemaven&logoColor=white" alt="Maven" />
  <img src="https://img.shields.io/badge/License-Not%20Specified-lightgrey?style=flat-square" alt="License not specified" />
</p>

<p align="center">
  <a href="https://github.com/om-ahire/EventSaathi">Repository</a> ·
  <a href="https://github.com/om-ahire/EventSaathi/issues">Report an Issue</a>
</p>

---

## Overview

Organizing an event involves coordinating venues, caterers, photographers, decorators, budgets, schedules, and bookings. Managing these moving parts across multiple platforms can quickly become complicated.

**EventSaathi** brings these activities together in one application. It provides a centralized platform where customers can discover vendors, organize events, manage bookings, and explore recommendations based on their requirements.

The project combines a Java Spring Boot backend, a MySQL database, a browser-based frontend, and role-based application functionality.

> **Project status:** Working local prototype. A publicly accessible production deployment has not been specified.

## Features

### Vendor Marketplace

- Browse vendors across venues, catering, photography, decoration, music, makeup, and transportation.
- Filter listings by category, city, budget, and keywords.
- Sort results by rating, price, or name.
- View vendor details, service packages, capacity, pricing, and reviews.

### Event Planning

- Create and manage events for weddings, birthdays, engagements, and corporate occasions.
- Configure event dates, budgets, guest counts, and other requirements.
- Organize vendor selections and event-related information.

### Booking Management

- Submit booking requests and manage their status.
- Allow vendors to review, accept, or reject incoming requests.
- Manage service packages and availability.
- Include safeguards against overlapping active bookings for the same vendor.

### AI-Assisted Recommendations

- Integrate Google's Gemini API for event and vendor recommendations.
- Use structured prompts to generate recommendation results.
- Provide rule-based fallback recommendations when an AI API key is unavailable.
- Record recommendation activity for auditing and analysis.

### Customer Experience

- Explore vendors and their packages.
- Manage event bookings and special instructions.
- Simulate payments using UPI, cards, or net banking.
- Submit ratings and reviews for completed services.

### Vendor Portal

- Review incoming booking requests.
- Manage service packages and availability.
- Respond to customer requests.

### Admin Console

- Review vendor applications.
- Approve or reject vendor submissions.
- View platform statistics, including partner and booking information.

### Authentication and Security

- Spring Security integration.
- JWT-based authentication.
- Password hashing using BCrypt.
- Role-based access control.
- Request validation and protected application endpoints.

*Feature availability depends on the local application configuration and implemented workflows.*

## Technology Stack

| Layer | Technology |
|---|---|
| Programming language | Java 21 |
| Backend framework | Spring Boot 3.3.5 |
| Web and REST APIs | Spring Web |
| Persistence | Spring Data JPA |
| ORM | Hibernate |
| Database | MySQL 8 |
| Authentication | Spring Security, JWT |
| Password hashing | BCrypt |
| AI integration | Google Gemini API |
| Frontend | HTML, CSS, JavaScript, Bootstrap 5 |
| API documentation | SpringDoc OpenAPI / Swagger UI |
| Build tool | Apache Maven |
| Testing | JUnit 5, Mockito, Spring Boot Test |
| Test database | H2 |

## Architecture

EventSaathi follows a layered application architecture.

```text
                   User / Browser
                         |
                         v
               Frontend (HTML/CSS/JS)
                         |
                         v
                 Spring Boot Server
                         |
             +-----------+-----------+
             |           |           |
             v           v           v
          REST API    Security    Validation
             |
             v
        Service Layer
             |
             v
       Spring Data JPA
             |
             v
          MySQL DB

      External Integration
             |
             v
       Google Gemini API
       (with rule-based fallback)
```

The backend separates request handling, business logic, data access, authentication, and persistence into distinct responsibilities.

## Project Structure

```text
EventSaathi/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/eventsaathi/
│   │   │       ├── config/
│   │   │       ├── controller/
│   │   │       ├── service/
│   │   │       ├── repository/
│   │   │       ├── entity/
│   │   │       └── security/
│   │   └── resources/
│   │       ├── static/
│   │       └── application.properties
│   └── test/
├── db/
├── pom.xml
├── .gitignore
└── README.md
```

*The tree illustrates the intended organization; exact package and file names may differ from the repository.*

## Getting Started

Follow these instructions to run the project locally on Windows.

### Prerequisites

Install the following:

- JDK 21
- Apache Maven 3.9 or later
- MySQL 8
- Git
- Visual Studio Code or another Java IDE

Verify Java and Maven:

```bash
java -version
javac -version
mvn -version
```

### 1. Clone the Repository

```bash
git clone https://github.com/om-ahire/EventSaathi.git
cd EventSaathi
```

### 2. Create the Database

Start your MySQL server and execute:

```sql
CREATE DATABASE IF NOT EXISTS eventsaathi_db
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;
```

### 3. Configure Database Credentials

Open `src/main/resources/application.properties`.

Configure the database URL, username, and password for your local MySQL installation. For example:

```properties
spring.datasource.url=${DB_URL:jdbc:mysql://localhost:3306/eventsaathi_db}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
```

These are example configuration entries. Integrate them with the existing properties in your project rather than blindly replacing the entire file.

Set `DB_USERNAME` and `DB_PASSWORD` in your local environment. Do not commit real credentials to GitHub.

If your project already uses different property names, preserve those names and use the matching environment variables.

### 4. Configure AI Integration (Optional)

If AI recommendations are enabled, provide a valid Gemini API key using a server-side environment variable.

Never place a private API key in frontend JavaScript or commit it to the repository.

The application documents a rule-based recommendation fallback when an AI API key is unavailable.

### 5. Run the Application

From the project root, execute:

```bash
mvn spring-boot:run
```

Wait for Spring Boot to finish starting and confirm that the terminal reports that Tomcat has started.

The default local address is:

**http://localhost:8080**

The application includes a static frontend at the root path.

### 6. Explore API Documentation

When the corresponding endpoints are enabled, open:

- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI specification: http://localhost:8080/v3/api-docs
- Health endpoint: http://localhost:8080/api/v1/health

These addresses refer to your local application. They are not public deployment URLs.

### 7. Run Tests

Execute:

```bash
mvn test
```

The project includes Spring Boot testing dependencies and H2 for tests. Confirm that the tests pass in your local environment.

## Security Notes

- Keep database credentials, JWT signing secrets, and API keys outside version control.
- Use environment variables or a secret manager for sensitive configuration.
- Do not publish production credentials or real user data.
- Use BCrypt for password hashing and validate access permissions on protected endpoints.
- Payment functionality described in this project is a simulation; it should not be treated as a real payment integration.
- Demo credentials, if used, should be disposable local-development credentials and must never be reused in production.

## Current Scope and Future Improvements

Potential next steps for the project include:

- Deploying the frontend and backend to a public hosting platform.
- Adding screenshots, a live demo, and a short product walkthrough.
- Improving automated test coverage for booking, authentication, and authorization workflows.
- Adding CI checks for builds, tests, and dependency security.
- Improving environment-specific configuration and deployment documentation.
- Integrating a real payment gateway only with the appropriate security and compliance controls.

## Learning Outcomes

This project provides practical experience with:

- Building REST APIs using Java and Spring Boot.
- Designing persistent data models with JPA and MySQL.
- Implementing authentication and authorization.
- Structuring a layered backend application.
- Integrating an external AI service with a fallback strategy.
- Managing vendor listings, bookings, and application workflows.
- Testing and documenting a full-stack application.

## Author

**Om Ahire**

Full-Stack Developer | Java | Spring Boot | MySQL

- GitHub: [@om-ahire](https://github.com/om-ahire)
- Project: [EventSaathi](https://github.com/om-ahire/EventSaathi)

---

<p align="center">
  <strong>EventSaathi — Making event planning more organized, connected, and accessible.</strong>
</p>
