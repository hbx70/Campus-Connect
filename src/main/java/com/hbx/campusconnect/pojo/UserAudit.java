package com.hbx.campusconnect.pojo;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserAudit {
    private Integer id;
    private Integer userId;
    private Integer adminId;
    private User.Status action;
    private String reason;
    private LocalDateTime createdAt;
}
