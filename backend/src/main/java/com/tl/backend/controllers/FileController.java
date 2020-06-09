package com.tl.backend.controllers;

import com.tl.backend.fileHandling.FileResource;
import com.tl.backend.fileHandling.FileResourceRepository;
import com.tl.backend.fileHandling.FileServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private final FileServiceImpl fileService;
    private final FileResourceRepository fileResourceRepository;

    @Autowired
    public FileController(FileServiceImpl fileService, FileResourceRepository fileResourceRepository){
        this.fileService = fileService;
        this.fileResourceRepository = fileResourceRepository;
    }

    @DeleteMapping
    public ResponseEntity<?> deleteResources(@NotNull @RequestParam List<String> urls){
        for (String url : urls){
            String[] parts = url.split("/");
            Optional<FileResource> optionalFileResource = fileResourceRepository.findById(parts[parts.length-1]);
            optionalFileResource.ifPresent(fileService::deleteFileResource);
        }
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
