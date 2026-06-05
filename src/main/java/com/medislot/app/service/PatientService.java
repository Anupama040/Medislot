package com.medislot.app.service;

import com.medislot.app.entity.Appointment;
import com.medislot.app.entity.AppointmentStatus;
import com.medislot.app.entity.DoctorProfile;
import com.medislot.app.entity.NotificationType;
import com.medislot.app.entity.Review;
import com.medislot.app.entity.SlotStatus;
import com.medislot.app.entity.TimeSlot;
import com.medislot.app.entity.User;
import com.medislot.app.entity.VerificationStatus;
import com.medislot.app.repository.AppointmentRepository;
import com.medislot.app.repository.DoctorProfileRepository;
import com.medislot.app.repository.ReviewRepository;
import com.medislot.app.repository.TimeSlotRepository;
import com.medislot.app.repository.UserRepository;
import com.medislot.app.entity.Prescription;
import com.medislot.app.repository.PrescriptionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class PatientService {

    private static final List<AppointmentStatus> BLOCKING_STATUSES = List.of(
            AppointmentStatus.PENDING,
            AppointmentStatus.APPROVED
    );

    private final DoctorProfileRepository doctorProfileRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final AppointmentRepository appointmentRepository;
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final PrescriptionRepository prescriptionRepository;

    public PatientService(
            DoctorProfileRepository doctorProfileRepository,
            TimeSlotRepository timeSlotRepository,
            AppointmentRepository appointmentRepository,
            ReviewRepository reviewRepository,
            UserRepository userRepository,
            NotificationService notificationService,
            PrescriptionRepository prescriptionRepository
    ) {
        this.doctorProfileRepository = doctorProfileRepository;
        this.timeSlotRepository = timeSlotRepository;
        this.appointmentRepository = appointmentRepository;
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
        this.prescriptionRepository = prescriptionRepository;
    }

    public List<DoctorProfile> searchDoctors(Long specializationId, String city) {
        boolean hasSpecialization = specializationId != null;
        boolean hasCity = city != null && !city.isBlank();
        if (hasSpecialization && hasCity) {
            return doctorProfileRepository.findByVerificationStatusAndSpecializationIdAndCityContainingIgnoreCase(
                    VerificationStatus.APPROVED, specializationId, city.trim());
        }
        if (hasSpecialization) {
            return doctorProfileRepository.findByVerificationStatusAndSpecializationId(
                    VerificationStatus.APPROVED, specializationId);
        }
        if (hasCity) {
            return doctorProfileRepository.findByVerificationStatusAndCityContainingIgnoreCase(
                    VerificationStatus.APPROVED, city.trim());
        }
        return doctorProfileRepository.findByVerificationStatus(VerificationStatus.APPROVED);
    }

    public List<TimeSlot> availableSlots(Long doctorUserId) {
        return timeSlotRepository.findByDoctorIdAndStatusAndSlotDateGreaterThanEqualOrderBySlotDateAscStartTimeAsc(
                doctorUserId, SlotStatus.AVAILABLE, LocalDate.now());
    }

    public List<Appointment> appointments(Long patientUserId) {
        return appointmentRepository.findByPatientIdOrderByCreatedAtDesc(patientUserId);
    }

    public List<Prescription> medicalHistory(Long patientUserId) {
        return prescriptionRepository.findByAppointmentPatientIdOrderByCreatedAtDesc(patientUserId);
    }
    
    public Prescription getPrescription(Long prescriptionId, Long patientUserId) {
        Prescription prescription = prescriptionRepository.findById(prescriptionId)
                .orElseThrow(() -> new IllegalArgumentException("Prescription not found."));
        if (!prescription.getAppointment().getPatient().getId().equals(patientUserId)) {
            throw new IllegalArgumentException("You do not have permission to view this prescription.");
        }
        return prescription;
    }

    @Transactional
    public void book(Long patientUserId, Long slotId, String reason) {
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("Please enter appointment reason.");
        }
        TimeSlot slot = timeSlotRepository.findById(slotId)
                .orElseThrow(() -> new IllegalArgumentException("Selected slot not found."));
        if (slot.getStatus() != SlotStatus.AVAILABLE
                || appointmentRepository.existsBySlotIdAndStatusIn(slotId, BLOCKING_STATUSES)) {
            throw new IllegalArgumentException("This slot is already booked.");
        }
        User patient = userRepository.findById(patientUserId)
                .orElseThrow(() -> new IllegalArgumentException("Patient not found."));
        DoctorProfile doctorProfile = doctorProfileRepository.findByUserId(slot.getDoctor().getId())
                .orElseThrow(() -> new IllegalArgumentException("Doctor profile not found."));
        if (doctorProfile.getVerificationStatus() != VerificationStatus.APPROVED) {
            throw new IllegalArgumentException("Doctor is not available for booking.");
        }

        slot.setStatus(SlotStatus.BOOKED);
        Appointment appointment = new Appointment();
        appointment.setPatient(patient);
        appointment.setDoctor(slot.getDoctor());
        appointment.setSlot(slot);
        appointment.setReason(reason.trim());
        appointment.setFee(doctorProfile.getConsultationFee());
        appointmentRepository.save(appointment);

        notificationService.create(slot.getDoctor(), "New appointment request",
                patient.getName() + " booked a slot and is waiting for your approval.",
                NotificationType.APPOINTMENT);
    }

    @Transactional
    public void cancel(Long appointmentId, Long patientUserId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found."));
        if (!appointment.getPatient().getId().equals(patientUserId)) {
            throw new IllegalArgumentException("You can only cancel your own appointment.");
        }
        if (appointment.getStatus() == AppointmentStatus.COMPLETED) {
            throw new IllegalArgumentException("Completed appointment cannot be cancelled.");
        }
        appointment.setStatus(AppointmentStatus.CANCELLED_BY_PATIENT);
        appointment.setUpdatedAt(LocalDateTime.now());
        appointment.getSlot().setStatus(SlotStatus.AVAILABLE);
    }

    @Transactional
    public void review(Long appointmentId, Long patientUserId, Integer rating, String comment) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found."));
        if (!appointment.getPatient().getId().equals(patientUserId)) {
            throw new IllegalArgumentException("You can only review your own appointment.");
        }
        if (appointment.getStatus() != AppointmentStatus.COMPLETED) {
            throw new IllegalArgumentException("Review can be added only after appointment completion.");
        }
        if (reviewRepository.existsByAppointmentId(appointmentId)) {
            throw new IllegalArgumentException("Review already submitted.");
        }
        if (rating == null || rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5.");
        }

        Review review = new Review();
        review.setAppointment(appointment);
        review.setPatient(appointment.getPatient());
        review.setDoctor(appointment.getDoctor());
        review.setRating(rating);
        review.setComment(comment);
        reviewRepository.save(review);
    }
}
