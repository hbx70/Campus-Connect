package com.hbx.campusconnect.mapper;

import com.hbx.campusconnect.pojo.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface UserMapper {
    // find user by username
    @Select("select * from users where username=#{username}")
    User findUserByUsername(String username);

    // add user
    @Insert("insert into users (username, email, password, role, user_avatar, user_status, created_at) values (#{username}, #{email}, #{password}, #{role}, #{userAvatar}, #{userStatus}, #{createdAt})")
    void register(User user);

    // find user by id
    @Select("select * from users where id=#{id}")
    User findUserById(Integer id);

    // find user by email
    @Select("select * from users where email=#{email}")
    User findUserByEmail(String email);

    // update user avatar
    @Update("update users set user_avatar=#{avatarURL} where id=#{userId}")
    void updateAvatar(String avatarURL, Integer userId);

    // ban user by id
    @Update("update users set user_status='BANNED' where id=#{userId} and user_status='ACTIVE'")
    int banUser(Integer userId);

    // active user by id
    @Update("update users set user_status='ACTIVE' where id=#{userId} and user_status='BANNED'")
    int activeUser(Integer userId);

    // update username
    @Update("update users set username=#{newUsername} where id=#{currUserId}")
    void updateUsername(Integer currUserId, String newUsername);

    // update user email
    @Update("update users set email=#{newEmail} where id=#{loginUserId}")
    void updateEmail(String newEmail, Integer loginUserId);

    // change password by email
    @Update("update users set password=#{encryptedNewPwd} where id=#{userId}")
    void updatePassword(String encryptedNewPwd, Integer userId);

}
