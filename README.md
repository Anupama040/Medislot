# MediSlot - Smart Doctor Appointment Booking System

MediSlot is a Java Spring Boot web application for booking appointments with verified doctors. It is designed as a final-year engineering project with separate workflows for patients, doctors, and administrators.

The main idea is simple: patients should only book appointments with doctors who have been verified by an admin. A doctor can apply through the system, but the doctor account remains disabled until the admin approves the medical registration details.

## Tech Stack

- Backend: Java 17, Spring Boot
- Frontend: Thymeleaf, HTML, CSS
- Database: MySQL
- ORM: Spring Data JPA / Hibernate
- Build Tool: Maven
- Authentication: Session-based login with BCrypt password hashing

## Project Location

```text
D:\DCLprojects\medislot-smart-appointment
```

## Main Modules

### 1. Patient Module

Patients can:

- Register and login
- Search verified doctors by specialization and city
- View available doctor slots
- Book an appointment
- Cancel pending or approved appointments
- View appointment history
- Submit review after the doctor marks the appointment as completed
- View notifications

### 2. Doctor Module

Doctors can:

- Apply/register with professional details
- Wait for admin verification
- Login only after admin approval
- Generate available time slots
- View appointment requests
- Approve or reject appointment requests
- Mark approved appointments as completed
- View notifications

### 3. Admin Module

Admins can:

- Login using the default admin account
- View dashboard statistics
- Verify doctor applications
- Approve, reject, or suspend doctors
- Manage users by enabling/disabling accounts
- Add medical specializations
- View all appointments in the system

## Why Doctor Verification Exists

Doctor self-registration alone is risky because anyone could create a fake doctor account. MediSlot solves this by using a verification workflow:

```text
Doctor applies -> Admin reviews details -> Admin approves/rejects -> Doctor can login only if approved
```

Doctor verification statuses:

```text
PENDING_VERIFICATION
APPROVED
REJECTED
SUSPENDED
```

Only doctors with `APPROVED` status can create slots and receive bookings.

## Appointment Workflow

The normal booking lifecycle is:

```text
Patient books slot -> Appointment becomes PENDING
Doctor approves -> Appointment becomes APPROVED
Doctor completes -> Appointment becomes COMPLETED
Patient reviews -> Review is saved
```

Appointment statuses:

```text
PENDING
APPROVED
REJECTED
CANCELLED_BY_PATIENT
CANCELLED_BY_DOCTOR
COMPLETED
```

Slot statuses:

```text
AVAILABLE
BOOKED
BLOCKED
```

When a patient books a slot, the slot becomes `BOOKED`. If the doctor rejects the appointment or the patient cancels it, the slot becomes available again.

## Database Tables

The project uses the following main tables:

| Table | Purpose |
|---|---|
| `users` | Stores login accounts for admin, doctor, and patient |
| `doctor_profiles` | Stores doctor professional and verification details |
| `patient_profiles` | Stores patient profile information |
| `specializations` | Stores medical specialization categories |
| `time_slots` | Stores doctor-generated appointment slots |
| `appointments` | Stores patient-doctor appointment bookings |
| `reviews` | Stores patient reviews for completed appointments |
| `notifications` | Stores in-app notifications |

SQL scripts are available here:

```text
src/main/resources/sql/schema.sql
src/main/resources/sql/sample-data.sql
```

The application can also create/update tables automatically because this property is enabled:

```properties
spring.jpa.hibernate.ddl-auto=update
```

## Folder Structure

```text
medislot-smart-appointment
├── pom.xml
├── README.md
└── src
    └── main
        ├── java
        │   └── com
        │       └── medislot
        │           └── app
        │               ├── MediSlotApplication.java
        │               ├── config
        │               ├── controller
        │               ├── entity
        │               ├── repository
        │               └── service
        └── resources
            ├── application.properties
            ├── static
            │   └── css
            ├── templates
            └── sql
```

## Important Java Packages

| Package | Description |
|---|---|
| `config` | Application configuration and default data initializer |
| `controller` | MVC controllers for auth, admin, doctor, and patient routes |
| `entity` | JPA entity classes and enums |
| `repository` | Spring Data JPA repositories |
| `service` | Business logic and validation rules |

## Key Files

| File | Purpose |
|---|---|
| `MediSlotApplication.java` | Main Spring Boot startup class |
| `application.properties` | Database and server configuration |
| `DataInitializer.java` | Creates default admin and default specializations |
| `AuthController.java` | Login, logout, patient registration, doctor registration |
| `AdminController.java` | Admin dashboard, doctor verification, user and specialization management |
| `DoctorController.java` | Doctor dashboard, slots, appointment actions |
| `PatientController.java` | Patient dashboard, doctor search, booking, cancellation, review |
| `PatientService.java` | Patient booking rules and review logic |
| `DoctorService.java` | Doctor slot and appointment management |
| `AdminService.java` | Doctor approval, suspension, users, and specialization logic |

