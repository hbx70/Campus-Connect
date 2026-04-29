package com.hbx.campusconnect.service.impl;

import com.hbx.campusconnect.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {
    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private org.springframework.core.env.Environment env;

    private static final String LOGO_URL = "https://img.campusconnect.one/avatars/logo.png";

    @Override
    public void sendOTPEmailBuilder(String toEmail, String code, String requestTime) throws MessagingException {
        String title = "Email Verification Code";
        String html = buildVerifyEmailHtml(code, requestTime);
        this.sendEmailHandler(toEmail, title, html);
    }

    @Override
    public void sendUpdateEmailEBuilder(String username, String toEmail, String code, String requestTime) throws MessagingException {
        String title = "Email Verification Code";
        String html = buildVerifyEmailChangeHtml(username, toEmail, code, requestTime);
        this.sendEmailHandler(toEmail, title, html);
    }

    @Override
    public void sendNoticeEmailBuilder(String toEmail, String username, String postTitle, String decision, String decisionTime, String reason) throws MessagingException {
        String title = "Post Review " + ("APPROVED".equalsIgnoreCase(decision) ? "Approved" : "Rejected");
        String html = buildNoticeEmailHtml(username, postTitle, decision, decisionTime, reason);
        this.sendEmailHandler(toEmail, title, html);
    }

    @Override
    public void sendAccountHelpOTP(String username, String toEmail, String otp, String requestTime) throws MessagingException {
        String title = "Email Verification Code";
        String html = buildPasswordChangedHtml(username, otp, requestTime);
        this.sendEmailHandler(toEmail, title, html);
    }

    private void sendEmailHandler(String toEmail, String title, String html) throws MessagingException {
        String from = env.getProperty("spring.mail.username");
        String subject = "CampusConnect - " + title;

        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "UTF-8");

        helper.setFrom("CampusConnect <" + from + ">");
        helper.setTo(toEmail);
        helper.setSubject(subject);
        helper.setText(html, true);
        mailSender.send(mimeMessage);
    }

    private String buildNoticeEmailHtml(String username, String postTitle, String decision, String decisionTime, String reason) {

//        String logoUrl = "https://img.campusconnect.one/assets/logo.png"; // 你的R2 Logo地址
        boolean approved = "APPROVED".equalsIgnoreCase(decision);

        String decisionText = approved ? "Approved" : "Rejected";
        String decisionBadgeBg = approved ? "#ecfdf5" : "#fef2f2";
        String decisionBadgeBorder = approved ? "#a7f3d0" : "#fecaca";
        String decisionBadgeColor = approved ? "#065f46" : "#991b1b";

        String safeReason = (reason == null || reason.trim().isEmpty())
                ? (approved ? "No additional notes." : "No reason provided.")
                : reason.trim();

        return """
                <!DOCTYPE html>
                    <html lang="en">
                    <head>
                      <meta charset="UTF-8">
                      <meta name="viewport" content="width=device-width, initial-scale=1.0">
                      <title>Post Review Update</title>
                    </head>
                    <body style="margin:0;padding:0;background:#f5f7fb;font-family:Arial,Helvetica,sans-serif;">
                      <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" style="background:#f5f7fb;padding:20px 0;">
                        <tr>
                          <td align="center" style="padding:0 12px;">
                            <!-- Container -->
                            <table role="presentation" width="640" cellspacing="0" cellpadding="0"
                                   style="width:640px;max-width:640px;background:#ffffff;border-radius:12px;
                                          overflow:hidden;box-shadow:0 6px 20px rgba(0,0,0,.06);">
                
                              <!-- Header -->
                              <tr style="width=100%%">
                                <td style="padding:18px 20px;">
                                  <table role="presentation" width="100%%" cellspacing="0" cellpadding="0">
                                    <tr>
                                      <!-- Logo -->
                                      <td width="56" style="width:56px;padding-right:12px;vertical-align:middle;">
                                        <img src="%s" alt="CampusConnect" width="75" height="75"
                                             style="display:block;border:0;outline:none;text-decoration:none;width:75px;height:75px;object-fit:contain;">
                                      </td>
                
                                      <td style="vertical-align:middle;">
                                        <div style="font-weight:800;font-size:20px;color:#111827;line-height:1.2;">
                                          CampusConnect
                                        </div>
                                        <div style="font-size:16px;color:#6b7280;line-height:1.2;margin-top:2px;">
                                          Post Review Notification
                                        </div>
                                      </td>
                
                                      <!-- Right label -->
                                      <td align="right" style="vertical-align:middle;">
                                        <span style="display:inline-block;padding:6px 10px;border-radius:999px;
                                                     background:%s;border:1px solid %s;color:%s;
                                                     font-size:20px;font-weight:700;">
                                          %s
                                        </span>
                                      </td>
                                    </tr>
                                  </table>
                                </td>
                              </tr>
                
                              <!-- Title -->
                              <tr>
                                <td style="padding:0 20px 6px 20px;">
                                  <div style="font-size:22px;font-weight:800;color:#111827;line-height:1.3;">
                                    Your post has been reviewed
                                  </div>
                                </td>
                              </tr>
                
                              <!-- Greeting -->
                              <tr>
                                <td style="padding:0 20px 16px 20px;">
                                  <div style="color:#4b5563;font-size:18px;line-height:1.7;">
                                    Hi <span style="font-weight:800;color:#111827;">%s</span>, your post review status has been updated.
                                    Details are below.
                                  </div>
                                </td>
                              </tr>
                
                              <!-- Card -->
                              <tr width="100%%">
                                <td style="padding:0 20px 20px 20px;">
                                  <table role="presentation" width="100%%" cellspacing="0" cellpadding="0"
                                         style="border:1px solid #e5e7eb;border-radius:12px;background:#f9fafb;">
                                    <tr>
                                      <td style="padding:16px;">
                
                                        <!-- Post title -->
                                        <div style="font-size:18px;color:#6b7280;margin-bottom:6px;">Post Title</div>
                                        <div style="font-size:21px;font-weight:800;color:#111827;line-height:1.4;margin-bottom:14px;">
                                          %s
                                        </div>
                
                                        <!-- Decision + time (2 columns table) -->
                                        <table role="presentation" width="100%%" cellspacing="0" cellpadding="0">
                                          <tr>
                                            <td style="width:50%%;vertical-align:top;padding-right:10px;">
                                              <div style="font-size:16px;color:#6b7280;margin-bottom:6px;">Decision</div>
                                              <div style="font-size:19px;font-weight:800;color:#111827;">%s</div>
                                            </td>
                                            <td style="width:50%%;vertical-align:top;padding-left:10px;">
                                              <div style="font-size:16px;color:#6b7280;margin-bottom:6px;">Decision Time</div>
                                              <div style="font-size:19px;font-weight:800;color:#111827;">%s</div>
                                            </td>
                                          </tr>
                                        </table>
                
                                        <!-- Reason -->
                                        <div style="font-size:16px;color:#6b7280;margin:14px 0 6px 0;">Reason / Notes</div>
                                        <div style="font-size:17px;color:#374151;line-height:1.7;background:#ffffff;
                                                    border:1px solid #eef2f7;border-radius:10px;padding:12px;">
                                          %s
                                        </div>
                
                                      </td>
                                    </tr>
                                  </table>
                                </td>
                              </tr>
                
                              <!-- Footer -->
                              <tr>
                                <td style="padding:14px 20px;border-top:1px solid #eef2f7;">
                                  <div style="color:#9ca3af;font-size:12px;line-height:1.6;">
                                    CampusConnect • Singapore<br>
                                    This is an automated message. Please do not reply.
                                  </div>
                                </td>
                              </tr>
                            </table>
                          </td>
                        </tr>
                      </table>
                    </body>
                    </html>
    """.formatted(
                LOGO_URL,
                decisionBadgeBg, decisionBadgeBorder, decisionBadgeColor, decisionText,
                escapeHtml(username),
                escapeHtml(postTitle),
                escapeHtml(decisionText),
                escapeHtml(decisionTime),
                escapeHtml(safeReason)
        );
    }

    private String buildVerifyEmailHtml(String code, String requestTime) {
        return """
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Verify your email</title>
</head>
<body style="margin:0;padding:0;background:#f5f7fb;font-family:Arial,Helvetica,sans-serif;">
<table role="presentation" width="100%%" cellpadding="0" cellspacing="0"
       style="width:100%%;background:#f5f7fb;padding:20px 0;">
  <tr>
    <td align="center" style="padding:0 12px;">

      <!-- Container -->
      <table role="presentation" width="640" cellpadding="0" cellspacing="0"
             style="width:640px;max-width:640px;background:#ffffff;border-radius:12px;
                    overflow:hidden;box-shadow:0 6px 20px rgba(0,0,0,.06);">

        <!-- Header -->
        <tr>
          <td style="padding:18px 20px;">
            <table role="presentation" width="100%%" cellpadding="0" cellspacing="0"
                   style="width:100%%;table-layout:fixed;">
              <tr>
                <td width="56" style="width:56px;padding-right:12px;vertical-align:middle;">
                  <img src="%s" alt="CampusConnect" width="44" height="44"
                       style="display:block;width:44px;height:44px;object-fit:contain;border:0;outline:none;">
                </td>
                <td style="vertical-align:middle;">
                  <div style="font-weight:800;font-size:16px;color:#111827;line-height:1.2;">
                    CampusConnect
                  </div>
                  <div style="font-size:12px;color:#6b7280;line-height:1.2;margin-top:2px;">
                    Registered security verification
                  </div>
                </td>
                <td align="right" style="vertical-align:middle;">
                  <span style="display:inline-block;padding:6px 10px;border-radius:999px;
                               background:#fff7ed;border:1px solid #fed7aa;color:#9a3412;
                               font-size:12px;font-weight:700;">
                    Registered
                  </span>
                </td>
              </tr>
            </table>
          </td>
        </tr>

        <!-- Title -->
        <tr>
          <td style="padding:0 20px 8px 20px;">
            <div style="font-size:18px;font-weight:800;color:#111827;line-height:1.3;">
              Verify your email
            </div>
          </td>
        </tr>

        <!-- Intro -->
        <tr>
          <td style="padding:0 20px 16px 20px;">
            <div style="color:#4b5563;font-size:14px;line-height:1.7;">
              Use the verification code below to complete your registration. This code is valid for <b>5 minutes</b>.
            </div>
          </td>
        </tr>

        <!-- Card -->
        <tr>
          <td style="padding:0 20px 20px 20px;">
            <table role="presentation" width="100%%" cellpadding="0" cellspacing="0"
                   style="width:100%%;table-layout:fixed;border:1px solid #e5e7eb;
                          border-radius:12px;background:#f9fafb;">
              <tr>
                <td style="padding:16px;">

                  <table role="presentation" width="100%%" cellpadding="0" cellspacing="0"
                         style="width:100%%;table-layout:fixed;">
                    <tr>
                      <td style="width:50%%;vertical-align:top;padding-right:10px;">
                        <div style="font-size:12px;color:#6b7280;margin-bottom:6px;">
                          Request Time
                        </div>
                        <div style="font-size:14px;font-weight:800;color:#111827;">
                          %s
                        </div>
                      </td>
                      <td style="width:50%%;vertical-align:top;padding-left:10px;">
                        <div style="font-size:12px;color:#6b7280;margin-bottom:6px;">
                          Code Validity
                        </div>
                        <div style="font-size:14px;font-weight:800;color:#111827;">
                          5 minutes
                        </div>
                      </td>
                    </tr>
                  </table>

                  <div style="margin-top:14px;font-size:12px;color:#6b7280;margin-bottom:6px;">
                    Verification Code
                  </div>

                  <div style="background:#ffffff;border:1px solid #eef2f7;border-radius:12px;
                              text-align:center;padding:14px;">
                    <div style="font-size:12px;color:#6b7280;margin-bottom:8px;">
                      Your code
                    </div>
                    <div style="font-size:28px;font-weight:800;letter-spacing:6px;color:#111827;">
                      %s
                    </div>
                  </div>

                  <div style="margin-top:14px;font-size:13px;color:#6b7280;line-height:1.7;">
                    If you didn’t request this, you can safely ignore this email.
                  </div>

                </td>
              </tr>
            </table>
          </td>
        </tr>

        <!-- Footer -->
        <tr>
          <td style="padding:14px 20px;border-top:1px solid #eef2f7;">
            <div style="color:#9ca3af;font-size:12px;line-height:1.6;">
              CampusConnect • Singapore<br>
              This is an automated message. Please do not reply.
            </div>
          </td>
        </tr>
      </table>
    </td>
  </tr>
</table>
</body>
</html>
""".formatted(
                LOGO_URL,
                escapeHtml(requestTime),
                escapeHtml(code)
        );
    }



