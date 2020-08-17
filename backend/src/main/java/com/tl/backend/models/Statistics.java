package com.tl.backend.models;

import com.fasterxml.jackson.annotation.JsonFormat;
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
    private Long totalTimelinesViews = 0L;
    private Long mainPageViews = 0L;
    private Long numberOfUsers = 0L;
    private Long activeUsers = 0L;
    private Long profileViews = 0L;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate day;
    private Map<String, Long> devices = new HashMap<>();
}
