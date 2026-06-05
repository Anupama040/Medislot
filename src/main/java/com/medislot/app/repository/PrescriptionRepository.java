package com.medislot.app.repository;

import com.medislot.app.entity.Prescription;
import com.medislot.app.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {
    Optional<Prescription> findByAppointment(Appointment appointment);
    
    // Find all prescriptions for a specific patient, ordered by creation date
    List<Prescription> findByAppointmentPatientIdOrderByCreatedAtDesc(Long patientId);
}
