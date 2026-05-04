package com.hbx.campusconnect.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.hbx.campusconnect.pojo.*;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.io.IOException;
import java.util.ArrayList;

public interface PostService {
    // request to add a new post
    void requestAddPost(Post post) throws JsonProcessingException;

    // get all read only post
    PageBean<PostList> getAllReadPosts(GetPostRequest postData);

    // get all posts
    PageBean<PostList> getAllPosts(GetOwnPostRequest postData);

    // delete post with id
    void deletePost(@NotNull Integer id, String reason) throws JsonProcessingException;

    // approve post by id
    void approvePost(@NotNull Integer postId) throws MessagingException, IOException;

    // reject post by id
    void rejectPost(PostAduit postAduit) throws MessagingException, IOException;

    // participate / like the post
    void participatePost(@NotNull(message = "post id cannot be null or empty") Integer postId);

    // withdraw / unlike the post
    void withdrawPost(@NotNull(message = "post id cannot be null") Integer postId);

    // get all user info that liked given post
    ArrayList<UserBasicInfo> getAllUserLikedPost(@NotNull(message = "Post id cannot be null") Integer postId);
}
