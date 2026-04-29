package com.hbx.campusconnect.pojo;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class RequestPost {
    private Integer id;
    //    @NotBlank(message = "user id cannot be empty")
    private Integer userId;
    @NotBlank(message = "title cannot be empty")
    private String title;
    @NotBlank(message = "content cannot be empty")
    private String content;
    private Integer comments;
    private Integer participants;
    @NotNull(message = "type cannot be empty")
    private Type type;
    @NotNull(message = "images cannot be null")
    private ArrayList<String> coverImage;
    private Status status;
    private LocalDateTime createdAt;
    private LocalDateTime approvedAt;

    public enum Type {
        ACTIVITY, FORUM
    }

    public enum Status {
        PENDING, APPROVED, REJECTED
    }
}