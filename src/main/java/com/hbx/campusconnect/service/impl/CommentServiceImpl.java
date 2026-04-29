package com.hbx.campusconnect.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.hbx.campusconnect.exception.ForbiddenException;
import com.hbx.campusconnect.mapper.CommentLikeMapper;
import com.hbx.campusconnect.mapper.CommentMapper;
import com.hbx.campusconnect.mapper.PostMapper;
import com.hbx.campusconnect.mapper.UserMapper;
import com.hbx.campusconnect.pojo.*;
import com.hbx.campusconnect.service.CommentAuditService;
import com.hbx.campusconnect.service.CommentService;
import com.hbx.campusconnect.utils.ThreadLocalUtil;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class CommentServiceImpl implements CommentService {

    @Autowired
    private PostMapper postMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private CommentAuditService commentAuditService;

    @Autowired
    private CommentLikeMapper commentLikeMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void leaveComment(Comment commentData) {
        Integer postId = commentData.getPostId();
        if (postMapper.findPostById(postId) == null) {
            throw new RuntimeException("post not found");
        }
        if (postMapper.findPostById(postId).getStatus() != Post.Status.APPROVED) {
            throw new RuntimeException("post has not been approved");
        }

        Map<String, Object> claims = ThreadLocalUtil.get();
        Integer loginUserId = (Integer) claims.get("id");
        commentData.setUserId(loginUserId);
        commentData.setParentId(null);
        commentData.setReplyToCommentId(null);
        commentData.setLikes(0);
        commentData.setParentComments(0);
        commentData.setIsDeleted(0);
        commentData.setCreatedAt(LocalDateTime.now());
        int row = commentMapper.leaveComment(commentData);
        if (row != 1) {
            throw new RuntimeException("insert failed");
        }
        postMapper.leaveNewComment(commentData.getPostId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void replyComment(ReplyCommentRequest replyCommentData) {
        // parentId make sure pagination effect and organize (where parent_id = #{parentId})
        // replyToCommentId make sure xxx reply @xxx, the chain of reply
        Comment parent = commentMapper.findCommentById(replyCommentData.getParentId());
        if (parent == null) {
            throw new RuntimeException("parent comment not found");
        }
        if (parent.getParentId() != null) {
            throw new RuntimeException("its parent comment must be the root comment");
        }

        Integer postId = parent.getPostId();
        Comment replyTo = commentMapper.findCommentById(replyCommentData.getReplyToCommentId());
        if (replyTo == null) {
            throw new RuntimeException("comment not found");
        }
        if (!postId.equals(replyTo.getPostId())) {
            throw new RuntimeException("replyTo comment not in same post");
        }

        boolean replyToIsParent = replyTo.getId().equals(parent.getId());
        boolean replyToIsChildOfParent = replyTo.getParentId() != null && replyTo.getParentId().equals(parent.getId());
        if (!(replyToIsParent || replyToIsChildOfParent)) {
            throw new RuntimeException("replyTo comment not in this thread");
        }

        Map<String, Object> claims = ThreadLocalUtil.get();
        Integer userId = (Integer) claims.get("id");

        Comment newReply = new Comment();
        newReply.setPostId(postId);
        newReply.setUserId(userId);
        newReply.setParentId(parent.getId());
        newReply.setReplyToCommentId(replyTo.getId());
        newReply.setContent(replyCommentData.getContent());
        newReply.setLikes(0);
        newReply.setIsDeleted(0);
        newReply.setCreatedAt(LocalDateTime.now());

        int rows = commentMapper.leaveComment(newReply);
        commentMapper.updateParentComment(parent.getId());
        if (rows != 1) {
            throw new RuntimeException("insert reply failed");
        }
        postMapper.leaveNewComment(postId);
    }

    @Override
    public ArrayList<CommentList> getParentComments(Integer postId, Comment.OrderBy orderBy) {
        Post post = postMapper.findPostById(postId);
        if (post == null) {
            throw new RuntimeException("post not found");
        }
        if (post.getStatus() != Post.Status.APPROVED) {
            throw new RuntimeException("post has not been approved");
        }
        ArrayList<CommentList> commentList = new ArrayList<>();
        if (orderBy == Comment.OrderBy.TIME) {
            commentList = commentMapper.getParentComments(postId, "c.created_at desc");
        } else {
            commentList = commentMapper.getParentComments(postId, "c.likes desc");
        }
        return commentList;
    }


    @Override
    public PageBean<CommentList> getAllReplyComments(Integer pageNum, Integer pageSize, Integer parentId) {
        Comment parent = commentMapper.findCommentById(parentId);
        if (parent == null) {
            throw new RuntimeException("comment not found");
        }
        if (parent.getParentId() != null) {
            throw new RuntimeException("comment is not root comment");
        }

        // 1. create PageBean object
        PageBean<CommentList> pb = new PageBean<>();

        // 2. start PageHelper
        PageHelper.startPage(pageNum, pageSize);

        List<CommentList> commentList = commentMapper.getAllReplyComments(parentId);

        Page<CommentList> page = (Page<CommentList>) commentList;

        pb.setTotal(page.getTotal());
        pb.setItems(page.getResult());
        return pb;
    }

//    @Override
//    public void actionOnComment(Integer commentId, CommentAudit.Action action, String reason) {
//        Comment comment = commentMapper.findCommentById(commentId);
//        if (comment == null) {
//            throw new RuntimeException("comment not found");
//        }
//        if (action.equals(CommentAudit.Action.DELETE)) {
//            if (comment.getIsDeleted() == 1) {
//                throw new RuntimeException("comment has been deleted");
//            }
//        } else {
//            if (comment.getIsDeleted() == 0) {
//                throw new RuntimeException("comment is active");
//            }
//        }
//        Map<String, Object> claims = ThreadLocalUtil.get();
//        Integer loginUserId = (Integer) claims.get("id");
//        String currentRole = (String) claims.get("role");
//        boolean isCommentOwner = Objects.equals(loginUserId, comment.getUserId());
//        boolean isAdmin = "ADMIN".equals(currentRole);
//        if (!isCommentOwner && !isAdmin) {
//            throw new ForbiddenException("you have no right to " + action.name() + " this comment");
//        }
//        int row = commentMapper.updateIsDeleted(commentId, action.equals(CommentAudit.Action.DELETE) ? 1 : 0);
//        if (row != 1) {
//            throw new RuntimeException(action.name() + " failed");
//        }
//        if (isAdmin) {
//            // record admin operation
//            CommentAudit newCommentAudit = new CommentAudit();
//            newCommentAudit.setCommentId(commentId);
//            newCommentAudit.setAdminId(loginUserId);
//            newCommentAudit.setAction(action);
//            newCommentAudit.setReason(reason);
//            newCommentAudit.setCreatedAt(LocalDateTime.now());
//            commentAuditService.recordActionOnComment(newCommentAudit);
//        }
//    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void softDelete(Integer commentId, String reason) {
        Comment comment = commentMapper.findCommentById(commentId);
        if (comment == null) {
            throw new RuntimeException("comment not found");
        }
        if (comment.getIsDeleted() == 1) {
            throw new RuntimeException("comment has been deleted");
        }
        Map<String, Object> claims = ThreadLocalUtil.get();
        Integer loginUserId = (Integer) claims.get("id");
        String currentRole = (String) claims.get("role");
        boolean isCommentOwner = Objects.equals(loginUserId, comment.getUserId());
        boolean isAdmin = "ADMIN".equals(currentRole);
        if (!isCommentOwner && !isAdmin) {
            throw new ForbiddenException("you have no right to delete this comment");
        }
        String preCommentContent = comment.getContent();
        int row = commentMapper.softDelete(commentId);
        if (row != 1) {
            throw new RuntimeException("soft delete failed");
        }
        if (isAdmin) {
            // record admin operation
            CommentAudit newCommentAudit = new CommentAudit();
            newCommentAudit.setCommentId(commentId);
            newCommentAudit.setCommentContent(preCommentContent);
            newCommentAudit.setAdminId(loginUserId);
            newCommentAudit.setAction("DELETE");
            newCommentAudit.setReason(reason);
            newCommentAudit.setCreatedAt(LocalDateTime.now());
            commentAuditService.recordActionOnComment(newCommentAudit);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void likeComment(Integer commentId) {
        Map<String, Object> claims = ThreadLocalUtil.get();
        Integer loginUserId = (Integer) claims.get("id");
        if (this.isCommentLikeExists(commentId, loginUserId)) {
            throw new RuntimeException("You've already liked this comment");
        }
        CommentLike newCommentLike = new CommentLike();
        newCommentLike.setCommentId(commentId);
        newCommentLike.setUserId(loginUserId);
        newCommentLike.setCreatedAt(LocalDateTime.now());
        int row = commentLikeMapper.insertCommentLike(newCommentLike);
        if (row != 1) {
            throw new RuntimeException("operation failure");
        }
        commentMapper.updateLikes(commentId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unlikeComment(Integer commentId) {
        Map<String, Object> claims = ThreadLocalUtil.get();
        Integer loginUserId = (Integer) claims.get("id");
        if (!this.isCommentLikeExists(commentId, loginUserId)) {
            throw new RuntimeException("You've already unliked this comment");
        }
        int row = commentLikeMapper.deleteCommentLike(commentId, loginUserId);
        if (row != 1) {
            throw new RuntimeException("operation failure");
        }
        commentMapper.updateUnlikes(commentId);
    }

    private boolean isCommentLikeExists(Integer commentId, Integer loginUserId) {
        Comment comment = commentMapper.findCommentById(commentId);
        if (comment == null) {
            throw new RuntimeException("comment not found");
        }
        if (comment.getIsDeleted() == 1) {
            throw new RuntimeException("this comment has been deleted");
        }
        CommentLike commentLike = commentLikeMapper.findCommentLikeByCommentIdAndUserId(commentId, loginUserId);
        return commentLike != null;
    }

    //    @Override
//    public void restoreComment(Integer commentId) {
//        Comment comment = commentMapper.findCommentById(commentId);
//        if (comment == null) {
//            throw new RuntimeException("comment not found");
//        }
//        if (comment.getIsDeleted() == 0) {
//            throw new RuntimeException("comment has been restored");
//        }
//        Map<String, Object> claims = ThreadLocalUtil.get();
//        Integer loginUserId = (Integer) claims.get("id");
//        String currentRole = (String) claims.get("role");
//        boolean isCommentOwner = Objects.equals(loginUserId, comment.getUserId());
//        boolean isAdmin = "ADMIN".equals(currentRole);
//        if (!isCommentOwner && !isAdmin) {
//            throw new ForbiddenException("you have no right to delete this comment");
//        }
//        int row = commentMapper.restoreComment(commentId);
//        if (row != 1) {
//            throw new RuntimeException("soft delete failed");
//        }
//        if (isAdmin) {
//            CommentAudit newCommentAudit = new CommentAudit();
//            newCommentAudit.setCommentId(commentId);
//            newCommentAudit.setAdminId(loginUserId);
//            newCommentAudit.setAction(CommentAudit.Action.RESTORE);
//            newCommentAudit.setReason("restore");
//            newCommentAudit.setCreatedAt(LocalDateTime.now());
//            commentAuditService.recordActionOnComment(newCommentAudit);
//        }
//    }
}
