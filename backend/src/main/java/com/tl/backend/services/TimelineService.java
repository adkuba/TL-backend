package com.tl.backend.services;

import com.tl.backend.entities.Timeline;
import com.tl.backend.entities.User;

import java.util.Optional;

public interface TimelineService {

    //pobiera glowny timeline usera
    Timeline getTimelineByUsername(String username);

    Timeline saveTimeline(Timeline timeline);

    void deleteByTimelineId(String id);
}
