package com.tl.backend.fileHandling;

import com.google.cloud.storage.Blob;
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

    public FileResource createFileResource(MultipartFile file){
        FileResource fileResource = new FileResource();
        fileResource.setMimeType(file.getContentType());
        String[] type = fileResource.getMimeType().split("/");
        fileResource.setId(UUID.randomUUID().toString() + '.' + type[type.length - 1]);
        fileResource.setContentLength(file.getSize());
        return fileResource;
    }

    public FileResource createFileResource(String json, String name){
        FileResource fileResource = new FileResource();
        fileResource.setId(name);
        fileResource.setMimeType("application/json");
        int size = json.getBytes().length;
        fileResource.setContentLength((long) size);
        return fileResource;
    }

    @SneakyThrows
    private FileResource setContent(FileResource fileResource, MultipartFile file){
        BlobId blobId = BlobId.of("tline-files", fileResource.getId());
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();
        gStorage.create(blobInfo, file.getBytes());
        return fileResourceRepository.save(fileResource);
    }

    @SneakyThrows
    private FileResource setContent(FileResource fileResource, String json){
        BlobId blobId = BlobId.of("tline-files", fileResource.getId());
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();
        gStorage.create(blobInfo, json.getBytes());
        return fileResourceRepository.save(fileResource);
    }

    @Override
    public FileResource saveFileResource(MultipartFile file) {
        return setContent(createFileResource(file),file);
    }

    @Override
    public FileResource saveFileResource(String json, String name) {
        return setContent(createFileResource(json, name),json);
    }

    @Override
    public void deleteFileResource(FileResource resource) {
        if (fileResourceRepository.findById(resource.getId()).isPresent()){
            gStorage.delete("tline-files", resource.getId());
            fileResourceRepository.delete(resource);
        }
    }

    @Override
    public void deleteFileResource(String resourceId) {
        if (fileResourceRepository.findById(resourceId).isPresent()){
            gStorage.delete("tline-files", resourceId);
            fileResourceRepository.deleteById(resourceId);
        }
    }

    @Override
    public FileResource changeFileResourceID(FileResource fileResource, String newID) {
        fileResourceRepository.delete(fileResource);
        BlobId blobId = BlobId.of("tline-files", newID);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();
        Blob blob = gStorage.get("tline-files", fileResource.getId());
        gStorage.create(blobInfo, blob.getContent());
        deleteFileResource(fileResource.getId());
        fileResource.setId(newID);
        return fileResourceRepository.save(fileResource);
    }
}
