package com.tl.backend.fileHandling;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.util.UUID;

@Service
public class FileServiceImpl implements FileService {

    private final FileResourceRepository fileResourceRepository;
    private final Storage gStorage;

    @Autowired
    public FileServiceImpl(Storage gStorage, FileResourceRepository fileResourceRepository) {
        this.fileResourceRepository = fileResourceRepository;
        this.gStorage = gStorage;
    }

    private FileResource createFileResource(MultipartFile file){
        FileResource fileResource = new FileResource();
        fileResource.setId(UUID.randomUUID().toString());
        fileResource.setMimeType(file.getContentType());
        fileResource.setContentLength(file.getSize());
        return fileResource;
    }

    @SneakyThrows
    private FileResource setContent(FileResource fileResource, MultipartFile file){
        BlobId blobId = BlobId.of("tline-files", fileResource.getId());
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();
        gStorage.create(blobInfo, file.getBytes());
        return fileResourceRepository.save(fileResource);
    }

    @Override
    public FileResource saveFileResource(MultipartFile file) {
        return setContent(createFileResource(file),file);
    }

    @Override
    public void deleteFileResource(FileResource resource) {
        gStorage.delete("tline-files", resource.getId());
        fileResourceRepository.delete(resource);
    }
}
