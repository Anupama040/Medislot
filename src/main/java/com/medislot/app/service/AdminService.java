package com.medislot.app.service;

import com.medislot.app.entity.DoctorProfile;
import com.medislot.app.entity.NotificationType;
import com.medislot.app.entity.Role;
import com.medislot.app.entity.Specialization;
import com.medislot.app.entity.User;
import com.medislot.app.entity.VerificationStatus;
import com.medislot.app.repository.AppointmentRepository;
import com.medislot.app.repository.DoctorProfileRepository;
import com.medislot.app.repository.SpecializationRepository;
import com.medislot.app.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final DoctorProfileRepository doctorProfileRepository;
    private final SpecializationRepository specializationRepository;
    private final AppointmentRepository appointmentRepository;
    private final NotificationService notificationService;
    private final DoctorService doctorService;

    public AdminService(
            UserRepository userRepository,
            DoctorProfileRepository doctorProfileRepository,
            SpecializationRepository specializationRepository,
            AppointmentRepository appointmentRepository,
            NotificationService notificationService,
            DoctorService doctorService
    ) {
        this.userRepository = userRepository;
        this.doctorProfileRepository = doctorProfileRepository;
        this.specializationRepository = specializationRepository;
        this.appointmentRepository = appointmentRepository;
        this.notificationService = notificationService;
        this.doctorService = doctorService;
    }

    public List<User> users() {
        return userRepository.findAll();
    }

    public List<DoctorProfile> pendingDoctors() {
        return doctorProfileRepository.findByVerificationStatusOrderByCreatedAtDesc(VerificationStatus.PENDING_VERIFICATION);
    }

    public List<DoctorProfile> allDoctors() {
        return doctorProfileRepository.findAll();
    }

    public List<Specialization> specializations() {
        return specializationRepository.findAllByOrderByNameAsc();
    }

    @Transactional
    public void approveDoctor(Long profileId, String remark) {
        DoctorProfile profile = doctorProfileRepository.findById(profileId)
                .orElseThrow(() -> new IllegalArgumentException("Doctor profile not found."));
        profile.setVerificationStatus(VerificationStatus.APPROVED);
        profile.setAdminRemark(remark);
        profile.setUpdatedAt(LocalDateTime.now());
        profile.getUser().setEnabled(true);
        notificationService.create(profile.getUser(), "Doctor profile approved",
                "Your doctor account is verified. You can now create slots and manage appointments.",
                NotificationType.DOCTOR_VERIFICATION);
                
        // Auto generate slots using AI upon approval
        try {
            doctorService.autoGenerateSlots(profile.getUser().getId());
        } catch (Exception e) {
            System.err.println("Failed to auto-generate slots: " + e.getMessage());
        }
    }

    @Transactional
    public void rejectDoctor(Long profileId, String remark) {
        DoctorProfile profile = doctorProfileRepository.findById(profileId)
                .orElseThrow(() -> new IllegalArgumentException("Doctor profile not found."));
        profile.setVerificationStatus(VerificationStatus.REJECTED);
        profile.setAdminRemark(remark);
        profile.setUpdatedAt(LocalDateTime.now());
        profile.getUser().setEnabled(false);
    }

    @Transactional
    public void suspendDoctor(Long profileId, String remark) {
        DoctorProfile profile = doctorProfileRepository.findById(profileId)
                .orElseThrow(() -> new IllegalArgumentException("Doctor profile not found."));
        profile.setVerificationStatus(VerificationStatus.SUSPENDED);
        profile.setAdminRemark(remark);
        profile.setUpdatedAt(LocalDateTime.now());
        profile.getUser().setEnabled(false);
    }

    @Transactional
    public void saveSpecialization(String name, String description) {
        if (specializationRepository.existsByNameIgnoreCase(name.trim())) {
            throw new IllegalArgumentException("Specialization already exists.");
        }
        Specialization specialization = new Specialization();
        specialization.setName(name.trim());
        specialization.setDescription(description);
        specializationRepository.save(specialization);
    }

    @Transactional
    public void toggleUser(Long userId, Long currentAdminId) {
        if (userId.equals(currentAdminId)) {
            throw new IllegalArgumentException("You cannot disable your own account.");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
        if (user.getRole() == Role.ADMIN && userRepository.countByRole(Role.ADMIN) <= 1) {
            throw new IllegalArgumentException("At least one admin must remain.");
        }
        user.setEnabled(!user.isEnabled());
    }

    public long totalAppointments() {
        return appointmentRepository.count();
    }
}
