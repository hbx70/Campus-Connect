package com.hbx.campusconnect.mapper;

import com.hbx.campusconnect.pojo.*;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Mapper
public interface PostMapper {
    // request to add a new post
    @Insert("insert into posts (user_id, title, content, type, cover_image, status, created_at) values (#{post.userId}, #{post.title}, #{post.content}, #{post.type}, #{coverImgJson}, #{post.status}, #{post.createdAt})")
    void requestAddPost(Post post, String coverImgJson);

    // get all post
    List<PostList> getAllPost(GetPostRequest postData);

    // get all own post
    List<PostList> getAllOwnPost(GetOwnPostRequest postData);

    // find post by id
    @Select("select * from posts where id=#{id}")
    Post findPostById(Integer id);

    // delete post by id
    @Delete("delete from posts where id=#{id}")
    void deletePost(Integer id);

    // approve post by id
    @Update("update posts set status=#{status}, approved_at=#{operateTime} where id=#{postId}")
    void updatePostStatus (Integer postId, LocalDateTime operateTime, PostAduit.PostAuditAction status);

    // update number of comments (add)
    @Update("update posts set comments=comments+1 where id=#{id}")
    void leaveNewComment(Integer id);

    // increase number of participants
    @Update("update posts set participants=participants+1 where id=#{postId}")
    int increaseParticipants(Integer postId);

    // decrease number of participants
    @Update("update posts set participants=participants-1 where id=#{postId}")
    int decreaseParticipants(Integer postId);

    // get all post id that current user owned
    @Select("select id from posts where user_id=#{currentUserId}")
    ArrayList<Integer> getAllOwnedPost(Integer currentUserId);

    // find all post cover images by id
    @Select("select cover_image from posts where id=#{postId}")
    String findPostCoverImageById(Integer postId);
}
