package com.tl.backend.controllers;

import com.tl.backend.models.Event;
import com.tl.backend.mappers.EventsMapper;
import com.tl.backend.models.Timeline;
import com.tl.backend.response.EventResponse;
import com.tl.backend.services.EventService;
import com.tl.backend.services.TimelineService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventService eventService;
    private final EventsMapper eventsMapper;
    private final TimelineService timelineService;

    @Autowired
    public EventController(EventService eventService, EventsMapper eventsMapper, TimelineService timelineService){
        this.eventService = eventService;
        this.eventsMapper = eventsMapper;
        this.timelineService = timelineService;
    }

    @PostMapping(consumes = {"application/json"})
    public ResponseEntity<Event> createEvent(@RequestBody @Valid @NotNull Event event){
        Event createdEvent = eventService.saveEvent(event);
        return new ResponseEntity<>(createdEvent, HttpStatus.CREATED);
    }

    @PostMapping(value = "/multiple",consumes = {"application/json"})
    public ResponseEntity<List<EventResponse>> createEvents(@RequestBody @Valid @NotNull List<Event> events){
        List<Event> createdEvents = new ArrayList<>();
        for (Event event : events){
            createdEvents.add(eventService.saveEvent(event));
        }
        return new ResponseEntity<>(eventsMapper.eventResponse(createdEvents), HttpStatus.CREATED);
    }

    @PostMapping(value = "/{id}/pictures", consumes = {"multipart/form-data"})
    public ResponseEntity<?> addPictures(@PathVariable String id, @Valid @NotNull @RequestParam List<MultipartFile> pictures){
        Event picEvent = eventService.setPictures(id, pictures);
        return new ResponseEntity<>(picEvent, HttpStatus.CREATED);
    }

    @PostMapping(value = "/{id}/picturesURL")
    public ResponseEntity<?> addPicturesURL(@PathVariable String id, @Valid @RequestParam List<String> picturesURL){
        Event picEvent = eventService.setPicturesURL(id, picturesURL);
        return new ResponseEntity<>(picEvent, HttpStatus.CREATED);
    }

    @DeleteMapping(value = "/{id}")
    public ResponseEntity<?> deleteEvent(@PathVariable String id){
        eventService.deleteByEventId(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping(value = "/public", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getEventsByTimelineId(@RequestParam String timelineId, @RequestParam(required = false) Boolean view){
        List<Event> events;
        if (view != null){
            events = eventService.getEventsByTimelineId(timelineId, view);
        } else {
            events = eventService.getEventsByTimelineId(timelineId, false);
        }
        return ResponseEntity.ok(eventsMapper.eventResponse(events));
    }

    @GetMapping(value = "/allSubEventsByMainTimelineId", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getAllSubEventsByMainTimelineId(@RequestParam String timelineId){
        List<Event> events = eventService.getEventsByTimelineId(timelineId, false);
        List<List<EventResponse>> subEvents = new ArrayList<>();
        for (Event event : events){
            Timeline subTimeline = timelineService.getTimelineByEventId(event.getId());
            List<EventResponse> pom = new ArrayList<>();
            if (subTimeline != null){
                pom = eventsMapper.eventResponse(eventService.getEventsByTimelineId(subTimeline.getId(), false));
            }
            subEvents.add(pom);
        }
        return ResponseEntity.ok(subEvents);
    }
}
