package com.tl.backend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.Credentials;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.maxmind.db.Reader;
import com.maxmind.geoip2.DatabaseReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
        Storage storage = gStorage();
        Blob blob = storage.get("tline-files", "GeoLite2-City.mmdb");
        InputStream inputStream = new ByteArrayInputStream(blob.getContent());
        return new DatabaseReader.Builder(inputStream).build();
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
        return StorageOptions.newBuilder().setProjectId("tline-files").setCredentials(credentials).build().getService();
    }
}
