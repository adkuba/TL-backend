package com.tl.backend.services;

import com.stripe.exception.StripeException;
import com.tl.backend.models.Timeline;
import com.tl.backend.request.HomepageRequest;
import com.tl.backend.response.TimelineResponse;
import org.bson.types.ObjectId;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

public interface TimelineService {

    Timeline getTimelineById(String id, String username) throws StripeException;

    Timeline getTimelineByEventId(String eventId);

    Timeline saveTimeline(Timeline timeline);

    void deleteMineTimelineById(String id, Boolean delPictures, String reason);

    Timeline setPictures(String id, List<MultipartFile> multipartFiles);

    Timeline setPicturesURL(String id, List<String> urls);

    List<Timeline> getUserTimelines(String username);

    List<Timeline> randomTimelines(List<String> seenIDS);

    List<Timeline> newTimelines(List<String> seenIDS);

    List<Timeline> popularTimelines(List<String> seenIDS);

    List<Timeline> trendingTimelines(List<String> seenIDS);

    List<TimelineResponse> getHomepageTimelines(HomepageRequest homepageRequest);

    List<Timeline> premiumTimelines(List<String> seenIDS);

    void addPremiumView(String timelineId);

    void reportTimeline(String timelineId);

    void unReportTimeline(String timelineId);

    void deleteUserTimelines(String username);

    void makeActive(List<String> active, String username);

    List<Timeline> getReported();

    List<Timeline> getAllUserTimelines();

    List<String> likeTimeline(String timelineId, String username);

    List<String> dislikeTimeline(String timelineId, String username);

    List<Timeline> searchTimelines(String text);
}
