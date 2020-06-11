package com.tl.backend.services;

import com.tl.backend.models.Timeline;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

public interface TimelineService {

    Optional<Timeline> getTimelineById(String id);

    Timeline getTimelineByEventId(String eventId);

    Timeline saveTimeline(Timeline timeline);

    void deleteMineTimelineById(String id, Boolean delPictures);

    Timeline setPictures(String id, List<MultipartFile> multipartFiles);

    Timeline setPicturesURL(String id, List<String> urls);

    List<Timeline> getUserTimelines(String username);

    List<Timeline> randomTimelines();

    List<Timeline> newTimelines();

    List<Timeline> popularTimelines();

    List<Timeline> trendingTimelines();

    List<Timeline> getAllUserTimelines();

    List<String> likeTimeline(String timelineId, String username);

    List<String> dislikeTimeline(String timelineId, String username);
}
