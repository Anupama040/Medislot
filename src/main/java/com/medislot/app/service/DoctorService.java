package com.medislot.app.service;

import com.medislot.app.entity.Appointment;
import com.medislot.app.entity.AppointmentStatus;
import com.medislot.app.entity.DoctorProfile;
import com.medislot.app.entity.NotificationType;
import com.medislot.app.entity.SlotStatus;
import com.medislot.app.entity.TimeSlot;
import com.medislot.app.entity.User;
import com.medislot.app.entity.VerificationStatus;
import com.medislot.app.repository.AppointmentRepository;
import com.medislot.app.repository.DoctorProfileRepository;
import com.medislot.app.repository.TimeSlotRepository;
import com.medislot.app.repository.UserRepository;
import com.medislot.app.entity.Prescription;
import com.medislot.app.repository.PrescriptionRepository;
import com.medislot.app.service.GeminiService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class DoctorService {

    private final UserRepository userRepository;
    private final DoctorProfileRepository doctorProfileRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final AppointmentRepository appointmentRepository;
    private final NotificationService notificationService;
    private final PrescriptionRepository prescriptionRepository;
    private final GeminiService geminiService;

    public DoctorService(
            UserRepository userRepository,
            DoctorProfileRepository doctorProfileRepository,
            TimeSlotRepository timeSlotRepository,
            AppointmentRepository appointmentRepository,
            NotificationService notificationService,
            PrescriptionRepository prescriptionRepository,
            GeminiService geminiService
    ) {
        this.userRepository = userRepository;
        this.doctorProfileRepository = doctorProfileRepository;
        this.timeSlotRepository = timeSlotRepository;
        this.appointmentRepository = appointmentRepository;
        this.notificationService = notificationService;
        this.prescriptionRepository = prescriptionRepository;
        this.geminiService = geminiService;
    }

    public DoctorProfile profile(Long doctorUserId) {
        return doctorProfileRepository.findByUserId(doctorUserId)
                .orElseThrow(() -> new IllegalArgumentException("Doctor profile not found."));
    }

    public List<TimeSlot> slots(Long doctorUserId) {
        return timeSlotRepository.findByDoctorIdOrderBySlotDateAscStartTimeAsc(doctorUserId);
    }

    public List<Appointment> appointments(Long doctorUserId) {
        return appointmentRepository.findByDoctorIdOrderByCreatedAtDesc(doctorUserId);
    }

    @Transactional
    public void createSlots(Long doctorUserId, LocalDate date, LocalTime start, LocalTime end, int durationMinutes) {
        DoctorProfile profile = profile(doctorUserId);
        if (profile.getVerificationStatus() != VerificationStatus.APPROVED) {
            throw new IllegalArgumentException("Only approved doctors can create slots.");
        }
        if (date == null || start == null || end == null || !end.isAfter(start)) {
            throw new IllegalArgumentException("Please enter a valid date and time range.");
        }
        if (date.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Slot date cannot be in the past.");
        }
        if (durationMinutes < 10 || durationMinutes > 120) {
            throw new IllegalArgumentException("Slot duration must be between 10 and 120 minutes.");
        }

        User doctor = userRepository.findById(doctorUserId)
                .orElseThrow(() -> new IllegalArgumentException("Doctor not found."));
        LocalTime cursor = start;
        while (!cursor.plusMinutes(durationMinutes).isAfter(end)) {
            LocalTime slotEnd = cursor.plusMinutes(durationMinutes);
            
            // Skip lunch break between 13:00 (1:00 PM) and 14:00 (2:00 PM)
            if (!(cursor.isBefore(LocalTime.of(14, 0)) && slotEnd.isAfter(LocalTime.of(13, 0)))) {
                if (!timeSlotRepository.existsByDoctorIdAndSlotDateAndStartTime(doctorUserId, date, cursor)) {
                    TimeSlot slot = new TimeSlot();
                    slot.setDoctor(doctor);
                    slot.setSlotDate(date);
                    slot.setStartTime(cursor);
                    slot.setEndTime(slotEnd);
                    slot.setStatus(SlotStatus.AVAILABLE);
                    timeSlotRepository.save(slot);
                }
            }
            // Add a 5 minute relief gap between every appointment
            cursor = slotEnd.plusMinutes(5);
        }
    }
    
    @Transactional
    public void deleteSlot(Long slotId, Long doctorUserId) {
        TimeSlot slot = timeSlotRepository.findById(slotId)
                .orElseThrow(() -> new IllegalArgumentException("Slot not found."));
        if (!slot.getDoctor().getId().equals(doctorUserId)) {
            throw new IllegalArgumentException("This slot does not belong to you.");
        }
        if (slot.getStatus() != SlotStatus.AVAILABLE) {
            throw new IllegalArgumentException("Only available slots can be deleted.");
        }
        timeSlotRepository.delete(slot);
    }
    
    @Transactional
    public void autoGenerateSlots(Long doctorUserId) {
        DoctorProfile profile = profile(doctorUserId);
        if (profile.getVerificationStatus() != VerificationStatus.APPROVED) {
            throw new IllegalArgumentException("Only approved doctors can auto-generate slots.");
        }
        
        String schedule = geminiService.suggestWorkingHours(profile.getSpecialization().getName());
        String[] parts = schedule.split("-");
        LocalTime start = LocalTime.parse(parts[0]);
        LocalTime end = LocalTime.parse(parts[1]);
        
        LocalDate startDate = LocalDate.now().plusDays(1);
        for (int i = 0; i < 7; i++) {
            LocalDate currentDate = startDate.plusDays(i);
            // Skip Sundays (DayOfWeek.SUNDAY is 7)
            if (currentDate.getDayOfWeek().getValue() == 7) {
                continue;
            }
            createSlots(doctorUserId, currentDate, start, end, 30);
        }
    }

    @Transactional
    public void approveAppointment(Long appointmentId, Long doctorUserId) {
        Appointment appointment = getDoctorAppointment(appointmentId, doctorUserId);
        if (appointment.getStatus() != AppointmentStatus.PENDING) {
            throw new IllegalArgumentException("Only pending appointments can be approved.");
        }
        appointment.setStatus(AppointmentStatus.APPROVED);
        appointment.setUpdatedAt(LocalDateTime.now());
        notificationService.create(appointment.getPatient(), "Appointment approved",
                "Dr. " + appointment.getDoctor().getName() + " approved your appointment.",
                NotificationType.APPOINTMENT);
    }

    @Transactional
    public void rejectAppointment(Long appointmentId, Long doctorUserId) {
        Appointment appointment = getDoctorAppointment(appointmentId, doctorUserId);
        if (appointment.getStatus() != AppointmentStatus.PENDING) {
            throw new IllegalArgumentException("Only pending appointments can be rejected.");
        }
        appointment.setStatus(AppointmentStatus.REJECTED);
        appointment.setUpdatedAt(LocalDateTime.now());
        appointment.getSlot().setStatus(SlotStatus.AVAILABLE);
        notificationService.create(appointment.getPatient(), "Appointment rejected",
                "Dr. " + appointment.getDoctor().getName() + " rejected your appointment.",
                NotificationType.APPOINTMENT);
    }

    @Transactional
    public void completeAppointment(Long appointmentId, Long doctorUserId, String diagnosis, String medicines, String notes, boolean useAi) {
        Appointment appointment = getDoctorAppointment(appointmentId, doctorUserId);
        if (appointment.getStatus() != AppointmentStatus.APPROVED) {
            throw new IllegalArgumentException("Only approved appointments can be completed.");
        }
        appointment.setStatus(AppointmentStatus.COMPLETED);
        appointment.setUpdatedAt(LocalDateTime.now());
        
        Prescription prescription = new Prescription();
        prescription.setAppointment(appointment);
        prescription.setDiagnosis(diagnosis);
        prescription.setNotes(notes);
        
        if (useAi) {
            String rawNotes = "Diagnosis: " + diagnosis + "\nMedicines: " + medicines + "\nAdditional Notes: " + notes;
            String enhancedMedicines = geminiService.enhancePrescription(rawNotes);
            prescription.setMedicines(enhancedMedicines);
        } else {
            prescription.setMedicines(medicines);
        }
        
        prescriptionRepository.save(prescription);
    }
    
    public String generateAiSummary(Long appointmentId, Long doctorUserId) {
        Appointment appointment = getDoctorAppointment(appointmentId, doctorUserId);
        Long patientId = appointment.getPatient().getId();
        
        List<Prescription> pastPrescriptions = prescriptionRepository.findByAppointmentPatientIdOrderByCreatedAtDesc(patientId);
        if (pastPrescriptions.isEmpty()) {
            return "No previous medical history found for this patient.";
        }
        
        StringBuilder historyText = new StringBuilder();
        for (Prescription p : pastPrescriptions) {
            historyText.append("Date: ").append(p.getCreatedAt().toLocalDate()).append("\n");
            historyText.append("Diagnosis: ").append(p.getDiagnosis()).append("\n");
            historyText.append("Medicines: ").append(p.getMedicines()).append("\n\n");
        }
        
        return geminiService.summarizeMedicalHistory(historyText.toString());
    }
    
    public Prescription getPrescriptionByAppointment(Long appointmentId, Long doctorUserId) {
        Appointment appointment = getDoctorAppointment(appointmentId, doctorUserId);
        return prescriptionRepository.findByAppointment(appointment)
                .orElseThrow(() -> new IllegalArgumentException("No prescription found for this appointment."));
    }

    private Appointment getDoctorAppointment(Long appointmentId, Long doctorUserId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found."));
        if (!appointment.getDoctor().getId().equals(doctorUserId)) {
            throw new IllegalArgumentException("This appointment does not belong to you.");
        }
        return appointment;
    }
}
