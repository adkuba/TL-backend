package com.tl.backend.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.net.URL;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;

@Data
@NoArgsConstructor
public class EventResponse implements Serializable {
    private String id;

    private String title;
    private String shortDescription;
    private String description;
    private HashMap<String, String> links;
    private List<URL> pictures;
    private LocalDate date;
}
