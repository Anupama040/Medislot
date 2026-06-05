package com.medislot.app.controller;

import com.medislot.app.entity.Role;
import com.medislot.app.service.DoctorService;
import com.medislot.app.service.NotificationService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import com.medislot.app.service.PdfService;
import com.medislot.app.entity.Prescription;
import com.medislot.app.entity.TimeSlot;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/doctor")
public class DoctorController {

    private final DoctorService doctorService;
    private final NotificationService notificationService;
    private final PdfService pdfService;

    public DoctorController(DoctorService doctorService, NotificationService notificationService, PdfService pdfService) {
        this.doctorService = doctorService;
        this.notificationService = notificationService;
        this.pdfService = pdfService;
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        Long doctorId = requireDoctor(session);
        if (doctorId == null) return "redirect:/login";
        model.addAttribute("name", session.getAttribute("name"));
        model.addAttribute("profile", doctorService.profile(doctorId));
        model.addAttribute("appointments", doctorService.appointments(doctorId));
        model.addAttribute("notifications", notificationService.forUser(doctorId));
        return "doctor-dashboard";
    }

    @GetMapping("/slots")
    public String slots(HttpSession session, Model model) {
        Long doctorId = requireDoctor(session);
        if (doctorId == null) return "redirect:/login";
        
        List<TimeSlot> allSlots = doctorService.slots(doctorId);
        Map<LocalDate, List<TimeSlot>> groupedSlots = allSlots.stream()
                .collect(Collectors.groupingBy(TimeSlot::getSlotDate, TreeMap::new, Collectors.toList()));
        
        model.addAttribute("groupedSlots", groupedSlots);
        model.addAttribute("today", LocalDate.now());
        return "doctor-slots";
    }

    @PostMapping("/slots")
    public String createSlots(
            @RequestParam LocalDate date,
            @RequestParam LocalTime start,
            @RequestParam LocalTime end,
            @RequestParam Integer duration,
            HttpSession session,
            Model model
    ) {
        Long doctorId = requireDoctor(session);
        if (doctorId == null) return "redirect:/login";
        try {
            doctorService.createSlots(doctorId, date, start, end, duration);
            return "redirect:/doctor/slots?created";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("slots", doctorService.slots(doctorId));
            model.addAttribute("today", LocalDate.now());
            return "doctor-slots";
        }
    }
    
    @PostMapping("/slots/auto-generate")
    public String autoGenerateSlots(HttpSession session, RedirectAttributes redirectAttributes) {
        Long doctorId = requireDoctor(session);
        if (doctorId == null) return "redirect:/login";
        try {
            doctorService.autoGenerateSlots(doctorId);
            redirectAttributes.addFlashAttribute("success", "Your weekly timetable has been successfully generated!");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/doctor/slots";
    }

    @PostMapping("/slots/{id}/delete")
    public String deleteSlot(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        Long doctorId = requireDoctor(session);
        if (doctorId == null) return "redirect:/login";
        try {
            doctorService.deleteSlot(id, doctorId);
            redirectAttributes.addFlashAttribute("success", "Slot deleted successfully.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/doctor/slots";
    }

    @GetMapping("/appointments")
    public String appointments(HttpSession session, Model model) {
        Long doctorId = requireDoctor(session);
        if (doctorId == null) return "redirect:/login";
        model.addAttribute("appointments", doctorService.appointments(doctorId));
        return "doctor-appointments";
    }

    @PostMapping("/appointments/{id}/approve")
    public String approve(@PathVariable Long id, HttpSession session) {
        Long doctorId = requireDoctor(session);
        if (doctorId == null) return "redirect:/login";
        doctorService.approveAppointment(id, doctorId);
        return "redirect:/doctor/appointments?approved";
    }

    @PostMapping("/appointments/{id}/reject")
    public String reject(@PathVariable Long id, HttpSession session) {
        Long doctorId = requireDoctor(session);
        if (doctorId == null) return "redirect:/login";
        doctorService.rejectAppointment(id, doctorId);
        return "redirect:/doctor/appointments?rejected";
    }

    @PostMapping("/appointments/{id}/complete")
    public String complete(
            @PathVariable Long id, 
            @RequestParam String diagnosis,
            @RequestParam String medicines,
            @RequestParam(required = false) String notes,
            @RequestParam(required = false, defaultValue = "false") boolean useAi,
            HttpSession session
    ) {
        Long doctorId = requireDoctor(session);
        if (doctorId == null) return "redirect:/login";
        doctorService.completeAppointment(id, doctorId, diagnosis, medicines, notes, useAi);
        return "redirect:/doctor/appointments?completed";
    }

    @PostMapping("/appointments/{id}/ai-summary")
    public String generateAiSummary(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        Long doctorId = requireDoctor(session);
        if (doctorId == null) return "redirect:/login";
        
        String summary = doctorService.generateAiSummary(id, doctorId);
        redirectAttributes.addFlashAttribute("aiSummary", summary);
        redirectAttributes.addFlashAttribute("summaryApptId", id);
        return "redirect:/doctor/appointments";
    }
    
    @GetMapping("/appointments/{id}/prescription/pdf")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable Long id, HttpSession session) {
        Long doctorId = requireDoctor(session);
        if (doctorId == null) return ResponseEntity.status(401).build();
        
        try {
            Prescription prescription = doctorService.getPrescriptionByAppointment(id, doctorId);
            byte[] pdfBytes = pdfService.generatePrescriptionPdf(prescription);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "Prescription_Appt_" + id + ".pdf");
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    private Long requireDoctor(HttpSession session) {
        if (session.getAttribute("role") != Role.DOCTOR) {
            return null;
        }
        return (Long) session.getAttribute("userId");
    }
}
