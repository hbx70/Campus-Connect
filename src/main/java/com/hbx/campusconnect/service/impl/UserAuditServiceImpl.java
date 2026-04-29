package com.hbx.campusconnect.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.hbx.campusconnect.exception.ForbiddenException;
import com.hbx.campusconnect.mapper.UserAuditMapper;
import com.hbx.campusconnect.pojo.*;
import com.hbx.campusconnect.service.UserAuditService;
import com.hbx.campusconnect.utils.ThreadLocalUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class UserAuditServiceImpl implements UserAuditService {

    @Autowired
    private UserAuditMapper userAuditMapper;

    @Override
    public void recordBanUser(Integer userId, Integer adminId, String reason) {
        UserAudit userAudit = new UserAudit();
        userAudit.setUserId(userId);
        userAudit.setAdminId(adminId);
        userAudit.setAction(User.Status.BANNED);
        userAudit.setReason(reason);
        userAudit.setCreatedAt(LocalDateTime.now());
        userAuditMapper.recordOperationUser(userAudit);
    }

    @Override
    public void recordActiveUser(Integer userId, Integer adminId) {
        UserAudit userAudit = new UserAudit();
        userAudit.setUserId(userId);
        userAudit.setAdminId(adminId);
        userAudit.setAction(User.Status.ACTIVE);
        userAudit.setReason("active");
        userAudit.setCreatedAt(LocalDateTime.now());
        userAuditMapper.recordOperationUser(userAudit);
    }

    @Override
    public PageBean<UserAuditRecord> getUserAudit(Integer pageNum, Integer pageSize, String order, String adminName, User.Status action) {
        Map<String, Object> claims = ThreadLocalUtil.get();
        String currentRole = (String) claims.get("role");
        if (!("ADMIN").equals(currentRole)) {
            throw new ForbiddenException("you have no right to get the audit");
        }
        PageBean<UserAuditRecord> pb = new PageBean<>();
        PageHelper.startPage(pageNum, pageSize);
        List<UserAuditRecord> allUserAuditList = userAuditMapper.getUserAudit(order, adminName, action);
        Page<UserAuditRecord> page = (Page<UserAuditRecord>) allUserAuditList;
        pb.setTotal(page.getTotal());
        pb.setItems(page.getResult());
        return pb;
    }
}
