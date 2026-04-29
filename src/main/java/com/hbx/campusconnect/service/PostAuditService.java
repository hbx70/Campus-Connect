package com.hbx.campusconnect.service;

import com.hbx.campusconnect.pojo.*;

import java.util.ArrayList;

public interface PostAuditService {
    // get post audit
    PageBean<PostAuditRecord> getPostAudit(Integer pageNum, Integer pageSize, String order, PostAduit.PostAuditAction action, String adminName);
}
