CREATE DATABASE IF NOT EXISTS medislot_db;
USE medislot_db;

DROP TABLE IF EXISTS reviews;
DROP TABLE IF EXISTS notifications;
DROP TABLE IF EXISTS appointments;
DROP TABLE IF EXISTS time_slots;
DROP TABLE IF EXISTS patient_profiles;
DROP TABLE IF EXISTS doctor_profiles;
DROP TABLE IF EXISTS specializations;
DROP TABLE IF EXISTS users;

CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    phone VARCHAR(15),
    role VARCHAR(20) NOT NULL,
    enabled BIT NOT NULL,
    created_at DATETIME NOT NULL
);

CREATE TABLE specializations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(500),
    active BIT NOT NULL
);

CREATE TABLE doctor_profiles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    specialization_id BIGINT NOT NULL,
    qualification VARCHAR(120) NOT NULL,
    registration_number VARCHAR(80) NOT NULL UNIQUE,
    experience_years INT NOT NULL,
    clinic_name VARCHAR(120) NOT NULL,
    clinic_address VARCHAR(255) NOT NULL,
    city VARCHAR(80) NOT NULL,
    consultation_fee DECIMAL(10,2) NOT NULL,
    bio VARCHAR(500),
    verification_status VARCHAR(30) NOT NULL,
    admin_remark VARCHAR(500),
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    CONSTRAINT fk_doctor_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_doctor_specialization FOREIGN KEY (specialization_id) REFERENCES specializations(id)
);

CREATE TABLE patient_profiles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    age INT,
    gender VARCHAR(20),
    city VARCHAR(100),
    address VARCHAR(255),
    medical_history VARCHAR(1000),
    CONSTRAINT fk_patient_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE time_slots (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    doctor_id BIGINT NOT NULL,
    slot_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    status VARCHAR(20) NOT NULL,
    CONSTRAINT fk_slot_doctor FOREIGN KEY (doctor_id) REFERENCES users(id),
    UNIQUE KEY uq_doctor_slot (doctor_id, slot_date, start_time)
);

CREATE TABLE appointments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_id BIGINT NOT NULL,
    doctor_id BIGINT NOT NULL,
    slot_id BIGINT NOT NULL UNIQUE,
    reason VARCHAR(500) NOT NULL,
    status VARCHAR(30) NOT NULL,
    payment_status VARCHAR(20) NOT NULL,
    fee DECIMAL(10,2) NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    CONSTRAINT fk_appt_patient FOREIGN KEY (patient_id) REFERENCES users(id),
    CONSTRAINT fk_appt_doctor FOREIGN KEY (doctor_id) REFERENCES users(id),
    CONSTRAINT fk_appt_slot FOREIGN KEY (slot_id) REFERENCES time_slots(id)
);

CREATE TABLE reviews (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    appointment_id BIGINT NOT NULL UNIQUE,
    patient_id BIGINT NOT NULL,
    doctor_id BIGINT NOT NULL,
    rating INT NOT NULL,
    comment VARCHAR(500),
    created_at DATETIME NOT NULL,
    CONSTRAINT fk_review_appt FOREIGN KEY (appointment_id) REFERENCES appointments(id),
    CONSTRAINT fk_review_patient FOREIGN KEY (patient_id) REFERENCES users(id),
    CONSTRAINT fk_review_doctor FOREIGN KEY (doctor_id) REFERENCES users(id)
);

CREATE TABLE notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(120) NOT NULL,
    message VARCHAR(700) NOT NULL,
    type VARCHAR(30) NOT NULL,
    read_status BIT NOT NULL,
    created_at DATETIME NOT NULL,
    CONSTRAINT fk_notification_user FOREIGN KEY (user_id) REFERENCES users(id)
);
