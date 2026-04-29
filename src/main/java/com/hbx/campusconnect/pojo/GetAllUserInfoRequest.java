package com.hbx.campusconnect.pojo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetAllUserInfoRequest {
    @NotNull
    private Integer pageNum;
    @NotNull
    private Integer pageSize;
    @NotBlank(message = "order cannot be blank")
    private String order;
    private User.Role role;
    private User.Status status;
    private String username;
}
