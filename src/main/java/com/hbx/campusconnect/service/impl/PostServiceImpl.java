package com.hbx.campusconnect.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.hbx.campusconnect.exception.ForbiddenException;
import com.hbx.campusconnect.mapper.*;
import com.hbx.campusconnect.pojo.*;
import com.hbx.campusconnect.service.EmailService;
import com.hbx.campusconnect.service.PostService;
import com.hbx.campusconnect.service.UploadService;
import com.hbx.campusconnect.service.UserService;
import com.hbx.campusconnect.utils.ThreadLocalUtil;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class PostServiceImpl implements PostService {

    @Autowired
    private PostMapper postMapper;

    @Autowired
    private PostAuditMapper postAuditMapper;

    @Autowired
    private PostParticipateMapper postParticipateMapper;

    @Autowired
    private EmailService emailService;
    @Autowired
    private UserService userService;
    @Autowired
    private UploadMapper uploadMapper;
    @Autowired
    private CommentLikeMapper commentLikeMapper;
    @Autowired
    private CommentMapper commentMapper;
    @Autowired
    private UploadService uploadService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void requestAddPost(Post post) throws JsonProcessingException {
        if (post.getType() == Post.Type.ACTIVITY && post.getCoverImage().isEmpty()) {
            throw new RuntimeException("At least one cover image need to be uploaded");
        }
        if (post.getCoverImage().size() > 9) {
            throw new RuntimeException("Maximum 9 image");
        }
        PolicyFactory policy = new HtmlPolicyBuilder()
                .allowElements("h1", "h2", "h3", "p", "strong", "em", "s", "u", "span", "pre", "code", "br", "ol", "ul", "li", "a", "sub", "sup")
                .allowAttributes("href", "rel", "target").onElements("a")
                .allowUrlProtocols("http", "https", "mailto")
                .toFactory();

        Map<String, Object> claims = ThreadLocalUtil.get();
        Integer userId = (Integer) claims.get("id");
        post.setUserId(userId);
        post.setContent(policy.sanitize(post.getContent()));
        post.setComments(0);
        post.setParticipants(0);
        post.setStatus(Post.Status.PENDING);
        post.setCreatedAt(LocalDateTime.now());
        ObjectMapper mapper = new ObjectMapper();
        String coverImgJson = mapper.writeValueAsString(post.getCoverImage());
        postMapper.requestAddPost(post, coverImgJson);
        if (!post.getCoverImage().isEmpty()) {
            uploadMapper.markAsUsed(post.getCoverImage());
        }
    }

    @Override
    public PageBean<PostList> getAllReadPosts(GetPostRequest postData) {
        PageBean<PostList> pb = new PageBean<>();
        PageHelper.startPage(postData.getPageNum(), postData.getPageSize());
        postData.setUserId(null);
        postData.setStatus(Post.Status.APPROVED);
        List<PostList> posts = postMapper.getAllPost(postData);
        Page<PostList> page = (Page<PostList>) posts;
        pb.setTotal(page.getTotal());
        pb.setItems(page.getResult());
        return pb;
    }

    @Override
    public PageBean<PostList> getAllPosts(GetOwnPostRequest postData) {
        // 1. create PageBean object
        PageBean<PostList> pb = new PageBean<>();

        // 2. start PageHelper
        PageHelper.startPage(postData.getPageNum(), postData.getPageSize());

        Map<String, Object> claims = ThreadLocalUtil.get();
        String currentUserRole = (String) claims.get("role");
        Integer loginUserId = (Integer) claims.get("id");
        boolean isUser = currentUserRole.equals("USER");
        if (isUser) {
            if (postData.getStatus() != Post.Status.APPROVED) {
                // user can only get own post if the post did not approve
                postData.setUserId(loginUserId);
            } else {
                // get all approved posts if userId is null
                postData.setUserId(postData.getUserId() == null ? null : loginUserId);
            }
        } else {
            postData.setUserId(postData.getUserId() == null ? null : loginUserId);
        }
        List<PostList> posts = postMapper.getAllOwnPost(postData);
        Page<PostList> page = (Page<PostList>) posts;
        pb.setTotal(page.getTotal());
        pb.setItems(page.getResult());
        return pb;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePost(Integer id, String reason) throws JsonProcessingException {
        Post post = postMapper.findPostById(id);
        if (post == null) {
            throw new RuntimeException("post not found");
        }
        Map<String, Object> claims = ThreadLocalUtil.get();
        String currentRole = (String) claims.get("role");
        Integer loginUserId = (Integer) claims.get("id");
        Integer creatorUserId = post.getUserId();
        boolean isAdmin = currentRole.equals("ADMIN");
        boolean isPostOwner = Objects.equals(loginUserId, creatorUserId);
        if (!isAdmin && !isPostOwner) {
            throw new ForbiddenException("you can only delete your own post");
        }
        if (isAdmin) {
            PostAduit postAduit = new PostAduit();
            postAduit.setPostId(id);
            postAduit.setAdminId(loginUserId);
            postAduit.setAction(PostAduit.PostAuditAction.DELETED);
            postAduit.setReason(reason == null ? "The post violated the community's requirements" : reason);
            postAduit.setCreatedAt(LocalDateTime.now());
            postAuditMapper.recordActionOnPost(postAduit);
        }
        uploadService.removePostImage(post.getId());
        commentLikeMapper.deleteCommentLikeByPostId(id);
//        commentMapper.deleteCommentByPostId(id);
        postMapper.deletePost(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approvePost(Integer postId) throws MessagingException, IOException {
        this.updatePostStatus(postId, PostAduit.PostAuditAction.APPROVED, "approved");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rejectPost(PostAduit postAduit) throws MessagingException, IOException {
        this.updatePostStatus(postAduit.getPostId(), PostAduit.PostAuditAction.REJECTED, postAduit.getReason());
    }

    private void updatePostStatus(Integer postId, PostAduit.PostAuditAction status, String reason) throws MessagingException, IOException {
        Post post = postMapper.findPostById(postId);
        if (post == null || post.getStatus().name().equals(status.name())) {
            throw new RuntimeException("post dose not exist or has been " + status.name());
        }
        Map<String, Object> claims = ThreadLocalUtil.get();
        String role = (String) claims.get("role");
        if (!role.equals("ADMIN")) {
            // not admin, no right to operate
            throw new ForbiddenException("you have not right to update post status");
        }
        LocalDateTime updateTime = LocalDateTime.now();
        postMapper.updatePostStatus(postId, updateTime, status);
        Integer adminId = (Integer) claims.get("id");

        PostAduit postAduit = new PostAduit();
        postAduit.setPostId(postId);
        postAduit.setAdminId(adminId);
        postAduit.setAction(status);
        postAduit.setReason(reason);
        postAduit.setCreatedAt(updateTime);
        postAuditMapper.recordActionOnPost(postAduit);

        // email notice
        User user = userService.findUserById(post.getUserId());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        emailService.sendNoticeEmailBuilder(user.getEmail(), user.getUsername(), post.getTitle(), status.name(), updateTime.format(formatter), reason);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void participatePost(Integer postId) {
        Map<String, Object> map = ThreadLocalUtil.get();
        Integer loginUserId = (Integer) map.get("id");
        if (isParticipateExist(loginUserId, postId)) {
            if (postMapper.findPostById(postId).getType() == Post.Type.FORUM) {
                throw new RuntimeException("You have already liked this forum");
            }
            throw new RuntimeException("You've already participated in this activity");
        }
        PostParticipants newPostParticipants = new PostParticipants();
        newPostParticipants.setPostId(postId);
        newPostParticipants.setUserId(loginUserId);
        newPostParticipants.setCreatedAt(LocalDateTime.now());
        int recordParticipantsRow = postParticipateMapper.insertPostParticipants(newPostParticipants);
        if (recordParticipantsRow != 1) {
            throw new RuntimeException("operation failure");
        }
        int updateParticipantsRow = postMapper.increaseParticipants(postId);
        if (updateParticipantsRow != 1) {
            throw new RuntimeException("operation failure");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void withdrawPost(Integer postId) {
        Map<String, Object> claim = ThreadLocalUtil.get();
        Integer currUserId = (Integer) claim.get("id");
        if (!isParticipateExist(currUserId, postId)) {
            if (postMapper.findPostById(postId).getType() == Post.Type.FORUM) {
                throw new RuntimeException("You have not liked this forum");
            }
            throw new RuntimeException("You have not participated in this activity");
        }
        int recordParticipantsRow = postParticipateMapper.deletePostParticipants(currUserId, postId);
        if (recordParticipantsRow != 1) {
            throw new RuntimeException("operation failure");
        }
        int updateParticipantsRow = postMapper.decreaseParticipants(postId);
        if (updateParticipantsRow != 1) {
            throw new RuntimeException("operation failure");
        }
    }

    @Override
    public ArrayList<UserBasicInfo> getAllUserLikedPost(Integer postId) {
        Post post = postMapper.findPostById(postId);
        if (post == null) {
            throw new RuntimeException("This post is not existed");
        }
        Map<String, Object> claims = ThreadLocalUtil.get();
        Integer currUserId = (Integer) claims.get("id");
        if (!Objects.equals(currUserId, post.getUserId())) {
            throw new RuntimeException("You are not the post owner");
        }
        return postParticipateMapper.findPostParInfoByPostId(postId);
    }

    private boolean isParticipateExist(Integer userId, Integer postId) {
        Post post = postMapper.findPostById(postId);
        if (post == null) {
            throw new RuntimeException("Post is not exist");
        }
        if (post.getStatus() != Post.Status.APPROVED) {
            throw new RuntimeException("Post has not been published");
        }
        PostParticipants postParticipants = postParticipateMapper.findPostParByPostIdAndUserId(postId, userId);
        return postParticipants != null;
    }
}
