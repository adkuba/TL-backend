package com.tl.backend.fileHandling;

import org.springframework.web.multipart.MultipartFile;

public interface FileService {
    FileResource saveFileResource(MultipartFile file);
    void deleteFileResource(FileResource resource);
}
