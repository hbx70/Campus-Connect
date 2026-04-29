package com.hbx.campusconnect.service;

import com.hbx.campusconnect.pojo.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.ArrayList;

public interface CommentService {
    // user leave a comment
    void leaveComment(Comment commentData);

    // get parent comments
    ArrayList<CommentList> getParentComments(@NotNull Integer postId, @NotNull Comment.OrderBy orderBy);

    // get child comments
    PageBean<CommentList> getAllReplyComments(@NotNull Integer pageNum, @NotNull Integer pageSize, @NotNull Integer parentId);

    // reply the comment
    void replyComment(ReplyCommentRequest replyCommentRequest);

    // soft delete a comment by id
    void softDelete(@NotNull Integer commentId, String reason);

    // user like a comment
    void likeComment(@NotNull Integer commentId);

    // user unlike a comment
    void unlikeComment(@NotNull Integer commentId);

    // restore a comment by id
//    void restoreComment(@NotNull Integer commentId);

    // comment owner and admin delete or restore a comment by id
//    void actionOnComment(@NotNull Integer commentId, @NotNull CommentAudit.Action action, String reason);
}
