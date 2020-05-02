package com.tl.backend.services;

import com.tl.backend.entities.Event;

public interface EventService {

    Iterable<Event> listAllEvents();

    Event saveEvent(Event event);

    void deleteByEventId(String id);
}
