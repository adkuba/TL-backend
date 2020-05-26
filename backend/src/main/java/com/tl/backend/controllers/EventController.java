package com.tl.backend.controllers;

import com.tl.backend.models.Event;
import com.tl.backend.mappers.EventsMapper;
import com.tl.backend.services.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventService eventService;
    private final EventsMapper eventsMapper;

    @Autowired
    public EventController(EventService eventService, EventsMapper eventsMapper){
        this.eventService = eventService;
        this.eventsMapper = eventsMapper;
    }

    @PostMapping(consumes = {"application/json"})
    public ResponseEntity<Event> createEvent(@RequestBody @Valid @NotNull Event event){
        Event createdEvent = eventService.saveEvent(event);
        return new ResponseEntity<>(createdEvent, HttpStatus.CREATED);
    }

    @PostMapping(value = "/{id}/pictures", consumes = {"multipart/form-data"})
    public ResponseEntity<?> addPictures(@PathVariable String id, @Valid @NotNull @RequestParam List<MultipartFile> pictures){
        Event picEvent = eventService.setPictures(id, pictures);
        return new ResponseEntity<>(picEvent, HttpStatus.CREATED);
    }

    @DeleteMapping(value = "/{id}")
    public ResponseEntity<?> deleteEvent(@PathVariable String id){
        eventService.deleteByEventId(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping(value = "/public", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getEventsByTimelineId(@RequestParam String timelineId){
        List<Event> events = eventService.getEventsByTimelineId(timelineId);
        return ResponseEntity.ok(eventsMapper.eventResponse(events));
    }
}
