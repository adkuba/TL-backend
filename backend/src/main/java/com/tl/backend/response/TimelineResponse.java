package com.tl.backend.response;

import com.tl.backend.models.Event;
import com.tl.backend.models.User;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.net.URL;
import java.util.List;

@Data
@NoArgsConstructor
public class TimelineResponse {
    private String id;

    private User user;
    private String description;
    private String descriptionTitle;
    private Event event;
    private List<URL> pictures;
}
