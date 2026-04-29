package com.hbx.campusconnect.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostAuditRecord {
    private Integer id;
    private Integer postId;
    private Integer adminId;
    private PostAduit.PostAuditAction action;
    private String reason;
    private LocalDateTime createdAt;

    private String title;

    private String username;
    private String userAvatar;

    private String adminName;
    private String adminAvatar;
}
