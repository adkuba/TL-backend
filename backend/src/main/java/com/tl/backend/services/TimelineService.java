package com.tl.backend.services;

import com.tl.backend.entities.Timeline;
import com.tl.backend.entities.User;

import java.util.Optional;

public interface TimelineService {

    Optional<Timeline> getTimelineById(String id);

    Timeline getTimelineByEventId(String eventId);

    Timeline saveTimeline(Timeline timeline);

    void deleteByTimelineId(String id);
}
