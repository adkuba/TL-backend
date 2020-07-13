package com.tl.backend.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tl.backend.fileHandling.FileResource;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;

@Document(collection = "events")
@Data
@NoArgsConstructor
public class Event {

    @Id
    private String id;
    private String title;
    private String timelineId;
    private String description;
    private List<FileResource> pictures;
    private LocalDate date;
}
