package com.hbx.campusconnect.service.impl;

import com.hbx.campusconnect.exception.ForbiddenException;
import com.hbx.campusconnect.mapper.*;
import com.hbx.campusconnect.pojo.RegisterRequest;
import com.hbx.campusconnect.pojo.RetrieveUsernameRequest;
import com.hbx.campusconnect.pojo.User;
import com.hbx.campusconnect.service.EmailService;
import com.hbx.campusconnect.service.UserAuditService;
import com.hbx.campusconnect.service.UserService;
import com.hbx.campusconnect.utils.Md5Util;
import com.hbx.campusconnect.utils.ThreadLocalUtil;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Value;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl implements UserService {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final Integer CODE_TTL_MINUTES = 5;

    @Value("${r2.bucket}") private String bucket;
    @Value("${r2.publicBaseUrl}") private String publicBaseUrl;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private EmailService emailService;

    @Autowired
    private S3Client s3Client;

    @Autowired
    private UserAuditService userAuditService;
    @Autowired
    private PostParticipateMapper postParticipateMapper;
    @Autowired
    private CommentLikeMapper commentLikeMapper;
    @Autowired
    private PostMapper postMapper;
    @Autowired
    private CommentMapper commentMapper;

    @Override
    public User findUserByUsername(String username) {
        return userMapper.findUserByUsername(username);
    }

    @Override
    public void register(RegisterRequest req) {
        String encryptedPwd = Md5Util.getMD5String(req.getPassword());
        User user = new User();
        user.setUsername(req.getUsername());
        user.setEmail(req.getEmail());
        user.setPassword(encryptedPwd);
        user.setRole(User.Role.USER);
        user.setUserAvatar(req.getUserAvatar().isEmpty() ? "https://img.campusconnect.one/avatars/default.svg" : req.getUserAvatar());
        user.setUserStatus(User.Status.ACTIVE);
        user.setCreatedAt(LocalDateTime.now());
        userMapper.register(user);
    }

    @Override
    public User findUserById(Integer id) {
        return userMapper.findUserById(id);
    }

    @Override
    public User findUserByEmail(String email) {
        return userMapper.findUserByEmail(email);
    }

    @Override
    public void sendEmailOTP(String email) throws IOException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        String otp = this.generateAndStoreOTP(email, "register");
        emailService.sendOTPEmailBuilder(email, otp, LocalDateTime.now().format(formatter));
    }

    @Override
    public void sendEmailChangedCode(String email) throws IOException {
        String otp = this.generateAndStoreOTP(email, "changeEmail");
        Map<String, Object> claims = ThreadLocalUtil.get();
        Integer loginUserId = (Integer) claims.get("id");
        String username = userMapper.findUserById(loginUserId).getUsername();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        emailService.sendUpdateEmailEBuilder(username, email, otp, LocalDateTime.now().format(formatter));
    }

    @Override
    public void sendAccountHelpOTP(String email) throws IOException {
        User user = userMapper.findUserByEmail(email);
        if (user == null) {
            throw new RuntimeException("This email has not been registered");
        }
        if ("BANNED".equals(user.getUserStatus().name())) {
            throw new RuntimeException("Your account has been suspended. Please contact support.");
        }
        String otp = this.generateAndStoreOTP(email, "accountHelp");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        emailService.sendAccountHelpOTP(user.getUsername(), email, otp, LocalDateTime.now().format(formatter));
    }

    @Override
    public String uploadAvatar(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("file is empty");
        }
        if (file.getSize() > 1024 * 1024 * 5) {
            throw new RuntimeException("file too large (max 5MB)");
        }
        String contentType = file.getContentType();
        if (contentType == null || !(contentType.equals("image/png") || contentType.equals("image/jpg") || contentType.equals("image/jpeg") || contentType.equals("image/webp"))) {
            throw new RuntimeException("only png/jpg/jpeg/webp allowed");
        }
        String ext = contentType.substring(contentType.lastIndexOf("/") + 1);
        Map<String, Object> claims = ThreadLocalUtil.get();
        Integer userId = (Integer) claims.get("id");
        String key = "avatars/" + userId + "/" + UUID.randomUUID() + "." + ext;
        PutObjectRequest putReq = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(contentType)
                .build();
        s3Client.putObject(putReq, RequestBody.fromBytes(file.getBytes()));
        return publicBaseUrl + "/" + key;
    }

    @Override
    public void updateAvatar(String avatarURL) {
        Map<String, Object> claims = ThreadLocalUtil.get();
        Integer userId = (Integer) claims.get("id");
        userMapper.updateAvatar(avatarURL, userId);
    }

    @Override
    public void updateUserEmailCheck(String email) {
        if (userMapper.findUserByEmail(email) != null) {
            throw new RuntimeException("This email has been registered");
        }
    }

    @Override
    public void updateUsername(String newUsername) {
        if (userMapper.findUserByUsername(newUsername) != null) {
            throw new RuntimeException("This username has been occupied");
        }
        Map<String, Object> claims = ThreadLocalUtil.get();
        Integer currUserId = (Integer) claims.get("id");
        userMapper.updateUsername(currUserId, newUsername);
    }

    @Override
    public void updateUserEmail(String newEmail, String otp) {
        if (userMapper.findUserByEmail(newEmail) != null) {
            throw new RuntimeException("This email has been registered");
        }
        String key = "email:changeEmail:otp:" + newEmail;
        String savedOTP = stringRedisTemplate.opsForValue().get(key);
        if (savedOTP == null) {
            throw new RuntimeException("Verification code expired or not sent");
        }
        if (!otp.equals(savedOTP)) {
            throw new RuntimeException("Invalid verification code");
        }
        Map<String, Object> claims = ThreadLocalUtil.get();
        Integer loginUserId = (Integer) claims.get("id");
        userMapper.updateEmail(newEmail, loginUserId);
        stringRedisTemplate.delete(key);
    }

    @Override
    public String verifyAccountHelp(String email, String otp) {
        String key = "email:accountHelp:otp:" + email;
        String savedOTP = stringRedisTemplate.opsForValue().get(key);
        if (savedOTP == null) {
            throw new RuntimeException("Verification code expired or not sent");
        }
        if (!otp.equals(savedOTP)) {
            throw new RuntimeException("Invalid verification code");
        }
        stringRedisTemplate.delete(key);
        String token = UUID.randomUUID().toString();
        String tokenKey = "accountHelp:token:" + token;
        stringRedisTemplate.opsForValue().set(tokenKey, email, 15, TimeUnit.MINUTES);
        return token;
    }

    @Override
    public void updateUserPassword(String newPassword, String accHelpToken) {
        String key = "accountHelp:token:" + accHelpToken;
        String email = stringRedisTemplate.opsForValue().get(key);
        if (email == null) {
            throw new RuntimeException("Your session has expired. Please try again.");
        }
        User user = userMapper.findUserByEmail(email);
        if (user == null) {
            throw new RuntimeException("Operation failure");
        }
        Integer userId = user.getId();
        String encryptedNewPwd = Md5Util.getMD5String(newPassword);
        userMapper.updatePassword(encryptedNewPwd, userId);
        stringRedisTemplate.delete(key);

        String userTokensKey = "user:tokens:" + userId;
        Set<String> tokens = stringRedisTemplate.opsForSet().members(userTokensKey);

        if (tokens != null && !tokens.isEmpty()) {
            for (String token : tokens) {
                stringRedisTemplate.delete("token:" + token);
            }
            stringRedisTemplate.delete(userTokensKey);
        }
    }

    @Override
    public String retrieveUsername(String token) {
        String key = "accountHelp:token:" + token;
        String email = stringRedisTemplate.opsForValue().get(key);
        if (email == null) {
            throw new RuntimeException("Your session has expired. Please try again.");
        }
        User user = userMapper.findUserByEmail(email);
        if (user == null) {
            throw new RuntimeException("Operation failure");
        }
        return user.getUsername();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void banUser(Integer userId, String reason) {
        Map<String, Object> claims = ThreadLocalUtil.get();
        String currentRole = (String) claims.get("role");
        if (currentRole.equals("USER")) {
            throw new ForbiddenException("you have no right to ban a user");
        }
        User user = this.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("user not found");
        }
        if (user.getUserStatus() == User.Status.BANNED) {
            throw new RuntimeException("this user has been banned");
        }
        int rows = userMapper.banUser(userId);
        if (rows == 0) {
            throw new RuntimeException("ban failed");
        }

        Integer adminId = (Integer) claims.get("id");
        userAuditService.recordBanUser(userId, adminId, reason == null ? "The user violated the website's requirements" : reason);

        String userTokensKey = "user:tokens:" + userId;
        Set<String> tokens = stringRedisTemplate.opsForSet().members(userTokensKey);

        if (tokens != null && !tokens.isEmpty()) {
            for (String token : tokens) {
                stringRedisTemplate.delete("token:" + token);
            }
            stringRedisTemplate.delete(userTokensKey);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void activeUser(Integer userId) {
        Map<String, Object> claims = ThreadLocalUtil.get();
        String currentRole = (String) claims.get("role");
        if (currentRole.equals("USER")) {
            throw new ForbiddenException("you have no right to active a user");
        }
        User user = this.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("user not found");
        }
        if (user.getUserStatus() == User.Status.ACTIVE) {
            throw new RuntimeException("this user has been active");
        }
        int rows = userMapper.activeUser(userId);
        if (rows == 0) {
            throw new RuntimeException("active failed");
        }

        Integer adminId = (Integer) claims.get("id");
        userAuditService.recordActiveUser(userId, adminId);
    }

    @Override
    public ArrayList<Integer> getAllParticipatePost() {
        Map<String, Object> claims = ThreadLocalUtil.get();
        Integer currentUserId = (Integer) claims.get("id");
        return postParticipateMapper.getAllParticipatePost(currentUserId);
    }

    @Override
    public ArrayList<Integer> getAllCommentLiked() {
        Map<String, Object> claims = ThreadLocalUtil.get();
        Integer currentUserId = (Integer) claims.get("id");
        return commentLikeMapper.getAllCommentLiked(currentUserId);
    }

    @Override
    public ArrayList<Integer> getAllOwnedPost() {
        Map<String, Object> claims = ThreadLocalUtil.get();
        Integer currentUserId = (Integer) claims.get("id");
        return postMapper.getAllOwnedPost(currentUserId);
    }

    @Override
    public ArrayList<Integer> getAllLeavedCommentId() {
        Map<String, Object> claims = ThreadLocalUtil.get();
        Integer currentUserId = (Integer) claims.get("id");
        return commentMapper.getAllLeavedCommentId(currentUserId);
    }

    @Override
    public void logout(String token) {
        String key = "token:" + token;
        stringRedisTemplate.delete(key);
    }

    private String generateAndStoreOTP(String email, String action) {
        String key = "email:" + action + ":otp:" + email;
        String existing = stringRedisTemplate.opsForValue().get(key);
        if (existing != null) {
            // opt is existing
            return existing;
        }
        // generate email otp
        String otp = String.format("%06d", RANDOM.nextInt(1_000_000));
        stringRedisTemplate.opsForValue().set(key, otp, CODE_TTL_MINUTES, TimeUnit.MINUTES);
        return otp;
    }
}
