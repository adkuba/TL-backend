package com.tl.backend.config;

import com.maxmind.geoip2.DatabaseReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ResourceUtils;
import ua_parser.Parser;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

@Configuration
public class LoginNotificationConfig {

    @Bean
    public Parser uaParser() throws IOException {
        return new Parser();
    }

    @Bean(name="GeoIPCity")
    public DatabaseReader databaseReader() throws IOException {
        //uncomment for google cloud
        //ClassLoader cl = this.getClass().getClassLoader();
        //InputStream inputStream = cl.getResourceAsStream("classpath:maxmind/GeoLite2-Country.mmdb");
        //comment for google cloud
        File database = ResourceUtils.getFile("classpath:maxmind/GeoLite2-Country.mmdb");
        return new DatabaseReader.Builder(database).build();
    }
}
