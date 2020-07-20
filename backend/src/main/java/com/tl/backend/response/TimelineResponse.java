package com.tl.backend.response;

import com.tl.backend.models.Event;
import com.tl.backend.models.InteractionEvent;
import com.tl.backend.models.User;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

import java.net.URL;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
public class TimelineResponse {

    private String id;
    private UserResponse user;
    private String description;
    private String descriptionTitle;
    private String event;
    private long views;
    private long trendingViews;
    private long premiumViews;
    private LocalDate creationDate;
    private boolean active;
    private String category;
    private List<InteractionEvent> likes;
    private List<URL> pictures;
    private long numberOfReports;
}
