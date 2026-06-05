package com.medislot.app.repository;

import com.medislot.app.entity.Appointment;
import com.medislot.app.entity.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByPatientIdOrderByCreatedAtDesc(Long patientId);
    List<Appointment> findByDoctorIdOrderByCreatedAtDesc(Long doctorId);
    List<Appointment> findAllByOrderByCreatedAtDesc();
    long countByStatus(AppointmentStatus status);
    long countByDoctorIdAndStatus(Long doctorId, AppointmentStatus status);
    boolean existsBySlotIdAndStatusIn(Long slotId, Collection<AppointmentStatus> statuses);
}
