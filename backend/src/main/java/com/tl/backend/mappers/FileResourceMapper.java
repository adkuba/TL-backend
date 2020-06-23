package com.tl.backend.mappers;

import com.tl.backend.config.AppProperties;
import com.tl.backend.fileHandling.FileResource;
import com.tl.backend.fileHandling.FileStoreProperties;
import com.tl.backend.models.ERole;
import com.tl.backend.models.Role;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
    public List<URL> mapToURLS(List<FileResource> resources){
        if (resources != null){
            List<URL> urls = new ArrayList<>();
            for (FileResource fileResource: resources){
                urls.add(new URL(appProperties.getApiDomain() + storeProperties.getPath() + fileResource.getId()));
            }
            return urls;
        }
        return null;
    }

}