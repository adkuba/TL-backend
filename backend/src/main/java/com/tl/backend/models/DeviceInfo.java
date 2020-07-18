package com.tl.backend.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@Document(collection = "deviceInfo")
public class DeviceInfo {

    @Id
    private String id;
    private String location;
    private String deviceDetails;
    private String ip;
    private String username;
    private LocalDate lastLogged;
}
