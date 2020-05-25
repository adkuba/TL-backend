package com.tl.backend.services;

import com.tl.backend.models.Event;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface EventService {

    List<Event> getEventsByTimelineId(String timelineId);

    Event saveEvent(Event event);

    void deleteByEventId(String id);

    public Event setPicture(String id, MultipartFile picture);
}
