package com.tl.backend.response;

import com.tl.backend.fileHandling.FileResource;
import com.tl.backend.models.InteractionEvent;
import com.tl.backend.models.User;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;

import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
public class FullTimelineResponse {
    private String id;
    private User user;
    private Boolean premium = false;
    private Boolean active = true;
    private String description;
    private String descriptionTitle;
    private String eventId;
    private List<URL> pictures;
    private Map<LocalDate, Map<String, Long>> viewsDetails = new HashMap<>();
    private long views = 0;
    private long trendingViews = 0;
    private Boolean reported = false;
    private long numberOfReports = 0;
    private long premiumViews = 0;
    private LocalDate creationDate = LocalDate.now();
    private List<InteractionEvent> likes = new ArrayList<>();
}
