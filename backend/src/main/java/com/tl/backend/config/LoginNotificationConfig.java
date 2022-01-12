package com.tl.backend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.Credentials;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.maxmind.db.CHMCache;
import com.maxmind.db.Reader;
import com.maxmind.geoip2.DatabaseReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.util.ResourceUtils;
import ua_parser.Parser;

import java.io.*;

@Configuration
public class LoginNotificationConfig {

    @Bean
    public Parser uaParser() throws IOException {
        return new Parser();
    }

    @Bean(name="GeoIPCity")
    public DatabaseReader databaseReader() throws IOException {
        //uncomment for google cloud
        ClassLoader cl = this.getClass().getClassLoader();
        InputStream database = cl.getResourceAsStream("classpath:maxmind/GeoLite2-Country.mmdb");
        //comment for google cloud
        //File database = ResourceUtils.getFile("classpath:maxmind/GeoLite2-Country.mmdb");
        return new DatabaseReader.Builder(database).build();
    }

    @Bean(name = "gstorage")
    public Storage gStorage() throws IOException {
        //uncomment for localhost
        //File file = ResourceUtils.getFile("classpath:google/authentication.json");
        //Credentials credentials = GoogleCredentials.fromStream(new FileInputStream(file));
        //uncomment for google
        ClassLoader cl = this.getClass().getClassLoader();
        InputStream inputStream = cl.getResourceAsStream("classpath:google/authentication.json");
        assert inputStream != null;
        Credentials credentials = GoogleCredentials.fromStream(inputStream);
        return StorageOptions.newBuilder().setProjectId("quicpos").setCredentials(credentials).build().getService();
    }
}
