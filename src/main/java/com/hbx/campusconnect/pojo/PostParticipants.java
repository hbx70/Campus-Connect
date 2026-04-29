package com.hbx.campusconnect.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class PostParticipants {
    private Integer id;
    private Integer postId;
    private Integer userId;
    private LocalDateTime createdAt;
}
