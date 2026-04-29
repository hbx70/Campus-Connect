package com.hbx.campusconnect.mapper;

import com.hbx.campusconnect.pojo.PostParticipants;
import com.hbx.campusconnect.pojo.UserBasicInfo;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.ArrayList;

@Mapper
public interface PostParticipateMapper {
    // find post participate by postId and userId
    @Select("select * from post_participants where post_id=#{postId} and user_id=#{userId}")
    PostParticipants findPostParByPostIdAndUserId(Integer postId, Integer userId);

    // insert new post participate
    @Insert("insert into post_participants (post_id, user_id, created_at) values (#{postId}, #{userId}, #{createdAt})")
    int insertPostParticipants(PostParticipants newPostParticipants);

    // get all post that current user participate in
    @Select("select post_id from post_participants where user_id=#{currentUserId}")
    ArrayList<Integer> getAllParticipatePost(Integer currentUserId);

    // delete post participate
    @Delete("delete from post_participants where user_id=#{currUserId} and post_id=#{postId}")
    int deletePostParticipants(Integer currUserId, Integer postId);

    // get all user info that liked the given post
    @Select("select u.id, u.username, u.user_avatar from post_participants pp left join users u on pp.user_id = u.id where pp.post_id=#{postId} order by pp.created_at desc")
    ArrayList<UserBasicInfo> findPostParInfoByPostId(Integer postId);
}
