package com.medislot.app.controller;

import com.medislot.app.entity.Role;
import com.medislot.app.repository.AppointmentRepository;
import com.medislot.app.service.AdminService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final AdminService adminService;
    private final AppointmentRepository appointmentRepository;

    public AdminController(AdminService adminService, AppointmentRepository appointmentRepository) {
        this.adminService = adminService;
        this.appointmentRepository = appointmentRepository;
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        if (!isAdmin(session)) return "redirect:/login";
        model.addAttribute("name", session.getAttribute("name"));
        model.addAttribute("users", adminService.users().size());
        model.addAttribute("pendingDoctors", adminService.pendingDoctors().size());
        model.addAttribute("appointments", adminService.totalAppointments());
        model.addAttribute("recentAppointments", appointmentRepository.findAllByOrderByCreatedAtDesc());
        return "admin-dashboard";
    }

    @GetMapping("/doctors")
    public String doctors(HttpSession session, Model model) {
        if (!isAdmin(session)) return "redirect:/login";
        model.addAttribute("doctors", adminService.allDoctors());
        return "admin-doctors";
    }

    @PostMapping("/doctors/{id}/approve")
    public String approveDoctor(@PathVariable Long id, @RequestParam(required = false) String remark, HttpSession session) {
        if (!isAdmin(session)) return "redirect:/login";
        adminService.approveDoctor(id, remark);
        return "redirect:/admin/doctors?approved";
    }

    @PostMapping("/doctors/{id}/reject")
    public String rejectDoctor(@PathVariable Long id, @RequestParam(required = false) String remark, HttpSession session) {
        if (!isAdmin(session)) return "redirect:/login";
        adminService.rejectDoctor(id, remark);
        return "redirect:/admin/doctors?rejected";
    }

    @PostMapping("/doctors/{id}/suspend")
    public String suspendDoctor(@PathVariable Long id, @RequestParam(required = false) String remark, HttpSession session) {
        if (!isAdmin(session)) return "redirect:/login";
        adminService.suspendDoctor(id, remark);
        return "redirect:/admin/doctors?suspended";
    }

    @GetMapping("/users")
    public String users(HttpSession session, Model model) {
        if (!isAdmin(session)) return "redirect:/login";
        model.addAttribute("users", adminService.users());
        return "admin-users";
    }

    @PostMapping("/users/{id}/toggle")
    public String toggleUser(@PathVariable Long id, HttpSession session, Model model) {
        if (!isAdmin(session)) return "redirect:/login";
        adminService.toggleUser(id, (Long) session.getAttribute("userId"));
        return "redirect:/admin/users?toggled";
    }

    @GetMapping("/specializations")
    public String specializations(HttpSession session, Model model) {
        if (!isAdmin(session)) return "redirect:/login";
        model.addAttribute("specializations", adminService.specializations());
        return "admin-specializations";
    }

    @PostMapping("/specializations")
    public String addSpecialization(@RequestParam String name, @RequestParam(required = false) String description, HttpSession session, Model model) {
        if (!isAdmin(session)) return "redirect:/login";
        try {
            adminService.saveSpecialization(name, description);
            return "redirect:/admin/specializations?added";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("specializations", adminService.specializations());
            return "admin-specializations";
        }
    }

    @GetMapping("/appointments")
    public String appointments(HttpSession session, Model model) {
        if (!isAdmin(session)) return "redirect:/login";
        model.addAttribute("appointments", appointmentRepository.findAllByOrderByCreatedAtDesc());
        return "admin-appointments";
    }

    private boolean isAdmin(HttpSession session) {
        return session.getAttribute("role") == Role.ADMIN;
    }
}
