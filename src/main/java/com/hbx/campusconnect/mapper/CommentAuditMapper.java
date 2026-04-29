package com.hbx.campusconnect.mapper;

import com.hbx.campusconnect.pojo.CommentAudit;
import com.hbx.campusconnect.pojo.CommentAuditRecord;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

import java.util.ArrayList;

@Mapper
public interface CommentAuditMapper {
    // record admin action on comment
    @Insert("insert into comment_audit (comment_id, comment_content, admin_id, action, reason, created_at) values (#{commentId}, #{commentContent}, #{adminId}, #{action}, #{reason}, #{createdAt})")
    void recordActionOnComment(CommentAudit newCommentAudit);

    // get all comment audit record
    ArrayList<CommentAuditRecord> getCommentAudit(String order, String adminName, String content);
}
