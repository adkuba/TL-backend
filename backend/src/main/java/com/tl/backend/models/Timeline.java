package com.tl.backend.models;

import com.tl.backend.fileHandling.FileResource;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "timelines")
@Data
@NoArgsConstructor
public class Timeline {

    @Id
    private String id;

    private User user;

    private String description;

    private String descriptionTitle;

    private Event event;

    private List<FileResource> pictures;
}
