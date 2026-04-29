package com.hbx.campusconnect.mapper;

import com.hbx.campusconnect.pojo.CommentLike;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.ArrayList;

@Mapper
public interface CommentLikeMapper {
    // find comment_like by commentId and UserId
    @Select("select * from comment_likes where comment_id=#{commentId} and user_id=#{loginUserId}")
    CommentLike findCommentLikeByCommentIdAndUserId(Integer commentId, Integer loginUserId);

    // insert a new comment like
    @Insert("insert into comment_likes (comment_id, user_id, created_at) values (#{commentId}, #{userId}, #{createdAt})")
    int insertCommentLike(CommentLike newCommentLike);

    // delete a comment like
    @Delete("delete from comment_likes where comment_id=#{commentId} and user_id=#{loginUserId}")
    int deleteCommentLike(Integer commentId, Integer loginUserId);

    // get all comment id that user liked
    @Select("select comment_id from comment_likes where user_id=#{currentUserId}")
    ArrayList<Integer> getAllCommentLiked(Integer currentUserId);

    @Delete("delete from comment_likes WHERE comment_id IN (SELECT id FROM comments WHERE post_id = #{id})")
    void deleteCommentLikeByPostId(Integer id);
}
