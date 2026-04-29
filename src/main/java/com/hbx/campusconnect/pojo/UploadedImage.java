package com.hbx.campusconnect.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UploadedImage {
    private Integer id;
    private String url;
    private Integer userId;
    private Integer postId;
    private Status status;
    private LocalDateTime createdAt;

    public enum Status {
        TEMP, USED
    }
}
