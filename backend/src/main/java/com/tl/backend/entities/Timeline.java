package com.tl.backend.entities;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "timelines")
@Data
@NoArgsConstructor
public class Timeline {

    @Id
    private String id;

    private User user;

    private Event event;
}
