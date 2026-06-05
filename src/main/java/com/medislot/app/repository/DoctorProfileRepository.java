package com.medislot.app.repository;

import com.medislot.app.entity.DoctorProfile;
import com.medislot.app.entity.VerificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DoctorProfileRepository extends JpaRepository<DoctorProfile, Long> {
    Optional<DoctorProfile> findByUserId(Long userId);
    boolean existsByRegistrationNumberIgnoreCase(String registrationNumber);
    List<DoctorProfile> findByVerificationStatusOrderByCreatedAtDesc(VerificationStatus status);
    List<DoctorProfile> findByVerificationStatusAndSpecializationIdAndCityContainingIgnoreCase(
            VerificationStatus status,
            Long specializationId,
            String city
    );
    List<DoctorProfile> findByVerificationStatusAndCityContainingIgnoreCase(VerificationStatus status, String city);
    List<DoctorProfile> findByVerificationStatusAndSpecializationId(VerificationStatus status, Long specializationId);
    List<DoctorProfile> findByVerificationStatus(VerificationStatus status);
}
