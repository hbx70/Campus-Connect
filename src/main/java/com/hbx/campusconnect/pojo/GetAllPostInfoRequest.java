package com.hbx.campusconnect.pojo;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetAllPostInfoRequest {
    @NotNull
    private Integer pageNum;
    @NotNull
    private Integer pageSize;
    private Post.Type type;
    @NotBlank(message = "order cannot be blank")
    private String order;
    private Post.Status status;
    private String username;
}
