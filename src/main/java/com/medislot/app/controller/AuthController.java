package com.medislot.app.controller;

import com.medislot.app.entity.Role;
import com.medislot.app.entity.User;
import com.medislot.app.repository.SpecializationRepository;
import com.medislot.app.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

@Controller
public class AuthController {

    private final AuthService authService;
    private final SpecializationRepository specializationRepository;

    public AuthController(AuthService authService, SpecializationRepository specializationRepository) {
        this.authService = authService;
        this.specializationRepository = specializationRepository;
    }

    @GetMapping("/")
    public String home(HttpSession session) {
        Role role = (Role) session.getAttribute("role");
        if (role == Role.ADMIN) {
            return "redirect:/admin/dashboard";
        }
        if (role == Role.DOCTOR) {
            return "redirect:/doctor/dashboard";
        }
        if (role == Role.PATIENT) {
            return "redirect:/patient/dashboard";
        }
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String loginPage(HttpSession session) {
        Role role = (Role) session.getAttribute("role");
        if (role != null) {
            return redirectByRole(role);
        }
        return "login";
    }

    @PostMapping("/login")
    public String login(
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam(defaultValue = "patient") String userRole,
            HttpServletRequest request,
            Model model
    ) {
        return authService.login(email, password)
                .map(user -> {
                    if (user.getRole() == Role.DOCTOR && !"doctor".equalsIgnoreCase(userRole)) {
                        model.addAttribute("error", "Doctors must use the Doctor Access login. Please click the Doctor icon above.");
                        return "login";
                    }
                    if (user.getRole() == Role.PATIENT && "doctor".equalsIgnoreCase(userRole)) {
                        model.addAttribute("error", "Patients cannot use Doctor Access login.");
                        return "login";
                    }
                    return loginUser(user, request);
                })
                .orElseGet(() -> {
                    model.addAttribute("error", "Invalid credentials or account not approved/enabled.");
                    return "login";
                });
    }

    @GetMapping("/register/patient")
    public String patientRegisterPage(HttpSession session) {
        Role role = (Role) session.getAttribute("role");
        if (role != null) {
            return redirectByRole(role);
        }
        return "register-patient";
    }

    @PostMapping("/register/patient")
    public String registerPatient(
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam String phone,
            @RequestParam String password,
            @RequestParam String city,
            Model model
    ) {
        try {
            authService.registerPatient(name, email, phone, password, city);
            model.addAttribute("success", "Patient account created. Please login.");
            return "login";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
            return "register-patient";
        }
    }

    @GetMapping("/register/doctor")
    public String doctorRegisterPage(HttpSession session, Model model) {
        Role role = (Role) session.getAttribute("role");
        if (role != null) {
            return redirectByRole(role);
        }
        model.addAttribute("specializations", specializationRepository.findByActiveTrueOrderByNameAsc());
        return "register-doctor";
    }

    @PostMapping("/register/doctor")
    public String registerDoctor(
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam String phone,
            @RequestParam String password,
            @RequestParam Long specializationId,
            @RequestParam String qualification,
            @RequestParam String registrationNumber,
            @RequestParam Integer experienceYears,
            @RequestParam String clinicName,
            @RequestParam String clinicAddress,
            @RequestParam String city,
            @RequestParam BigDecimal consultationFee,
            @RequestParam(required = false) String bio,
            Model model
    ) {
        try {
            authService.registerDoctor(name, email, phone, password, specializationId, qualification,
                    registrationNumber, experienceYears, clinicName, clinicAddress, city, consultationFee, bio);
            model.addAttribute("success", "Doctor request submitted. Admin approval is required before login.");
            return "login";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("specializations", specializationRepository.findByActiveTrueOrderByNameAsc());
            return "register-doctor";
        }
    }
    
    @GetMapping("/forgot-password")
    public String forgotPasswordPage(HttpSession session) {
        Role role = (Role) session.getAttribute("role");
        if (role != null) return redirectByRole(role);
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String handleForgotPassword(@RequestParam String email, Model model) {
        try {
            authService.generatePasswordResetOtp(email);
            return "redirect:/reset-password?email=" + email;
        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
            return "forgot-password";
        } catch (RuntimeException ex) {
            model.addAttribute("error", ex.getMessage());
            return "forgot-password";
        }
    }

    @GetMapping("/reset-password")
    public String resetPasswordPage(@RequestParam String email, HttpSession session, Model model) {
        Role role = (Role) session.getAttribute("role");
        if (role != null) return redirectByRole(role);
        model.addAttribute("email", email);
        return "reset-password";
    }

    @PostMapping("/reset-password")
    public String handleResetPassword(
            @RequestParam String email,
            @RequestParam String otp,
            @RequestParam String password,
            Model model
    ) {
        try {
            authService.resetPassword(email, otp, password);
            model.addAttribute("success", "Password successfully reset! Please login.");
            return "login";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("email", email);
            return "reset-password";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login?logout";
    }

    private String loginUser(User user, HttpServletRequest request) {
        HttpSession oldSession = request.getSession(false);
        if (oldSession != null) {
            oldSession.invalidate();
        }

        HttpSession session = request.getSession(true);
        session.setAttribute("userId", user.getId());
        session.setAttribute("name", user.getName());
        session.setAttribute("role", user.getRole());
        session.setMaxInactiveInterval(30 * 60);

        return redirectByRole(user.getRole());
    }

    private String redirectByRole(Role role) {
        if (role == Role.ADMIN) {
            return "redirect:/admin/dashboard";
        }
        if (role == Role.DOCTOR) {
            return "redirect:/doctor/dashboard";
        }
        return "redirect:/patient/dashboard";
    }
}
