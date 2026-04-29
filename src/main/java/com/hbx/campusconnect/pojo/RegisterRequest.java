package com.hbx.campusconnect.pojo;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank(message = "Username cannot be empty")
    @Size(min = 3, max = 16, message = "The length of the username must be between 3 and 16")
    private String username;
    @NotBlank(message = "Email cannot be empty")
    @Email(message = "Please enter valid email address")
    private String email;
    @NotBlank(message = "Password cannot be empty")
    @Size(min = 6, message = "The length of the password must be at least 6")
    private String password;
    @NotBlank(message = "Avatar cannot be empty")
    private String userAvatar;
    @NotBlank(message = "Please enter verification code")
    @Size(min = 6, max = 6, message = "please enter 6 digits verification code")
    private String otp;
}
