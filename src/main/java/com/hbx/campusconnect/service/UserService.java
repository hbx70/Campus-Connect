package com.hbx.campusconnect.service;

import com.hbx.campusconnect.pojo.RegisterRequest;
import com.hbx.campusconnect.pojo.RetrieveUsernameRequest;
import com.hbx.campusconnect.pojo.User;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;

public interface UserService {
    // find user by given username
    User findUserByUsername(String username);

    // register user
    void register(RegisterRequest user);

    // find user by give id
    User findUserById(Integer id);

    // find user by email
    User findUserByEmail(@NotBlank(message = "email cannot be empty") @Email(message = "please enter correct email address") String email);

    // send OTP by email
    void sendEmailOTP(@Email @NotBlank String email) throws MessagingException;

    // upload user avatar
    String uploadAvatar(MultipartFile file) throws IOException;

    // update user avatar
    void updateAvatar(String avatarURL);

    // ban user by id
    void banUser(@NotNull Integer userId, @NotBlank String reason);

    // active user by id
    void activeUser(@NotNull Integer userId);

    // get email change code
    void sendEmailChangedCode(@NotBlank String email) throws MessagingException;

    // update user email checking
    void updateUserEmailCheck(@Valid @NotBlank(message = "email cannot be blank") String email);

    // update username
    void updateUsername(@NotBlank String newUsername);

    // update user email
    void updateUserEmail(@NotBlank String newEmail, @NotBlank @Size(min = 6, max = 6) String otp);

    // get change password code
    void sendAccountHelpOTP(@Email @NotBlank String email) throws MessagingException;

    // update user password
    void updateUserPassword(@NotNull String newPassword, @NotBlank String accHelpToken);

    // get all post that current user participate in;
    ArrayList<Integer> getAllParticipatePost();

    // get all comment that user liked
    ArrayList<Integer> getAllCommentLiked();

    // get all post id that current user owned
    ArrayList<Integer> getAllOwnedPost();

    // get all comment id that current user leaved
    ArrayList<Integer> getAllLeavedCommentId();

    // logout
    void logout(String token);

    // verify account help page email
    String verifyAccountHelp(@NotBlank String email, @NotBlank @Size(min = 6, max = 6) String otp);

    // retrieve username by email and token
    String retrieveUsername(String token);
}
