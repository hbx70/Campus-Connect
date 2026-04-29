package com.hbx.campusconnect.service;

import com.hbx.campusconnect.pojo.PageBean;
import com.hbx.campusconnect.pojo.User;
import com.hbx.campusconnect.pojo.UserAudit;
import com.hbx.campusconnect.pojo.UserAuditRecord;
import jakarta.validation.constraints.NotNull;

import java.util.ArrayList;

public interface UserAuditService {
    // record action of ban user
    void recordBanUser(Integer userId, Integer adminId, String reason);

    // record action of active user
    void recordActiveUser(Integer userId, Integer adminId);

    // get user audit
    PageBean<UserAuditRecord> getUserAudit(Integer pageNum, Integer pageSize, String order, String adminName, User.Status action);
}
