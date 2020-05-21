package com.tl.backend.services;

import com.tl.backend.models.Event;
import com.tl.backend.repositories.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;

    @Autowired
    public EventServiceImpl(EventRepository eventRepository){
        this.eventRepository = eventRepository;
    }


    @Override
    public List<Event> getEventsByTimelineId(String timelineId) {
        List<Event> events = new ArrayList<>();
        List<Event> allEvents = eventRepository.findAll();
        for(Event event : allEvents){
            if(event.getTimeline().getId().equals(timelineId)){
                events.add(event);
            }
        }
        return events;
    }

    @Override
    public Event saveEvent(Event event) {
        return eventRepository.save(event);
    }

    @Override
    public void deleteByEventId(String id) {
        eventRepository.deleteById(id);
    }
}
