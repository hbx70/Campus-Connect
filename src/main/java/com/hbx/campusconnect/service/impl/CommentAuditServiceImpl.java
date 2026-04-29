package com.hbx.campusconnect.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.hbx.campusconnect.exception.ForbiddenException;
import com.hbx.campusconnect.mapper.CommentAuditMapper;
import com.hbx.campusconnect.mapper.CommentMapper;
import com.hbx.campusconnect.pojo.CommentAudit;
import com.hbx.campusconnect.pojo.CommentAuditRecord;
import com.hbx.campusconnect.pojo.PageBean;
import com.hbx.campusconnect.pojo.PostList;
import com.hbx.campusconnect.service.CommentAuditService;
import com.hbx.campusconnect.utils.ThreadLocalUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class CommentAuditServiceImpl implements CommentAuditService {

    @Autowired
    private CommentAuditMapper commentAuditMapper;

    @Override
    public void recordActionOnComment(CommentAudit newCommentAudit) {
        commentAuditMapper.recordActionOnComment(newCommentAudit);
    }

    @Override
    public PageBean<CommentAuditRecord> getCommentAudit(Integer pageNum, Integer pageSize, String order, String adminName, String content) {
        Map<String, Object> claims = ThreadLocalUtil.get();
        String currentRole = (String) claims.get("role");
        if (!("ADMIN").equals(currentRole)) {
            throw new ForbiddenException("you have no right to get the audit");
        }

        PageBean<CommentAuditRecord> pb = new PageBean<>();
        PageHelper.startPage(pageNum, pageSize);
        List<CommentAuditRecord> allCommentAuditRecords = commentAuditMapper.getCommentAudit(order, adminName, content);
        Page<CommentAuditRecord> page = (Page<CommentAuditRecord>) allCommentAuditRecords;
        pb.setTotal(page.getTotal());
        pb.setItems(page.getResult());
        return pb;
    }
}
