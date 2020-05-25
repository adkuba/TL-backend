package com.tl.backend.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tl.backend.fileHandling.FileResource;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.HashMap;

@Document(collection = "events")
@Data
@NoArgsConstructor
public class Event {

    @Id
    private String id;

    private String title;

    private Timeline timeline;

    private String shortDescription;

    private String description;

    private HashMap<String, String> links;

    private FileResource picture;

    private LocalDateTime date;
}
