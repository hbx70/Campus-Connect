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
public class Comment {
    private Integer id;
    @NotNull(message = "post id cannot be null")
    private Integer postId;
    private Integer userId;
    private Integer parentId;
    private Integer replyToCommentId;
    @NotBlank(message = "content cannot be null")
    private String content;
    private Integer likes;
    private Integer parentComments;
    private Integer isDeleted;
    private LocalDateTime createdAt;

    public enum OrderBy {
        TIME, LIKES
    }
}
