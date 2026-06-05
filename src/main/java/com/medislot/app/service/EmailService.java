package com.medislot.app.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendPasswordResetOtp(String to, String otp) {
        try {
            jakarta.mail.internet.MimeMessage message = mailSender.createMimeMessage();
            org.springframework.mail.javamail.MimeMessageHelper helper = new org.springframework.mail.javamail.MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom("medislot.global.connect@gmail.com", "MediSlot Support");
            helper.setTo(to);
            helper.setSubject("MediSlot - Password Reset OTP");
            
            String htmlMsg = "<div style=\"font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; border: 1px solid #e0e0e0; border-radius: 8px; overflow: hidden;\">"
                           + "<div style=\"background-color: #0d6efd; color: white; padding: 20px; text-align: center;\">"
                           + "  <h2 style=\"margin: 0;\">MediSlot Account Security</h2>"
                           + "</div>"
                           + "<div style=\"padding: 30px; background-color: #ffffff;\">"
                           + "  <p style=\"font-size: 16px; color: #333333;\">Hello,</p>"
                           + "  <p style=\"font-size: 16px; color: #333333;\">We received a request to reset your password. Use the following One-Time Password (OTP) to proceed:</p>"
                           + "  <div style=\"text-align: center; margin: 30px 0;\">"
                           + "    <span style=\"font-size: 32px; font-weight: bold; letter-spacing: 5px; color: #0d6efd; background-color: #f8f9fa; padding: 15px 25px; border-radius: 5px; border: 1px dashed #0d6efd;\">" + otp + "</span>"
                           + "  </div>"
                           + "  <p style=\"font-size: 14px; color: #555555;\">This OTP is valid for <strong>10 minutes</strong>.</p>"
                           + "  <p style=\"font-size: 14px; color: #777777; margin-top: 30px; border-top: 1px solid #eeeeee; padding-top: 20px;\">If you did not request a password reset, please ignore this email. Your account is secure.</p>"
                           + "</div>"
                           + "</div>";
            
            helper.setText(htmlMsg, true);
            mailSender.send(message);
            
        } catch (Exception e) {
            System.err.println("Failed to send email to " + to + ": " + e.getMessage());
            throw new RuntimeException("Could not send email. Please check server configuration.");
        }
    }
}
