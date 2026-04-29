package com.hbx.campusconnect.service;

import jakarta.mail.MessagingException;

public interface EmailService {
    void sendOTPEmailBuilder(String toEmail, String code, String requestTime) throws MessagingException;

    void sendUpdateEmailEBuilder(String username, String toEmail, String code, String requestTime) throws MessagingException;

    void sendNoticeEmailBuilder(String toEmail, String username, String postTitle, String decision, String decisionTime, String reason) throws MessagingException;

    void sendAccountHelpOTP(String username, String email, String otp, String requestTime) throws MessagingException;
}
