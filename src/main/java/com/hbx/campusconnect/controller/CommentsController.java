package com.hbx.campusconnect.controller;

import com.hbx.campusconnect.pojo.*;
import com.hbx.campusconnect.service.CommentService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.apache.ibatis.annotations.Delete;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

@RestController
@RequestMapping("/comment")
@Validated
public class CommentsController {

    @Autowired
    private CommentService commentService;

    @PostMapping
    public Result leaveComment(@Valid @RequestBody Comment commentData) {
        commentService.leaveComment(commentData);
        return Result.success();
    }

    @PostMapping("reply")
    public Result replyComment(@Valid @RequestBody ReplyCommentRequest replyCommentRequest) {
        commentService.replyComment(replyCommentRequest);
        return Result.success();
    }

    @GetMapping("/root")
    public Result<ArrayList<CommentList>> getParentComments(@NotNull Integer postId, @NotNull Comment.OrderBy orderBy) {
        ArrayList<CommentList> commentList = commentService.getParentComments(postId, orderBy);
        return Result.success(commentList);
    }

    @GetMapping("/child")
    public Result<PageBean<CommentList>> getAllReplyComments(@NotNull Integer pageNum, @NotNull Integer pageSize, @NotNull Integer parentId) {
        PageBean<CommentList> pb = commentService.getAllReplyComments(pageNum, pageSize, parentId);
        return Result.success(pb);
    }

//    @PatchMapping
//    public Result actionOnComment(@NotNull Integer commentId, @NotNull CommentAudit.Action action, String reason) {
//        commentService.actionOnComment(commentId, action, reason);
//        return Result.success();
//    }

    @PatchMapping
    public Result softDeleteComment(@NotNull Integer commentId, String reason) {
        commentService.softDelete(commentId, reason);
        return Result.success();
    }

    @PostMapping("/like")
    public Result likeComment(@NotNull Integer commentId) {
        commentService.likeComment(commentId);
        return Result.success();
    }

    @DeleteMapping("unlike")
    public Result unlikeComment(@NotNull Integer commentId) {
        commentService.unlikeComment(commentId);
        return Result.success();
    }

//    @PatchMapping("/restore")
//    public Result restoreComment(@NotNull Integer commentId) {
//        commentService.restoreComment(commentId);
//        return Result.success();
//    }
}
