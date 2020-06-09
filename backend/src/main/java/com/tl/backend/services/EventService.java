package com.tl.backend.services;

import com.tl.backend.models.Event;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface EventService {

    List<Event> getEventsByTimelineId(String timelineId);

    Event saveEvent(Event event);

    void deleteByEventId(String id);

    Event setPictures(String id, List<MultipartFile> picture);

    Event setPicturesURL(String id, List<String> urls);
}
