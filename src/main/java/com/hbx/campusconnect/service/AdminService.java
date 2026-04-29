package com.hbx.campusconnect.service;

import com.hbx.campusconnect.pojo.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.ArrayList;

public interface AdminService {
    // get all user info
    PageBean<User> getAllUserInfo(GetAllUserInfoRequest allUserInfoRequest);

    // get all post info
    PageBean<PostList> getAllPostInfo(GetAllPostInfoRequest allPostInfoRequest);
}
