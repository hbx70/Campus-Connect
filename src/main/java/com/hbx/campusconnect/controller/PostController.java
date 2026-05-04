package com.hbx.campusconnect.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.hbx.campusconnect.pojo.*;
import com.hbx.campusconnect.service.PostService;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;

@RestController
@RequestMapping("/post")
@Validated
public class PostController {

    @Autowired
    private PostService postService;

    @PostMapping
    public Result requestAddPost(@Valid @RequestBody Post post) throws JsonProcessingException {
        postService.requestAddPost(post);
        return Result.success();
    }

    @GetMapping("/read")
    public Result<PageBean<PostList>> getAllReadPosts(GetPostRequest postData) {
        PageBean<PostList> pb = postService.getAllReadPosts(postData);
        return Result.success(pb);
    }

    @GetMapping
    public Result<PageBean<PostList>> getAllPosts(GetOwnPostRequest postData) {
        PageBean<PostList> pb = postService.getAllPosts(postData);
        return Result.success(pb);
    }

    @DeleteMapping
    public Result deletePost(@NotNull Integer id, String reason) throws JsonProcessingException {
        postService.deletePost(id, reason);
        return Result.success();
    }

    @PatchMapping("/approve")
    public Result approvePost(@NotNull Integer postId) throws MessagingException, IOException {
        postService.approvePost(postId);
        return Result.success();
    }

    @PatchMapping("/reject")
    public Result rejectPost(@Valid @RequestBody PostAduit postAduit) throws MessagingException, IOException {
        postService.rejectPost(postAduit);
        return Result.success();
    }

    @PostMapping("/participate")
    public Result participatePost(@NotNull(message = "post id cannot be null") Integer postId) {
        postService.participatePost(postId);
        return Result.success();
    }

    @DeleteMapping("/withdraw")
    public Result withdrawPost(@NotNull(message = "post id cannot be null") Integer postId) {
        postService.withdrawPost(postId);
        return Result.success();
    }

    @GetMapping("/user/likes")
    public Result<ArrayList<UserBasicInfo>> getAllUserLikedPost(@NotNull(message = "Post id cannot be null") Integer postId) {
        ArrayList<UserBasicInfo> userBasicInfoArrayList = postService.getAllUserLikedPost(postId);
        return Result.success(userBasicInfoArrayList);
    }
}
