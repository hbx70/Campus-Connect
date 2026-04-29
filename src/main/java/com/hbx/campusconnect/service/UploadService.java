package com.hbx.campusconnect.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.hbx.campusconnect.pojo.Post;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface UploadService {
    // upload file into R2 Object Storage, return URL
    String upload(@NotNull MultipartFile file) throws IOException;

    // delete file from R2 Object Storage
    void delete(String url);

    // save temp image into database
    void saveTempImage(String url);

    // remove all image of post that has been deleted
    void removePostImage(Integer postId) throws JsonProcessingException;
}
