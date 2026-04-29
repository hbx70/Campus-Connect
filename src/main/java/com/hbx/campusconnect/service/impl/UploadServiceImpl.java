package com.hbx.campusconnect.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hbx.campusconnect.mapper.PostMapper;
import com.hbx.campusconnect.mapper.UploadMapper;
import com.hbx.campusconnect.pojo.Post;
import com.hbx.campusconnect.pojo.UploadedImage;
import com.hbx.campusconnect.service.UploadService;
import com.hbx.campusconnect.utils.ThreadLocalUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Value;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class UploadServiceImpl implements UploadService {

    @Value("${r2.bucket}") private String bucket;
    @Value("${r2.publicBaseUrl}") private String publicBaseUrl;

    @Autowired
    private S3Client s3Client;

    @Autowired
    private UploadMapper uploadMapper;
    @Autowired
    private PostMapper postMapper;

    @Override
    public String upload(MultipartFile file) throws IOException {
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
        String fileName = UUID.randomUUID() + "-" + file.getOriginalFilename();
        Map<String, Object> claims = ThreadLocalUtil.get();
        Integer userId = (Integer) claims.get("id");
        String key = "images/" + userId + "/" +
                LocalDate.now().getYear() + "/" +
                LocalDate.now().getMonthValue() + "/" +
                fileName;
        PutObjectRequest putReq = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(file.getContentType())
                .build();

        s3Client.putObject(
                putReq,
                RequestBody.fromInputStream(file.getInputStream(), file.getSize())
        );

        return publicBaseUrl + "/" + key;
    }

    @Override
    public void delete(String url) {
        String key = url.replace(publicBaseUrl + "/", "");
        s3Client.deleteObject(
                DeleteObjectRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .build()
        );
    }

    @Override
    public void saveTempImage(String url) {
        Map<String, Object> claims = ThreadLocalUtil.get();
        Integer userId = (Integer) claims.get("id");
        UploadedImage img = new UploadedImage();
        img.setUrl(url);
        img.setUserId(userId);
        img.setStatus(UploadedImage.Status.TEMP);
        img.setCreatedAt(LocalDateTime.now());
        uploadMapper.saveTempImage(img);
    }

    @Override
    public void removePostImage(Integer postId) throws JsonProcessingException {
        String coverImages = postMapper.findPostCoverImageById(postId);
        ObjectMapper mapper = new ObjectMapper();
        List<String> imgUrls = mapper.readValue(coverImages, new TypeReference<List<String>>() {});
        for (String imgUrl : imgUrls) {
            uploadMapper.markAsTemp(imgUrl);
        }
    }

    @Scheduled(cron = "0 0 * * * ?")
    public void cleanTempImages() {
        List<UploadedImage> list = uploadMapper.findExpiredTempImages();
        for (UploadedImage img : list) {
            this.delete(img.getUrl());
            uploadMapper.deleteById(img.getId());
        }
    }
}
