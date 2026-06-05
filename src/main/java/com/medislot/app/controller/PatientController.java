package com.medislot.app.controller;

import com.medislot.app.entity.Role;
import com.medislot.app.repository.SpecializationRepository;
import com.medislot.app.service.NotificationService;
import com.medislot.app.service.PatientService;
import com.medislot.app.service.GeminiService;
import com.medislot.app.entity.Specialization;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import com.medislot.app.service.PdfService;
import com.medislot.app.entity.Prescription;

@Controller
@RequestMapping("/patient")
public class PatientController {

    private final PatientService patientService;
    private final SpecializationRepository specializationRepository;
    private final NotificationService notificationService;
    private final GeminiService geminiService;
    private final PdfService pdfService;

    public PatientController(
            PatientService patientService,
            SpecializationRepository specializationRepository,
            NotificationService notificationService,
            GeminiService geminiService,
            PdfService pdfService
    ) {
        this.patientService = patientService;
        this.specializationRepository = specializationRepository;
        this.notificationService = notificationService;
        this.geminiService = geminiService;
        this.pdfService = pdfService;
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        Long patientId = requirePatient(session);
        if (patientId == null) return "redirect:/login";
        model.addAttribute("name", session.getAttribute("name"));
        model.addAttribute("appointments", patientService.appointments(patientId));
        model.addAttribute("notifications", notificationService.forUser(patientId));
        return "patient-dashboard";
    }

    @GetMapping("/history")
    public String history(HttpSession session, Model model) {
        Long patientId = requirePatient(session);
        if (patientId == null) return "redirect:/login";
        model.addAttribute("prescriptions", patientService.medicalHistory(patientId));
        return "patient-history";
    }
    
    @GetMapping("/history/{id}/pdf")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable Long id, HttpSession session) {
        Long patientId = requirePatient(session);
        if (patientId == null) return ResponseEntity.status(401).build();
        
        try {
            Prescription prescription = patientService.getPrescription(id, patientId);
            byte[] pdfBytes = pdfService.generatePrescriptionPdf(prescription);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "Prescription_" + id + ".pdf");
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/doctors")
    public String doctors(
            @RequestParam(required = false) Long specializationId,
            @RequestParam(required = false) String city,
            HttpSession session,
            Model model
    ) {
        if (requirePatient(session) == null) return "redirect:/login";
        model.addAttribute("specializations", specializationRepository.findByActiveTrueOrderByNameAsc());
        model.addAttribute("doctors", patientService.searchDoctors(specializationId, city));
        model.addAttribute("selectedSpecialization", specializationId);
        model.addAttribute("city", city);
        return "patient-doctors";
    }

    @PostMapping("/ai-search")
    public String aiSearch(@RequestParam String symptoms, HttpSession session, Model model) {
        if (requirePatient(session) == null) return "redirect:/login";
        
        List<Specialization> specs = specializationRepository.findByActiveTrueOrderByNameAsc();
        List<String> specNames = specs.stream().map(Specialization::getName).collect(Collectors.toList());
        
        String recommendedSpec = geminiService.analyzeSymptoms(symptoms, specNames);
        
        // Find matching specialization ID
        Long matchedId = specs.stream()
                .filter(s -> s.getName().equalsIgnoreCase(recommendedSpec))
                .map(Specialization::getId)
                .findFirst()
                .orElse(null);
                
        if (matchedId != null) {
            return "redirect:/patient/doctors?specializationId=" + matchedId + "&aiMatched=" + recommendedSpec;
        } else {
            return "redirect:/patient/doctors?aiFailed=true";
        }
    }

    @GetMapping("/doctors/{doctorId}/slots")
    public String doctorSlots(@PathVariable Long doctorId, HttpSession session, Model model) {
        if (requirePatient(session) == null) return "redirect:/login";
        
        List<com.medislot.app.entity.TimeSlot> allSlots = patientService.availableSlots(doctorId);
        java.util.Map<java.time.LocalDate, List<com.medislot.app.entity.TimeSlot>> groupedSlots = allSlots.stream()
                .collect(Collectors.groupingBy(com.medislot.app.entity.TimeSlot::getSlotDate, java.util.TreeMap::new, Collectors.toList()));
                
        model.addAttribute("groupedSlots", groupedSlots);
        model.addAttribute("doctorId", doctorId);
        return "patient-slots";
    }

    @PostMapping("/book")
    public String book(@RequestParam Long slotId, @RequestParam String reason, HttpSession session, Model model) {
        Long patientId = requirePatient(session);
        if (patientId == null) return "redirect:/login";
        try {
            patientService.book(patientId, slotId, reason);
            return "redirect:/patient/dashboard?booked";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
            return "patient-slots";
        }
    }

    @PostMapping("/appointments/{id}/cancel")
    public String cancel(@PathVariable Long id, HttpSession session) {
        Long patientId = requirePatient(session);
        if (patientId == null) return "redirect:/login";
        patientService.cancel(id, patientId);
        return "redirect:/patient/dashboard?cancelled";
    }

    @PostMapping("/appointments/{id}/review")
    public String review(
            @PathVariable Long id,
            @RequestParam Integer rating,
            @RequestParam(required = false) String comment,
            HttpSession session
    ) {
        Long patientId = requirePatient(session);
        if (patientId == null) return "redirect:/login";
        patientService.review(id, patientId, rating, comment);
        return "redirect:/patient/dashboard?reviewed";
    }

    private Long requirePatient(HttpSession session) {
        if (session.getAttribute("role") != Role.PATIENT) {
            return null;
        }
        return (Long) session.getAttribute("userId");
    }
}
