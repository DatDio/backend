package com.mailshop_dragonvu.service.impl;

import com.mailshop_dragonvu.exception.BusinessException;
import com.mailshop_dragonvu.service.FileUploadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class FileUploadServiceImpl implements FileUploadService {

    private static final List<String> ALLOWED_IMAGE_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "gif", "webp", "svg");
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    @Value("${app.upload.path:uploads}")
    private String uploadPath;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @Override
    public String uploadImage(MultipartFile file, String folder) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        // Validate file size
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException("Kích thước file không được vượt quá 5MB");
        }

        // Get file extension
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.contains(".")) {
            throw new BusinessException("File không hợp lệ");
        }

        String extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();

        // Validate extension
        if (!ALLOWED_IMAGE_EXTENSIONS.contains(extension)) {
            throw new BusinessException("Chỉ chấp nhận file ảnh: " + String.join(", ", ALLOWED_IMAGE_EXTENSIONS));
        }

        try {
            // Create upload directory if not exists (relative to working directory)
            Path uploadDir = Paths.get(uploadPath, folder);
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            // Generate unique filename
            String newFilename = UUID.randomUUID().toString() + "." + extension;
            Path filePath = uploadDir.resolve(newFilename);

            // Save file
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            log.info("Uploaded file: {}", filePath.toAbsolutePath());

            // Return full URL (e.g., https://api.mailshop.vn/uploads/products/uuid.jpg)
            String relativePath = "/uploads/" + folder + "/" + newFilename;
            return baseUrl + relativePath;
        } catch (IOException e) {
            log.error("Error uploading file", e);
            throw new BusinessException("Lỗi khi upload file: " + e.getMessage());
        }
    }

    @Override
    public void deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return;
        }

        // Extract relative path from full URL or relative URL
        String relativePath;
        if (fileUrl.startsWith("http://") || fileUrl.startsWith("https://")) {
            // Full URL: https://api.mailshop.vn/uploads/products/uuid.jpg -> /uploads/products/uuid.jpg
            try {
                java.net.URL url = new java.net.URL(fileUrl);
                relativePath = url.getPath();
            } catch (java.net.MalformedURLException e) {
                log.warn("Invalid URL format: {}", fileUrl);
                return;
            }
        } else {
            relativePath = fileUrl;
        }

        // Security: Only allow deleting files in uploads folder
        if (!relativePath.startsWith("/uploads/")) {
            log.warn("Attempted to delete file outside uploads folder: {}", fileUrl);
            return;
        }

        try {
            // Convert URL path to file path
            // /uploads/icons/uuid.png -> uploads/icons/uuid.png
            String filePath = relativePath.substring(1); // Remove leading /
            Path path = Paths.get(filePath);

            if (Files.exists(path)) {
                Files.delete(path);
                log.info("Deleted file: {}", path);
            }
        } catch (IOException e) {
            log.error("Error deleting file: {}", fileUrl, e);
        }
    }
}
