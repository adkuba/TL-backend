package com.tl.backend.fileHandling;

import com.tl.backend.config.AppProperties;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URL;

@Component
public class FileResourceMapper {

    private final AppProperties appProperties;
    private final FileStoreProperties storeProperties;

    @Autowired
    public FileResourceMapper(AppProperties appProperties, FileStoreProperties storeProperties) {
        this.appProperties = appProperties;
        this.storeProperties = storeProperties;
    }

    @SneakyThrows
    public URL mapToURL(FileResource resource){
        return resource != null ? new URL( appProperties.getApiDomain() + storeProperties.getPath() + resource.getId()) : null;
    }

}