package com.hbx.campusconnect.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserAuditRecord {
    private Integer id;
    private Integer userId;
    private String username;
    private String email;
    private User.Role role;
    private String userAvatar;
    private LocalDateTime userCreatedAt;
    private User.Status userStatus;
    private Integer adminId;
    private String reason;
    private LocalDateTime opsCreatedAt;
    private String adminName;
    private String adminAvatar;
    private User.Status action;
}
