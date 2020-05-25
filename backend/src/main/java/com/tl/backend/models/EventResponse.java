package com.tl.backend.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.HashMap;

@Data
@NoArgsConstructor
public class EventResponse implements Serializable {
    private String id;

    private String title;
    private String shortDescription;
    private String description;
    private HashMap<String, String> links;
    private URL picture;
    private LocalDateTime date;
}
