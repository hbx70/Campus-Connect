package com.hbx.campusconnect.pojo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetOwnPostRequest {
    @NotNull(message = "page number cannot be empty")
    private Integer pageNum;
    @NotNull(message = "page size cannot be empty")
    private Integer pageSize;
    private Integer userId;
    @NotBlank(message = "order cannot be blank")
    private String order;
    @NotNull(message = "post type can only be ACTIVITY and FORUM")
    private Post.Type type;
    @NotNull(message = "post status can only be PENDING, APPROVED, and REJECTED")
    private Post.Status status;
    private String title;
}