package com.hbx.campusconnect.pojo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReplyCommentRequest {

    @NotBlank(message = "content cannot be empty")
    private String content;

    @NotNull(message = "parentId cannot be null")
    private Integer parentId;

    @NotNull(message = "replyToCommentId cannot be null")
    private Integer replyToCommentId;
}
