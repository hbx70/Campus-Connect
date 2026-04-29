package com.hbx.campusconnect.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private Integer id;
    private String username;
    private String email;
    private String password;
    private Role role;
    private String userAvatar;
    private Status userStatus;
    private LocalDateTime createdAt;

    public enum Role {
        USER, ADMIN
    }

    public enum Status {
        BANNED, ACTIVE
    }
}
