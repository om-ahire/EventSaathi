# EventSaathi – Multi-Vendor Event Planning Platform

> **Production-grade multi-vendor event planning platform** – built with Java 21, Spring Boot 3.3.5, Spring Data JPA, Spring Security + JWT, Google Gemini AI recommendation engine, and a modern Single-Page Application (SPA) frontend.

---

## 🚀 Live Working Prototype

The application is currently running and accessible at:
- **Web App**: [http://localhost:8080](http://localhost:8080)
- **REST Health Check**: [http://localhost:8080/api/v1/health](http://localhost:8080/api/v1/health)
- **Swagger UI Interactive API Docs**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **OpenAPI 3.0 JSON Specification**: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

---

## 🔑 1-Click Interactive Demo Accounts

The prototype features a persistent **Demo Switcher Bar** at the top of the interface for instantaneous role switching:

| Role | Account Name | Email | Password | Primary Demo Features |
|------|-------------|-------|----------|-----------------------|
| 👤 **Customer** | Ananya Sharma | `customer@eventsaathi.com` | `Customer@123456` | Explore vendors, plan events, book packages, make simulated UPI/Card payments, submit 5-star ratings & reviews |
| 🏢 **Vendor (Venue)** | Royal Grand Banquet | `vendor@eventsaathi.com` | `Vendor@123456` | View profile, manage 3 banquet packages, manage date availability |
| 🍽️ **Vendor (Catering)** | Saffron & Spice Gourmet | `caterer@eventsaathi.com` | `Vendor@123456` | View incoming client booking requests, accept or reject with notes |
| 📸 **Vendor (Photo)** | Drishti Cinematic Studios | `photo@eventsaathi.com` | `Vendor@123456` | View 4K cinema packages & client reviews |
| 🛡️ **Admin** | EventSaathi Admin | `admin@eventsaathi.com` | `Admin@123456` | Review pending vendor applications (`Apex Spark Events`), approve/reject vendors, inspect platform KPIs |

---

## 🌟 Core Features & Modules

### 1. Vendor Marketplace & Search
- Filter by **Category Pills** (Venue, Catering, Photography, Decoration, Music & DJ, Makeup, Transport).
- Filter by **City** (Mumbai, Delhi, Bengaluru, Hyderabad, Pune, Jaipur, Chennai, Kolkata).
- Filter by **Max Budget** and **Keywords** with instant debounce.
- Sort by **Rating**, **Price**, or **Alphabetical**.
- Vendor detail modal with photography gallery, package pricing, guest limits, and client reviews.

### 2. AI Event Planner & Recommendations
- Integrated with **Google Gemini API** (`gemini-2.0-flash`) with structured prompts.
- **Resilient Fallback**: Automatically switches to smart rule-based ranking if no API key is supplied, calculating budget fitness, guest capacity compatibility, and category scoring.
- Audit logging table (`ai_recommendation_logs`) tracking response duration and token metadata.

### 3. Customer Portal
- **Plan Events**: Set event occasion (Wedding, Corporate, Birthday, Engagement), dates, budget, and guest count.
- **Booking Management**: Book vendors with package selection and special instructions.
- **Double-Booking Guard**: Prevents vendor overlap on active dates.
- **Payment Gateway Simulation**: Authorize payments via UPI, Credit Card, or Net Banking with transaction IDs.
- **Review Engine**: Rate completed services from 1 to 5 stars with live rating recalculation.

### 4. Vendor Business Portal
- **Incoming Requests**: Review booking requests; Accept or Reject with customized client notes.
- **Service Package Management**: Create and delete tiered packages with guest capacities and hourly durations.
- **Availability Management**: Blackout dates when fully booked or unavailable.

### 5. Admin Console
- **Platform Analytics**: Total approved partners, pending applications, active bookings.
- **Verification Workflow**: Review pending vendor submissions and 1-click Approve or Reject.

---

## 🛠️ Technology Stack

| Layer | Technology | Details |
|-------|-----------|---------|
| **Language** | Java 21 | OpenJDK Temurin 25 / Java 21 LTS |
| **Framework** | Spring Boot 3.3.5 | Web, Data JPA, Security, Validation |
| **Build Tool** | Apache Maven 3.9+ | Declarative lifecycle & plugin manager |
| **Database** | MySQL 8.3 & H2 | MySQL for production/dev; H2 in-memory for JUnit |
| **ORM** | Hibernate 6.5 | 15+ normalized 3NF entities, batch fetching |
| **Security** | Spring Security + JWT | Stateless JJWT 0.12.5, BCrypt password hashing |
| **AI Integration** | Google Gemini REST | Structured JSON prompts with rule-based fallback |
| **Frontend** | Vanilla JS + Bootstrap 5 | Modern dark luxury glassmorphism SPA |
| **API Documentation** | SpringDoc OpenAPI 2.6.0 | Interactive Swagger UI at `/swagger-ui.html` |

---

## 📋 Build Roadmap Status

| Phase | Description | Status |
|-------|-------------|--------|
| **Phase 0** | Project setup, Maven parent, MySQL connection probe | ✅ Complete |
| **Phase 1** | 15+ JPA entities, 3NF normalization, Spring Data repositories | ✅ Complete |
| **Phase 2** | Spring Security, JJWT authentication filter, BCrypt, role RBAC | ✅ Complete |
| **Phase 3** | Vendor profile management, search specifications, admin approvals | ✅ Complete |
| **Phase 4** | Events, bookings lifecycle, double-booking guard, simulated payments | ✅ Complete |
| **Phase 5** | AI recommendations with Gemini + smart rule-based fallback & audit logs | ✅ Complete |
| **Phase 6** | Interactive Single-Page Application (SPA) with 1-click demo switcher | ✅ Complete |
| **Phase 7** | Swagger UI documentation, JUnit 5 + Mockito smoke & integration tests | ✅ Complete |

---

## 💻 Running Locally

### Prerequisites
- Java 21+ (`java -version`)
- MySQL 8 (`net start mysql` or port 3306 active)

### 1. Ensure MySQL Database Exists
```sql
CREATE DATABASE IF NOT EXISTS eventsaathi_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 2. Run Application
```powershell
mvn spring-boot:run
```

### 3. Run Automated Tests
```powershell
mvn test
```
All smoke and application context tests run against an in-memory H2 database.
