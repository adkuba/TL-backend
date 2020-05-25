package com.tl.backend.services;

import com.tl.backend.fileHandling.FileResource;
import com.tl.backend.fileHandling.FileService;
import com.tl.backend.fileHandling.FileServiceImpl;
import com.tl.backend.models.Event;
import com.tl.backend.repositories.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final FileServiceImpl fileService;

    @Autowired
    public EventServiceImpl(EventRepository eventRepository, FileServiceImpl fileService){
        this.eventRepository = eventRepository;
        this.fileService = fileService;
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
    public Event setPicture(String id, MultipartFile picture){
        Optional<Event> opPicEvent = eventRepository.findById(id);
        if (opPicEvent.isPresent()){
            Event picEvent = opPicEvent.get();
            FileResource fileResource = fileService.saveFileResource(picture);
            picEvent.setPicture(fileResource);
            return eventRepository.save(picEvent);
        }
        return null;
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
