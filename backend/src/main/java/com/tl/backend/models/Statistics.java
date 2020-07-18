package com.tl.backend.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "statistics")
@Data
@NoArgsConstructor
public class Statistics {

    @Id
    private String id;
    private long totalTimelinesViews = 0;
    private long mainPageViews = 0;
    private long numberOfUsers = 0;
    private long activeUsers = 0;
    private LocalDate day;
    private List<Review> reviews = new ArrayList<>();
    private List<String> devices = new ArrayList<>();
}
