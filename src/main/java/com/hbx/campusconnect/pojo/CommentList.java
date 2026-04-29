package com.hbx.campusconnect.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentList {
    private Integer id;
    private Integer postId;
    private Integer userId;
    private Integer parentId;
    private Integer replyToCommentId;
    private String content;
    private Integer parentComments;
    private Integer likes;
    private Integer isDeleted;
    private LocalDateTime createdAt;

    private String username;
    private String userAvatar;
    private User.Role role;
    private User.Status userStatus;

    private String replyToUsername;
}
