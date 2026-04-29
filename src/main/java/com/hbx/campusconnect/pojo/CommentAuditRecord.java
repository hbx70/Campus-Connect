package com.hbx.campusconnect.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentAuditRecord {
    private Integer id;
    private Integer commentId;
    private Integer adminId;
    private String commentContent;
    private String action;
    private String reason;
    private LocalDateTime createdAt;

    private Integer likes;

    private String username;
    private String userAvatar;

    private String adminName;
    private String adminAvatar;
}
