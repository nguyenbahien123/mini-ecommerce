package vn.nbh.productservice.service;

import org.springframework.web.multipart.MultipartFile;

public interface MediaService {
    String uploadImage(MultipartFile file);
}