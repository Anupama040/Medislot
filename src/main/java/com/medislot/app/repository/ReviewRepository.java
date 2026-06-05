package com.medislot.app.repository;

import com.medislot.app.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    boolean existsByAppointmentId(Long appointmentId);
    List<Review> findByDoctorIdOrderByCreatedAtDesc(Long doctorId);
}
