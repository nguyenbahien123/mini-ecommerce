package vn.nbh.productservice.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import vn.nbh.productservice.exception.AppException;
import vn.nbh.productservice.exception.ErrorCode;
import vn.nbh.productservice.service.MediaService;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MediaServiceImpl implements MediaService {

    private final Cloudinary cloudinary;

    private static final List<String> ALLOWED_CONTENT_TYPES = List.of(
            "image/jpeg", "image/png", "image/gif");
    @Override
    public String uploadImage(MultipartFile file) {
        if(file.isEmpty()){
            throw new AppException(ErrorCode.INVALID_INPUT);
        }
        String contentType = file.getContentType();
        if(contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)){
            throw new AppException(ErrorCode.INVALID_INPUT);
        }
        try {
            // 3. Đổi tên file để tránh path traversal và lưu gọn trên Cloudinary
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String newFilename = UUID.randomUUID().toString();

            // 4. Cấu hình upload lên Cloudinary
            Map<String, Object> uploadParams = ObjectUtils.asMap(
                    "public_id", "product-images/" + newFilename, // Lưu vào folder product-images
                    "resource_type", "image" // Chặn tải các loại file khác (như video/raw data)
            );

            // 5. Đẩy ảnh
            Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), uploadParams);

            // 6. Trả về URL an toàn (HTTPS)
            return uploadResult.get("secure_url").toString();

        } catch (IOException e) {
            log.error("Lỗi khi upload file lên Cloudinary: ", e);
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
}