## Prerequisites

Install these before running the project:

- Java 17 or higher
- MySQL Server
- Maven, or an IDE with Maven support such as IntelliJ IDEA, Eclipse, or Spring Tool Suite

Check Java:

```bash
java -version
```

Check Maven:

```bash
mvn -version
```

If `mvn` is not recognized, install Maven or run the project directly from your IDE.

## Database Setup

Create the database in MySQL:

```sql
CREATE DATABASE medislot_db;
```

You can also run:

```text
src/main/resources/sql/schema.sql
```

## Configure Database Credentials

Open:

```text
src/main/resources/application.properties
```

Update these values according to your MySQL setup:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/medislot_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=root
```

If your MySQL password is different, replace `root` with your actual password.

## How To Run

### Option 1: Run From IDE

1. Open the project folder in IntelliJ IDEA, Eclipse, or Spring Tool Suite.
2. Wait for Maven dependencies to load.
3. Run this file:

```text
src/main/java/com/medislot/app/MediSlotApplication.java
```

4. Open the browser:

```text
http://localhost:8080
```

### Option 2: Run From Terminal

From the project root:

```bash
mvn spring-boot:run
```

Then open:

```text
http://localhost:8080
```

## Default Admin Login

The application automatically creates a default admin account on first run:

```text
Email: admin@medislot.com
Password: admin123
```

This is created by:

```text
src/main/java/com/medislot/app/config/DataInitializer.java
```

## Demo Flow For Evaluation

Use this flow when showing the project to a teacher, guide, or evaluator:

1. Start the application.
2. Login as admin:

```text
admin@medislot.com
admin123
```

3. Check default specializations from the admin panel.
4. Logout.
5. Open `Doctor Apply`.
6. Register a doctor with valid details such as registration number, qualification, clinic, city, and fee.
7. Try logging in as that doctor. Login should fail because admin approval is pending.
8. Login as admin again.
9. Go to doctor verification and approve the doctor.
10. Logout and login as doctor.
11. Generate available slots.
12. Logout and register/login as a patient.
13. Search doctor by specialization or city.
14. Select a slot and book an appointment.
15. Login as doctor and approve the appointment.
16. Mark appointment as completed.
17. Login as patient and submit a review.

This demonstrates the full real-world workflow.

## URL Map

| URL | Access | Description |
|---|---|---|
| `/` | Public | Home page |
| `/login` | Public | Login page |
| `/register/patient` | Public | Patient registration |
| `/register/doctor` | Public | Doctor application |
| `/admin/dashboard` | Admin | Admin dashboard |
| `/admin/doctors` | Admin | Doctor verification |
| `/admin/users` | Admin | User management |
| `/admin/specializations` | Admin | Specialization management |
| `/admin/appointments` | Admin | All appointments |
| `/doctor/dashboard` | Doctor | Doctor dashboard |
| `/doctor/slots` | Doctor | Slot generation and list |
| `/doctor/appointments` | Doctor | Appointment approval/completion |
| `/patient/dashboard` | Patient | Patient dashboard |
| `/patient/doctors` | Patient | Search doctors |
| `/patient/doctors/{doctorId}/slots` | Patient | View doctor slots |

## Security Notes

- Passwords are stored using BCrypt hashing.
- Login is session-based.
- Doctors cannot login until admin approval.
- Disabled users cannot login.
- Admin cannot disable their own account.
- Booking checks prevent active double booking for the same slot.

## Current Scope

This project is intentionally built as a complete academic web application, not a hospital-grade production system. It includes realistic modules and business rules while keeping the code understandable for students.

Possible future improvements:

- Spring Security URL filters
- Email OTP verification
- File upload for doctor documents
- Online payment gateway integration
- PDF prescription upload
- Advanced reports and charts
- REST API version for mobile app integration

## Troubleshooting

### Maven command not found

Install Maven or run the project from an IDE that supports Maven.

### Database connection failed

Check:

- MySQL server is running
- Database `medislot_db` exists
- Username and password in `application.properties` are correct
- MySQL is running on port `3306`

### Port 8080 already in use

Change this in `application.properties`:

```properties
server.port=8081
```

Then open:

```text
http://localhost:8081
```

### Doctor cannot login

This is expected until admin approval. Login as admin, open doctor verification, and approve the doctor profile.

## Project Summary

MediSlot is a smart appointment booking system where:

- Patients book appointments only with verified doctors
- Doctors manage slots and appointments
- Admin controls trust, verification, users, and specializations
- The system prevents active double booking
- Reviews and notifications make the workflow more complete

This makes the project suitable for a final-year engineering submission and demonstration.
