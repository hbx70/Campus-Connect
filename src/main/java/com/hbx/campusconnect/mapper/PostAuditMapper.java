package com.hbx.campusconnect.mapper;

import com.hbx.campusconnect.pojo.Post;
import com.hbx.campusconnect.pojo.PostAduit;
import com.hbx.campusconnect.pojo.PostAuditRecord;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

import java.util.ArrayList;
import java.util.List;

@Mapper
public interface PostAuditMapper {
    // record approve post
    @Insert("insert into post_audit (post_id, admin_id, action, reason, created_at) values (#{postId}, #{adminId}, #{action}, #{reason}, #{createdAt})")
    void recordActionOnPost(PostAduit postAduit);

    // get post audit
    List<PostAuditRecord> getPostAudit(String order, PostAduit.PostAuditAction action, String adminName);
}
