package com.tl.backend.response;

import com.tl.backend.models.Event;
import com.tl.backend.models.User;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

import java.net.URL;
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
    private String category;
    private long trendingViews;
    private long likes;
    private List<URL> pictures;
}
