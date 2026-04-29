package com.hbx.campusconnect.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostList {
    private Integer id;
    private Integer userId;
    private String title;
    private String content;
    private Integer comments;
    private Integer participants;
    private Type type;
    private String coverImage;
    private Status status;
    private LocalDateTime createdAt;
    private LocalDateTime approvedAt;

    private String username;
    private String userAvatar;
    private User.Role role;
    private User.Status userStatus;

    public enum Type {
        ACTIVITY, FORUM
    }

    public enum Status {
        PENDING, APPROVED, REJECTED
    }

}