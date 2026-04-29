package com.hbx.campusconnect.pojo;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RetrieveUsernameRequest {
    @NotBlank(message = "Email cannot be blank")
    private String email;
    @NotBlank(message = "Token cannot be blank")
    private String token;
}