//    private String buildVerifyEmailHtml(String code) {
//        return """
//                <!doctype html>
//                <html>
//                <head>
//                  <meta charset="utf-8">
//                  <meta name="viewport" content="width=device-width, initial-scale=1.0">
//                  <title>Verify your email</title>
//                </head>
//                <body style="margin:0;padding:0;background:#f5f7fb;font-family:Arial,Helvetica,sans-serif;">
//                  <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" style="background:#f5f7fb;padding:24px 0;">
//                    <tr>
//                      <td align="center">
//                        <table role="presentation" width="600" cellspacing="0" cellpadding="0" style="background:#ffffff;border-radius:14px;overflow:hidden;box-shadow:0 6px 20px rgba(0,0,0,.06);">
//                          <tr>
//                            <td style="padding:22px 28px;background:#0b1220;">
//                              <table role="presentation" width="100%%" cellspacing="0" cellpadding="0">
//                                <tr>
//                                  <td valign="middle">
//                                    <img src="%s" alt="CampusConnect" height="32" style="display:block;border:0;outline:none;">
//                                  </td>
//                                  <td valign="middle" align="right" style="color:#b6c2ff;font-size:12px;">
//                                    Security verification
//                                  </td>
//                                </tr>
//                              </table>
//                            </td>
//                          </tr>
//                          <tr>
//                            <td style="padding:28px;">
//                              <h1 style="margin:0 0 10px 0;font-size:20px;color:#111827;">Verify your email address</h1>
//                              <p style="margin:0 0 16px 0;font-size:14px;line-height:1.6;color:#4b5563;">
//                                Use the verification code below to complete your registration. This code is valid for <b>5 minutes</b>.
//                              </p>
//                              <div style="margin:18px 0 16px 0;padding:16px;border:1px solid #e5e7eb;border-radius:12px;background:#f9fafb;text-align:center;">
//                                <div style="font-size:12px;color:#6b7280;margin-bottom:8px;">Your verification code</div>
//                                <div style="font-size:28px;letter-spacing:6px;font-weight:700;color:#111827;">%s</div>
//                              </div>
//                              <p style="margin:0 0 14px 0;font-size:13px;line-height:1.6;color:#6b7280;">
//                                If you didn’t request this, you can safely ignore this email.
//                              </p>
//                              <hr style="border:none;border-top:1px solid #eef2f7;margin:18px 0;">
//                              <p style="margin:0;font-size:12px;line-height:1.6;color:#9ca3af;">
//                                CampusConnect • Singapore<br>
//                                This is an automated message, please do not reply.
//                              </p>
//                            </td>
//                          </tr>
//                        </table>
//                      </td>
//                    </tr>
//                  </table>
//                </body>
//                </html>
//                """.formatted(LOGO_URL, code);
//    }

    private String buildVerifyEmailChangeHtml(String username, String newEmail, String code, String requestTime) {
        return """
<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Confirm Email Change</title>
</head>
<body style="margin:0;padding:0;background:#f5f7fb;font-family:Arial,Helvetica,sans-serif;">
<table role="presentation" width="100%%" cellpadding="0" cellspacing="0"
       style="width:100%%;background:#f5f7fb;padding:20px 0;">
  <tr>
    <td align="center" style="padding:0 12px;">

      <table role="presentation" width="640" cellpadding="0" cellspacing="0"
             style="width:640px;max-width:640px;background:#ffffff;border-radius:12px;
                    overflow:hidden;box-shadow:0 6px 20px rgba(0,0,0,.06);">

        <!-- Header -->
        <tr>
          <td style="padding:18px 20px;">
            <table role="presentation" width="100%%" cellpadding="0" cellspacing="0"
                   style="width:100%%;table-layout:fixed;">
              <tr>
                <td width="56" style="width:56px;padding-right:12px;vertical-align:middle;">
                  <img src="%s" alt="CampusConnect" width="44" height="44"
                       style="display:block;width:44px;height:44px;object-fit:contain;border:0;outline:none;">
                </td>
                <td style="vertical-align:middle;">
                  <div style="font-weight:800;font-size:16px;color:#111827;line-height:1.2;">
                    CampusConnect
                  </div>
                  <div style="font-size:12px;color:#6b7280;line-height:1.2;margin-top:2px;">
                    Security verification
                  </div>
                </td>
                <td align="right" style="vertical-align:middle;">
                  <span style="display:inline-block;padding:6px 10px;border-radius:999px;
                               background:#eff6ff;border:1px solid #bfdbfe;color:#1d4ed8;
                               font-size:12px;font-weight:700;">
                    Email Change
                  </span>
                </td>
              </tr>
            </table>
          </td>
        </tr>

        <!-- Title -->
        <tr>
          <td style="padding:0 20px 8px 20px;">
            <div style="font-size:18px;font-weight:800;color:#111827;line-height:1.3;">
              Confirm your new email address
            </div>
          </td>
        </tr>

        <!-- Intro -->
        <tr>
          <td style="padding:0 20px 16px 20px;">
            <div style="color:#4b5563;font-size:14px;line-height:1.7;">
              Hi <b>%s</b>, we received a request to change your account email to:
              <span style="font-weight:800;color:#111827;">%s</span>
            </div>
          </td>
        </tr>

        <!-- Card -->
        <tr>
          <td style="padding:0 20px 20px 20px;">
            <table role="presentation" width="100%%" cellpadding="0" cellspacing="0"
                   style="width:100%%;table-layout:fixed;border:1px solid #e5e7eb;
                          border-radius:12px;background:#f9fafb;">
              <tr>
                <td style="padding:16px;">
                  <table role="presentation" width="100%%" cellpadding="0" cellspacing="0"
                         style="width:100%%;table-layout:fixed;">
                    <tr>
                      <td style="width:50%%;vertical-align:top;padding-right:10px;">
                        <div style="font-size:12px;color:#6b7280;margin-bottom:6px;">Request Time</div>
                        <div style="font-size:14px;font-weight:800;color:#111827;">%s</div>
                      </td>
                      <td style="width:50%%;vertical-align:top;padding-left:10px;">
                        <div style="font-size:12px;color:#6b7280;margin-bottom:6px;">New Email</div>
                        <div style="font-size:14px;font-weight:800;color:#111827;word-break:break-word;">
                          %s
                        </div>
                      </td>
                    </tr>
                  </table>

                  <div style="margin-top:14px;font-size:12px;color:#6b7280;margin-bottom:6px;">
                    Verification Code (valid for 5 minutes)
                  </div>

                  <div style="background:#ffffff;border:1px solid #eef2f7;border-radius:12px;
                              text-align:center;padding:14px;">
                    <div style="font-size:12px;color:#6b7280;margin-bottom:8px;">Your code</div>
                    <div style="font-size:28px;font-weight:800;letter-spacing:6px;color:#111827;">
                      %s
                    </div>
                  </div>

                  <div style="margin-top:14px;font-size:13px;color:#6b7280;line-height:1.7;">
                    If you didn’t request this change, please ignore this email.
                  </div>

                </td>
              </tr>
            </table>
          </td>
        </tr>

        <!-- Footer -->
        <tr>
          <td style="padding:14px 20px;border-top:1px solid #eef2f7;">
            <div style="color:#9ca3af;font-size:12px;line-height:1.6;">
              CampusConnect • Singapore<br>
              This is an automated message. Please do not reply.
            </div>
          </td>
        </tr>

      </table>

    </td>
  </tr>
</table>
</body>
</html>
""".formatted(
                LOGO_URL,
                escapeHtml(username),
                escapeHtml(newEmail),
                escapeHtml(requestTime),
                escapeHtml(newEmail),
                escapeHtml(code)
        );
    }

    private String buildPasswordChangedHtml(String username, String code, String requestTime) {
        return """
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Account Help Verification</title>
</head>
<body style="margin:0;padding:0;background:#f5f7fb;font-family:Arial,Helvetica,sans-serif;">
<table role="presentation" width="100%%" cellpadding="0" cellspacing="0"
       style="width:100%%;background:#f5f7fb;padding:20px 0;">
  <tr>
    <td align="center" style="padding:0 12px;">

      <!-- Container -->
      <table role="presentation" width="640" cellpadding="0" cellspacing="0"
             style="width:640px;max-width:640px;background:#ffffff;border-radius:12px;
                    overflow:hidden;box-shadow:0 6px 20px rgba(0,0,0,.06);">

        <!-- Header -->
        <tr>
          <td style="padding:18px 20px;">
            <table role="presentation" width="100%%" cellpadding="0" cellspacing="0"
                   style="width:100%%;table-layout:fixed;">
              <tr>
                <td width="56" style="width:56px;padding-right:12px;vertical-align:middle;">
                  <img src="%s" alt="CampusConnect" width="44" height="44"
                       style="display:block;width:44px;height:44px;object-fit:contain;border:0;outline:none;">
                </td>
                <td style="vertical-align:middle;">
                  <div style="font-weight:800;font-size:16px;color:#111827;line-height:1.2;">
                    CampusConnect
                  </div>
                  <div style="font-size:12px;color:#6b7280;line-height:1.2;margin-top:2px;">
                    Account security verification
                  </div>
                </td>
                <td align="right" style="vertical-align:middle;">
                  <span style="display:inline-block;padding:6px 10px;border-radius:999px;
                               background:#fff7ed;border:1px solid #fed7aa;color:#9a3412;
                               font-size:12px;font-weight:700;">
                    Account Help
                  </span>
                </td>
              </tr>
            </table>
          </td>
        </tr>

        <!-- Title -->
        <tr>
          <td style="padding:0 20px 8px 20px;">
            <div style="font-size:18px;font-weight:800;color:#111827;line-height:1.3;">
              Verify your account
            </div>
          </td>
        </tr>

        <!-- Intro -->
        <tr>
          <td style="padding:0 20px 16px 20px;">
            <div style="color:#4b5563;font-size:14px;line-height:1.7;">
              Hi <b>%s</b>, we received a request to change your account password or retrieve your username.
              Please use the verification code below to continue. After successful verification, the Account Help page will remain accessible for 15 minutes only.
            </div>
          </td>
        </tr>

        <!-- Card -->
        <tr>
          <td style="padding:0 20px 20px 20px;">
            <table role="presentation" width="100%%" cellpadding="0" cellspacing="0"
                   style="width:100%%;table-layout:fixed;border:1px solid #e5e7eb;
                          border-radius:12px;background:#f9fafb;">
              <tr>
                <td style="padding:16px;">

                  <table role="presentation" width="100%%" cellpadding="0" cellspacing="0"
                         style="width:100%%;table-layout:fixed;">
                    <tr>
                      <td style="width:50%%;vertical-align:top;padding-right:10px;">
                        <div style="font-size:12px;color:#6b7280;margin-bottom:6px;">
                          Request Time
                        </div>
                        <div style="font-size:14px;font-weight:800;color:#111827;">
                          %s
                        </div>
                      </td>
                      <td style="width:50%%;vertical-align:top;padding-left:10px;">
                        <div style="font-size:12px;color:#6b7280;margin-bottom:6px;">
                          Code Validity
                        </div>
                        <div style="font-size:14px;font-weight:800;color:#111827;">
                          5 minutes
                        </div>
                      </td>
                    </tr>
                  </table>

                  <div style="margin-top:14px;font-size:12px;color:#6b7280;margin-bottom:6px;">
                    Verification Code
                  </div>

                  <div style="background:#ffffff;border:1px solid #eef2f7;border-radius:12px;
                              text-align:center;padding:14px;">
                    <div style="font-size:12px;color:#6b7280;margin-bottom:8px;">
                      Your code
                    </div>
                    <div style="font-size:28px;font-weight:800;letter-spacing:6px;color:#111827;">
                      %s
                    </div>
                  </div>

                  <div style="margin-top:14px;font-size:13px;color:#6b7280;line-height:1.7;">
                    If you did not request account help, please ignore this email or contact support immediately.
                  </div>

                </td>
              </tr>
            </table>
          </td>
        </tr>

        <!-- Footer -->
        <tr>
          <td style="padding:14px 20px;border-top:1px solid #eef2f7;">
            <div style="color:#9ca3af;font-size:12px;line-height:1.6;">
              CampusConnect • Singapore<br>
              This is an automated message. Please do not reply.
            </div>
          </td>
        </tr>
      </table>
    </td>
  </tr>
</table>
</body>
</html>
""".formatted(
                LOGO_URL,
                escapeHtml(username),
                escapeHtml(requestTime),
                escapeHtml(code)
        );
    }

    private String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
