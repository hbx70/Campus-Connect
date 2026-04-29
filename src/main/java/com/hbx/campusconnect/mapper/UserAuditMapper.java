package com.hbx.campusconnect.mapper;

import com.hbx.campusconnect.pojo.User;
import com.hbx.campusconnect.pojo.UserAudit;
import com.hbx.campusconnect.pojo.UserAuditRecord;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

import java.util.ArrayList;
import java.util.List;

@Mapper
public interface UserAuditMapper {
    // record admin operation
    @Insert("insert into user_audit (user_id, admin_id, action, reason, created_at) values (#{userId}, #{adminId}, #{action}, #{reason}, #{createdAt})")
    void recordOperationUser(UserAudit userAudit);

    // get all user audit
    ArrayList<UserAuditRecord> getUserAudit(String order, String adminName, User.Status action);
}
