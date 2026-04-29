package com.hbx.campusconnect.controller;

import com.hbx.campusconnect.pojo.*;
import com.hbx.campusconnect.service.AdminService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

@RestController
@RequestMapping("/admin")
@Validated
public class AdminController {

    @Autowired
    private AdminService adminService;

    @GetMapping("/user")
    public Result<PageBean<User>> getAllUserInfo(GetAllUserInfoRequest allUserInfoRequest) {
        return Result.success(adminService.getAllUserInfo(allUserInfoRequest));
    }

    @GetMapping("/post")
    public Result<PageBean<PostList>> getAllPostInfo(GetAllPostInfoRequest allPostInfoRequest) {
        return Result.success(adminService.getAllPostInfo(allPostInfoRequest));
    }
}
