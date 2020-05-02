package com.tl.backend.entities;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document(collection = "events")
@Data
@NoArgsConstructor
public class Event {

    @Id
    private String id;

    private String title;

    private Timeline timeline;

    private String description;

    private Date date;
}
