package com.hbx.campusconnect.controller;

import com.github.pagehelper.Page;
import com.hbx.campusconnect.pojo.*;
import com.hbx.campusconnect.service.CommentAuditService;
import com.hbx.campusconnect.service.PostAuditService;
import com.hbx.campusconnect.service.UserAuditService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;

@RestController
@RequestMapping("/audit")
@Validated
public class AuditController {

    @Autowired
    private PostAuditService postAuditService;

    @Autowired
    private UserAuditService userAuditService;

    @Autowired
    private CommentAuditService commentAuditService;

    @GetMapping("/post")
    public Result<PageBean<PostAuditRecord>> getPostAudit(@Valid @NotNull Integer pageNum, @NotNull Integer pageSize, @NotBlank(message = "order cannot be null") String order, @RequestParam(required = false) PostAduit.PostAuditAction action, @RequestParam(required = false) String adminName) {
        PageBean<PostAuditRecord> pb = postAuditService.getPostAudit(pageNum, pageSize, order, action, adminName);
        return Result.success(pb);
    }

    @GetMapping("/user")
    public Result<PageBean<UserAuditRecord>> getUserAudit(@Valid @NotNull Integer pageNum, @NotNull Integer pageSize, @NotBlank(message = "order cannot be blank") String order, @RequestParam(required = false) String adminName, @RequestParam(required = false) User.Status action) {
        PageBean<UserAuditRecord> pb = userAuditService.getUserAudit(pageNum, pageSize, order, adminName, action);
        return Result.success(pb);
    }

    @GetMapping("/comment")
    public Result<PageBean<CommentAuditRecord>> getCommentAudit(@Valid @NotNull Integer pageNum, @NotNull Integer pageSize, @NotBlank(message = "order cannot be empty") String order, @RequestParam(required = false) String adminName, @RequestParam(required = false) String content) {
        PageBean<CommentAuditRecord> pb = commentAuditService.getCommentAudit(pageNum, pageSize, order, adminName, content);
        return Result.success(pb);
    }
}
