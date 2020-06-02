package com.tl.backend.services;

import com.tl.backend.models.Timeline;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

public interface TimelineService {

    Optional<Timeline> getTimelineById(String id);

    Timeline getTimelineByEventId(String eventId);

    Timeline saveTimeline(Timeline timeline);

    void deleteByTimelineId(String id);

    Timeline setPictures(String id, List<MultipartFile> multipartFiles);

    List<Timeline> getUserTimelines(String username);

    List<Timeline> randomTimelines();
}
