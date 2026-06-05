USE medislot_db;

INSERT INTO specializations (name, description, active) VALUES
('Cardiology', 'Heart specialist consultation', 1),
('Dermatology', 'Skin and hair consultation', 1),
('Orthopedics', 'Bone and joint consultation', 1),
('General Physician', 'General health consultation', 1),
('Dentist', 'Dental consultation and care', 1);

-- The application automatically creates this admin on first run:
-- Email: admin@medislot.com
-- Password: admin123
--
-- For demo patients/doctors, use the registration pages. This keeps passwords BCrypt-hashed
-- by the application and keeps doctor verification realistic.