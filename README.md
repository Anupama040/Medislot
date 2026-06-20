<div align="center">
  <img src="src/main/resources/static/images/medislot-logo.png" alt="MediSlot Logo" width="160" style="margin-bottom: 15px;"/>
  <h1 style="margin: 0;">MediSlot</h1>
  <p><b>Intelligent Healthcare Scheduling & Verification Platform</b></p>
</div>

---

## 📖 Overview

**MediSlot** is an enterprise-grade, comprehensive healthcare appointment scheduling application built with Java Spring Boot. Designed with a deep focus on security and trust, MediSlot ensures that patients only connect with fully verified medical professionals. 

This project was engineered as a robust academic submission, featuring role-based access control, strict administrative workflows, and a polished user experience.

## ✨ Key Features

### 🛡️ Secure Doctor Verification
MediSlot eliminates the risk of fraudulent medical accounts. Every doctor who registers must undergo a strict administrative review process. Their accounts remain completely locked until an administrator manually verifies their credentials and approves their profile.

### 👥 Multi-Role Workflows
- **Patients**: Seamlessly search for doctors by specialization or city, view real-time availability, manage bookings, and leave post-appointment reviews.
- **Doctors**: Manage professional profiles, define custom availability schedules, accept/decline appointment requests, and track patient interactions.
- **Administrators**: The central authority. Admins verify doctor applications, manage user accounts, oversee global system statistics, and handle medical specializations.

### 📅 Advanced Scheduling Engine
The platform features an intelligent slot management system that automatically prevents double-booking and dynamically updates availability in real-time based on approvals and cancellations.

## 🛠️ Technology Stack

* **Core Framework**: Java 17, Spring Boot 3.x
* **Frontend Architecture**: Thymeleaf, HTML5, CSS3
* **Data Management**: MySQL, Spring Data JPA (Hibernate)
* **Build System**: Maven
* **Security & Auth**: Session-based authentication with BCrypt hashing algorithms
* **Document Generation**: OpenPDF integration

## 🚀 Getting Started

### Prerequisites
* Java Development Kit (JDK) 17+
* MySQL Server (running on default port `3306`)
* Maven installed locally (or rely on your IDE's embedded Maven)

### Installation & Setup

1. **Database Configuration**
   First, provision the database in your MySQL environment:
   ```sql
   CREATE DATABASE medislot_db;
   ```
2. **Configure Application Properties**
   Navigate to `src/main/resources/application.properties` and ensure your database credentials match your local MySQL setup:
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/medislot_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
   spring.datasource.username=root
   spring.datasource.password=your_mysql_password
   ```

3. **Launch the Application**
   Using Maven via terminal:
   ```bash
   mvn spring-boot:run
   ```
   Or simply run the `MediSlotApplication.java` file from your preferred Java IDE.

4. **Access the Portal**
   Open your web browser and navigate to: `http://localhost:8080`

## 👑 Administrative Access

On the very first launch, the system automatically provisions a super-admin account to help you get started immediately:

> **Email**: `admin@medislot.com`  
> **Password**: `admin123`

## 🏗️ System Architecture & Status Lifecycles

### Doctor Verification Lifecycle
`Pending Application` ➔ `Admin Review` ➔ `Approved` (Login Enabled) or `Rejected/Suspended`

### Appointment Lifecycle
`Slot Available` ➔ `Patient Books (Pending)` ➔ `Doctor Approves` ➔ `Appointment Completed` ➔ `Patient Reviews`

## 💡 Academic & Future Scope

MediSlot was meticulously developed to demonstrate full-stack Java capabilities, MVC architecture, and complex business logic for academic evaluation. 

**Future Roadmap:**
- Integration of Spring Security custom filters
- Email-based OTP verification
- Cloud-based document uploads for medical licenses
- Payment gateway integration for consultation fees
- Expansion to RESTful APIs for cross-platform mobile support

---
*Architected and developed exclusively by Anupama CY.*
