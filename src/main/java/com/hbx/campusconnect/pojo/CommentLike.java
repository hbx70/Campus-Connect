package com.hbx.campusconnect.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentLike {
    private Integer id;
    private Integer commentId;
    private Integer userId;
    private LocalDateTime createdAt;
}
