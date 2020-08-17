package com.tl.backend.fileHandling;

import org.springframework.web.multipart.MultipartFile;

public interface FileService {
    FileResource saveFileResource(MultipartFile file);
    FileResource saveFileResource(String json, String name);
    void deleteFileResource(FileResource resource);
    void deleteFileResource(String resourceId);
}
