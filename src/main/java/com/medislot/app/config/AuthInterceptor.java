package com.medislot.app.config;

import com.medislot.app.entity.Role;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String path = request.getRequestURI();

        if (isStaticResource(path) || isPublicPath(path)) {
            return true;
        }

        HttpSession session = request.getSession(false);
        Role role = session == null ? null : (Role) session.getAttribute("role");

        if (role == null) {
            response.sendRedirect(request.getContextPath() + "/login?expired");
            return false;
        }

        if (path.startsWith("/admin") && role != Role.ADMIN) {
            response.sendRedirect(request.getContextPath() + roleHome(role));
            return false;
        }

        if (path.startsWith("/doctor") && role != Role.DOCTOR) {
            response.sendRedirect(request.getContextPath() + roleHome(role));
            return false;
        }

        if (path.startsWith("/patient") && role != Role.PATIENT) {
            response.sendRedirect(request.getContextPath() + roleHome(role));
            return false;
        }

        return true;
    }

    private boolean isStaticResource(String path) {
        return path.startsWith("/css/")
                || path.startsWith("/js/")
                || path.startsWith("/images/")
                || path.startsWith("/webjars/")
                || path.equals("/favicon.ico");
    }

    private boolean isPublicPath(String path) {
        return path.equals("/")
                || path.equals("/login")
                || path.equals("/logout")
                || path.equals("/register/patient")
                || path.equals("/register/doctor")
                || path.startsWith("/forgot-password")
                || path.startsWith("/reset-password")
                || path.startsWith("/error");
    }

    private String roleHome(Role role) {
        if (role == Role.ADMIN) {
            return "/admin/dashboard";
        }
        if (role == Role.DOCTOR) {
            return "/doctor/dashboard";
        }
        return "/patient/dashboard";
    }
}
