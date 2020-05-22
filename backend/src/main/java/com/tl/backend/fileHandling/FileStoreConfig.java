package com.tl.backend.fileHandling;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.content.fs.config.EnableFilesystemStores;
import org.springframework.content.fs.io.FileSystemResourceLoader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.nio.file.Paths;

@Configuration
@EnableFilesystemStores
public class FileStoreConfig {

    private final FileStoreProperties fileStoreProperties;

    @Autowired
    public FileStoreConfig(FileStoreProperties fileStoreProperties) {this.fileStoreProperties = fileStoreProperties;}

    @Bean
    File fileSystemRoot(FileStoreProperties properties){
        File root = Paths.get(properties.getPath()).toAbsolutePath().normalize().toFile();

        if(!root.exists())
            root.mkdir();

        return root;
    }

    @Bean
    FileSystemResourceLoader fileSystemResourceLoader(){
        return new FileSystemResourceLoader(fileSystemRoot(fileStoreProperties).getAbsolutePath());
    }
}
