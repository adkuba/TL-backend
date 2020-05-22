package com.tl.backend.fileHandling;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "tl.filesystem.root")
public class FileStoreProperties {

    private String path = "./files";

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
