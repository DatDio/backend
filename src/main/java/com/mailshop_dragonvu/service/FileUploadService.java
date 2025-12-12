package com.mailshop_dragonvu.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileUploadService {
    
    /**
     * Upload an image file and return the URL path
     * @param file The multipart file to upload
     * @param folder The subfolder to store the file (e.g., "icons", "avatars")
     * @return The URL path to access the uploaded file
     */
    String uploadImage(MultipartFile file, String folder);
    
    /**
     * Delete a file by its URL path
     * @param filePath The URL path of the file to delete
     */
    void deleteFile(String filePath);
}
