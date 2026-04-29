package com.hbx.campusconnect.controller;

import com.hbx.campusconnect.pojo.*;
import com.hbx.campusconnect.service.UserService;
import com.hbx.campusconnect.utils.JwtUtil;
import com.hbx.campusconnect.utils.Md5Util;
import com.hbx.campusconnect.utils.ThreadLocalUtil;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/user")
@Validated
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private S3Client s3Client;

    // send opt
    @PostMapping("/email/register")
    public Result<String> sendEmailOTP (@Validated @RequestParam(required = true) @Email @NotBlank String email) throws MessagingException {
        userService.sendEmailOTP(email);
        return Result.success("verification code sent");
    }

    @PostMapping("/email/change")
    public Result<String> sendEmailChangeOTP (@Validated @RequestParam(required = true) @Email @NotBlank String email) throws MessagingException {
        userService.sendEmailChangedCode(email);
        return Result.success("verification code sent");
    }

    @PostMapping("/email/account")
    public Result<String> sendAccountHelpOTP(@Validated @RequestParam @Email @NotBlank String email) throws MessagingException {
        userService.sendAccountHelpOTP(email);
        return Result.success("verification code sent");
    }

    @PostMapping("/register/check")
    public Result checkRegister(@Valid @NotBlank String username, @NotBlank String email) {
        User getUserByUsername = userService.findUserByUsername(username);
        if (getUserByUsername != null) {
            // username exists
            return Result.error("Username has been occupied");
        }
        User getUserByEmail = userService.findUserByEmail(email);
        if (getUserByEmail != null) {
            // email registered
            return Result.error("Email has been registered");
        }
        return Result.success();
    }

    // Register
    @PostMapping("/register")
    public Result register(@Validated @RequestBody RegisterRequest req) {
        String key = "email:register:otp:" + req.getEmail();
        String saved = stringRedisTemplate.opsForValue().get(key);
        if (saved == null) {
            return Result.error("Verification code expired or not sent, please click to send again");
        }
        if (!saved.equals(req.getOtp())) {
            return Result.error("Invalid verification code");
        }
        stringRedisTemplate.delete(key);

        // register
        userService.register(req);
        return Result.success();
    }

    // Login
    @PostMapping("/login")
    public Result<String> login(@RequestBody LoginRequest loginRequestData) {
        String username = loginRequestData.getUsername();
        String password = loginRequestData.getPassword();
        User user = userService.findUserByUsername(username);
        if (user == null) {
            return Result.error("Invalid username or password");
        }
        if (Md5Util.getMD5String(password).equals(user.getPassword())) {
            if (("BANNED").equals(user.getUserStatus().name())) {
                return Result.error("Your account has been suspended. Please contact support.");
            }
            // login
            Map<String, Object> claims = new HashMap<>();
            claims.put("id", user.getId());
            claims.put("username", user.getUsername());
            claims.put("role", user.getRole().name());
            // generate JWT token
            String token = JwtUtil.genToken(claims);
            // store toke into redis
            ValueOperations<String, String> operations = stringRedisTemplate.opsForValue();
            SetOperations<String, String> setOperations = stringRedisTemplate.opsForSet();

            String tokenKey = "token:" + token;
            operations.set(tokenKey, "1", 6, TimeUnit.HOURS);

            String userTokensKey = "user:tokens:" + user.getId();
            setOperations.add(userTokensKey, token);

            stringRedisTemplate.expire(userTokensKey, 6, TimeUnit.HOURS);

            return Result.success(token);

        }
        return Result.error("Invalid username or password.");
    }

    @PostMapping("/logout")
    public Result logout(HttpServletRequest request){
        String token = request.getHeader("Authorization");
        userService.logout(token);
        return Result.success();
    }

    // get user info
    @GetMapping("/info")
    public Result<User> getUserInfo() {
        Map<String, Object> claims = ThreadLocalUtil.get();
        Integer id = (Integer) claims.get("id");
        User user = userService.findUserById(id);
        if (user == null) {
            return null;
        }
        user.setPassword(null);
        return Result.success(user);
    }

    @PatchMapping("/avatar")
    public Result<String> updateAvatar(MultipartFile file) throws IOException {
        String avatarURL = userService.uploadAvatar(file);
        userService.updateAvatar(avatarURL);
        return Result.success(avatarURL);
    }

    @PostMapping("/email/check")
    public Result updateUserEmailCheck(@Valid @NotBlank(message = "email cannot be blank") String email) {
        userService.updateUserEmailCheck(email);
        return Result.success();
    }

    @PatchMapping("/username")
    public Result updateUsername(@NotBlank String username) {
        userService.updateUsername(username);
        return Result.success();
    }

    @GetMapping("/recover/username")
    public Result<String> retrieveUsername(@NotBlank(message = "Token cannot be blank") String token) {
        String username = userService.retrieveUsername(token);
        return Result.success(username);
    }

    @PatchMapping("/email")
    public Result updateUserEmail(@NotBlank String email, @NotBlank @Size(min = 6, max = 6) String otp) {
        userService.updateUserEmail(email, otp);
        return Result.success();
    }

    @PostMapping("/recover/verify")
    public Result<String> verifyAccountHelp(@NotBlank String email, @NotBlank @Size(min = 6, max = 6) String otp) {
        String token = userService.verifyAccountHelp(email, otp);
        return Result.success(token);
    }

    @PatchMapping("/password")
    public Result updateUserPassword(@RequestBody ResetPasswordRequest resetPasswordRequest) {
        String password = resetPasswordRequest.getPassword();
        String accHelpToken = resetPasswordRequest.getToken();
        userService.updateUserPassword(password, accHelpToken);
        return Result.success();
    }

    @PatchMapping("/ban")
    public Result banUser(@NotNull Integer userId, @NotBlank String reason) {
        userService.banUser(userId, reason);
        return Result.success();
    }

    @PatchMapping("/active")
    public Result activeUser(@NotNull Integer userId) {
        userService.activeUser(userId);
        return Result.success();
    }

    @GetMapping("/paticipatePost")
    public Result<ArrayList<Integer>> getAllParticipatePost() {
        ArrayList<Integer> postIdList = userService.getAllParticipatePost();
        return Result.success(postIdList);
    }

    @GetMapping("/commentLiked")
    public Result<ArrayList<Integer>> getAllCommentLiked() {
        ArrayList<Integer> commentIdList = userService.getAllCommentLiked();
        return Result.success(commentIdList);
    }

    @GetMapping("/ownedPost")
    public Result<ArrayList<Integer>> getAllOwnedPost() {
        ArrayList<Integer> postIdList = userService.getAllOwnedPost();
        return Result.success(postIdList);
    }

    @GetMapping("/leavedComment")
    public Result<ArrayList<Integer>> getAllLeavedCommentId() {
        ArrayList<Integer> commentIdList = userService.getAllLeavedCommentId();
        return Result.success(commentIdList);
    }
}
