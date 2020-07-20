package com.tl.backend.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private Map<String, Long> devices = new HashMap<>();
}
