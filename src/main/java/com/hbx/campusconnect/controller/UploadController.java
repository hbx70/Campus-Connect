package com.hbx.campusconnect.controller;

import com.hbx.campusconnect.pojo.Result;
import com.hbx.campusconnect.service.UploadService;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;

@RestController
@RequestMapping("/upload")
@Validated
public class UploadController {

    @Autowired
    private UploadService uploadService;

    @PostMapping
    public Result<String> uploadImage(@NotNull @RequestParam("file") MultipartFile file) throws IOException {
        String url = uploadService.upload(file);
        uploadService.saveTempImage(url);
        return Result.success(url);
    }
}
