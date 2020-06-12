package com.tl.backend.fileHandling;

import com.tl.backend.repositories.FileResourceRepository;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileServiceImpl implements FileService {

    private final FileResourceRepository fileResourceRepository;
    private final FileResourceContentStore fileResourceContentStore;

    @Autowired
    public FileServiceImpl(FileResourceRepository fileResourceRepository, FileResourceContentStore fileResourceContentStore) {
        this.fileResourceRepository = fileResourceRepository;
        this.fileResourceContentStore = fileResourceContentStore;
    }

    private FileResource createFileResource(MultipartFile file){
        FileResource fileResource = new FileResource();
        fileResource.setMimeType(file.getContentType());
        fileResource.setContentLength(file.getSize());
        return fileResource;
    }

    @SneakyThrows
    private FileResource setContent(FileResource fileResource, MultipartFile file){
        fileResourceContentStore.setContent(fileResource,file.getInputStream());
        return fileResourceRepository.save(fileResource);
    }

    @Override
    public FileResource saveFileResource(MultipartFile file) {
        return setContent(createFileResource(file),file);
    }

    @Override
    public void deleteFileResource(FileResource resource) {
        fileResourceContentStore.unsetContent(resource);
        fileResourceRepository.delete(resource);
    }
}
