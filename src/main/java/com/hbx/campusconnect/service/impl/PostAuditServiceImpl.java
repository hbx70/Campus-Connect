package com.hbx.campusconnect.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.hbx.campusconnect.exception.ForbiddenException;
import com.hbx.campusconnect.mapper.PostAuditMapper;
import com.hbx.campusconnect.pojo.*;
import com.hbx.campusconnect.service.PostAuditService;
import com.hbx.campusconnect.utils.ThreadLocalUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class PostAuditServiceImpl implements PostAuditService {

    @Autowired
    private PostAuditMapper postAuditMapper;

    @Override
    public PageBean<PostAuditRecord> getPostAudit(Integer pageNum, Integer pageSize, String order, PostAduit.PostAuditAction action, String adminName) {
        Map<String, Object> claims = ThreadLocalUtil.get();
        String currentRole = (String) claims.get("role");
        if (!("ADMIN").equals(currentRole)) {
            throw new ForbiddenException("you have no right to get the audit");
        }
        PageBean<PostAuditRecord> pb = new PageBean<>();
        PageHelper.startPage(pageNum, pageSize);
        List<PostAuditRecord> postAuditRecords = postAuditMapper.getPostAudit(order, action, adminName);
        Page<PostAuditRecord> page = (Page<PostAuditRecord>) postAuditRecords;
        pb.setTotal(page.getTotal());
        pb.setItems(page.getResult());
        return pb;
    }
}
