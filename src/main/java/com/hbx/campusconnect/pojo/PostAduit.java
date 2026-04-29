package com.hbx.campusconnect.pojo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostAduit {
    private Integer id;
    @NotNull
    private Integer postId;
    private Integer adminId;
    private PostAuditAction action;
    @NotBlank
    private String reason;
    private LocalDateTime createdAt;

    public enum PostAuditAction {
        APPROVED, REJECTED, DELETED
    }
}
