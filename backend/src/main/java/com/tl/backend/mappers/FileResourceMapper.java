package com.tl.backend.mappers;

import com.tl.backend.config.AppProperties;
import com.tl.backend.fileHandling.FileResource;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@Component
public class FileResourceMapper {

    @Autowired
    public FileResourceMapper() {
    }

    @SneakyThrows
    public List<URL> mapToURLS(List<FileResource> resources){
        if (resources != null){
            List<URL> urls = new ArrayList<>();
            for (FileResource fileResource: resources){
                urls.add(new URL("https://storage.googleapis.com/tline-files/" + fileResource.getId()));
            }
            return urls;
        }
        return null;
    }

}