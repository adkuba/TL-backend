package com.tl.backend.services;

import com.tl.backend.models.Event;

import java.util.List;

public interface EventService {

    List<Event> getEventsByTimelineId(String timelineId);

    Event saveEvent(Event event);

    void deleteByEventId(String id);
}
