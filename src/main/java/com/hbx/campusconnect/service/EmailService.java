package com.hbx.campusconnect.service;

import java.io.IOException;

public interface EmailService {
    void sendOTPEmailBuilder(String toEmail, String code, String requestTime) throws IOException;

    void sendUpdateEmailEBuilder(String username, String toEmail, String code, String requestTime) throws IOException;

    void sendNoticeEmailBuilder(String toEmail, String username, String postTitle, String decision, String decisionTime, String reason) throws IOException;

    void sendAccountHelpOTP(String username, String email, String otp, String requestTime) throws IOException;
}
