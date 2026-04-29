package com.hbx.campusconnect.pojo;

//import jakarta.validation.constraints.NotBlank;
//import jakarta.validation.constraints.Size;
//import org.springframework.validation.annotation.Validated;
//import org.springframework.web.bind.annotation.RequestParam;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {
    @NotBlank(message = "Username cannot be empty")
    @Size(min = 3, max = 16, message = "The length of the username must be between 3 and 16")
    private String username;
    @NotBlank(message = "Password cannot be blank")
    @Size(min = 6, message = "The length of password must be at least 6")
    private String password;
}
