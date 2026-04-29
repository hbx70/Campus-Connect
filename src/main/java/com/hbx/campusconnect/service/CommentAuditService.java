package com.hbx.campusconnect.service;

import com.hbx.campusconnect.mapper.CommentAuditMapper;
import com.hbx.campusconnect.pojo.CommentAudit;
import com.hbx.campusconnect.pojo.CommentAuditRecord;
import com.hbx.campusconnect.pojo.PageBean;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;

public interface CommentAuditService {

    // record admin action on comment
    void recordActionOnComment(CommentAudit newCommentAudit);

    // get all comment audit
    PageBean<CommentAuditRecord> getCommentAudit(@Valid @NotNull Integer pageNum, @NotNull Integer pageSize, @NotBlank(message = "order cannot be empty") String order, String adminName, String content);
}
