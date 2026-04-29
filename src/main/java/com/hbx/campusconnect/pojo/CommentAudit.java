package com.hbx.campusconnect.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentAudit {
    private Integer id;
    private Integer commentId;
    private String commentContent;
    private Integer adminId;
    private String action;
    private String reason;
    private LocalDateTime createdAt;
}
