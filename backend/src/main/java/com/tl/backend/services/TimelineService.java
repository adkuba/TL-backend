package com.tl.backend.services;

import com.tl.backend.entities.Timeline;

import java.util.Optional;

public interface TimelineService {

    Optional<Timeline> getByTimelineId(String id);

    Timeline saveTimeline(Timeline timeline);

    void deleteByTimelineId(String id);
}
