package com.hbx.campusconnect.mapper;

import com.hbx.campusconnect.pojo.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.apache.ibatis.annotations.Mapper;

import java.util.ArrayList;

@Mapper
public interface AdminMapper {
    // get all user info
    ArrayList<User> getAllUserInfo(GetAllUserInfoRequest getAllUserInfoRequest);

    // get all post info
    ArrayList<PostList> getAllPostInfo(GetAllPostInfoRequest allPostInfoRequest);
}
