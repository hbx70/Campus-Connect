package com.hbx.campusconnect.mapper;

import com.hbx.campusconnect.pojo.Post;
import com.hbx.campusconnect.pojo.UploadedImage;
import jakarta.validation.constraints.NotNull;
import org.apache.ibatis.annotations.*;

import java.util.ArrayList;
import java.util.List;

@Mapper
public interface UploadMapper {
    // save temp image (used to delete temp image)
    @Insert("insert into uploaded_images (url, user_id, status, created_at) values (#{url}, #{userId}, #{status}, #{createdAt})")
    void saveTempImage(UploadedImage img);

    // update temp image into used
    void markAsUsed(@Param("urls") ArrayList<String> coverImgJson);

    // get all expired images
    @Select("select * from uploaded_images where status = 'TEMP' and created_at < NOW() - interval 2 hour")
    List<UploadedImage> findExpiredTempImages();

    // delete all expired images
    @Delete("delete from uploaded_images where id=#{id}")
    void deleteById(Integer id);

    // mark deleted image to temp
    @Update("update uploaded_images set status='TEMP' where url=#{imgUrl}")
    void markAsTemp(String imgUrl);
}
