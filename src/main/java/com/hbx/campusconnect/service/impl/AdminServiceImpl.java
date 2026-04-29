package com.hbx.campusconnect.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.hbx.campusconnect.mapper.AdminMapper;
import com.hbx.campusconnect.pojo.*;
import com.hbx.campusconnect.service.AdminService;
import com.hbx.campusconnect.utils.ThreadLocalUtil;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class AdminServiceImpl implements AdminService {

    @Autowired
    private AdminMapper adminMapper;

    @Override
    public PageBean<User> getAllUserInfo(GetAllUserInfoRequest allUserInfoRequest) {
        if (!this.isAdmin()) {
            throw new RuntimeException("You have no right to get user info");
        }
        PageBean<User> pb = new PageBean<>();
        PageHelper.startPage(allUserInfoRequest.getPageNum(), allUserInfoRequest.getPageSize());
        List<User> allUserInfoList = adminMapper.getAllUserInfo(allUserInfoRequest);
        Page<User> page = (Page<User>) allUserInfoList;
        pb.setTotal(page.getTotal());
        pb.setItems(page.getResult());
        return pb;
    }

    @Override
    public PageBean<PostList> getAllPostInfo(GetAllPostInfoRequest allPostInfoRequest) {
        if (!this.isAdmin()) {
            throw new RuntimeException("You have no right to get post info");
        }
        PageBean<PostList> pb = new PageBean<>();
        PageHelper.startPage(allPostInfoRequest.getPageNum(), allPostInfoRequest.getPageSize());
        List<PostList> allPostInfoList = adminMapper.getAllPostInfo(allPostInfoRequest);
        Page<PostList> page = (Page<PostList>) allPostInfoList;
        pb.setTotal(page.getTotal());
        pb.setItems(page.getResult());
        return pb;
    }

    private boolean isAdmin() {
        Map<String, Object> claims = ThreadLocalUtil.get();
        String currentUserRole = (String) claims.get("role");
        return currentUserRole.equals("ADMIN");
    }
}
