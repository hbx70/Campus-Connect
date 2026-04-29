package com.hbx.campusconnect.mapper;

import com.hbx.campusconnect.pojo.Comment;
import com.hbx.campusconnect.pojo.CommentAudit;
import com.hbx.campusconnect.pojo.CommentList;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.ArrayList;
import java.util.List;

@Mapper
public interface CommentMapper {
    // leave a comment by a user
    @Insert("insert into comments (post_id, user_id, parent_id, reply_to_comment_id, content, likes, is_deleted, created_at) values (#{postId}, #{userId}, #{parentId}, #{replyToCommentId}, #{content}, #{likes}, #{isDeleted}, #{createdAt})")
    int leaveComment(Comment commentData);

    // get root comments and order by
    @Select("select c.*, u.username, u.user_avatar, u.role, u.user_status from comments c left join users u on c.user_id = u.id where c.post_id = #{postId} and c.parent_id is null order by ${orderBy}")
    ArrayList<CommentList> getParentComments(Integer postId, String orderBy);

    // find comment by id
    @Select("select * from comments where id=#{postId}")
    Comment findCommentById(Integer postId);

    // get all reply comment chain
    @Select("select c.*, u.username, u.user_avatar, u.role, u.user_status, rtu.username as reply_to_username from comments c " +
            "left join users u on c.user_id = u.id " +
            "left join comments rtc on c.reply_to_comment_id = rtc.id " +
            "left join users rtu on rtc.user_id = rtu.id " +
            "where c.parent_id=#{parentId}")
    List<CommentList> getAllReplyComments(Integer parentId);

    // soft delete a comment by id
    @Update("update comments set is_deleted=1, content='[deleted comment]' where id=#{commentId} and is_deleted=0")
    int softDelete(Integer commentId);

    // update comment likes
    @Update("update comments set likes=likes+1 where id=#{commentId}")
    void updateLikes(Integer commentId);

    // update comment unlikes
    @Update("update comments set likes=likes-1 where id=#{commentId}")
    void updateUnlikes(Integer commentId);

    // update parent comments
    @Update("update comments set parent_comments=parent_comments+1 where id=#{id}")
    void updateParentComment(Integer id);

    // get all comment id that current user leaved
    @Select("select id from comments where user_id=#{currentUserId}")
    ArrayList<Integer> getAllLeavedCommentId(Integer currentUserId);
}
