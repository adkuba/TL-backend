package com.tl.backend.controllers;

import com.tl.backend.models.Event;
import com.tl.backend.services.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

@RestController
@RequestMapping("/events")
public class EventController {

    private final EventService eventService;

    @Autowired
    public EventController(EventService eventService){
        this.eventService = eventService;
    }

    @PostMapping(consumes = {"application/json"})
    public ResponseEntity<Event> createEvent(@RequestBody @Valid @NotNull Event event){
        eventService.saveEvent(event);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @DeleteMapping(value = "/{id}")
    public ResponseEntity<?> deleteEvent(@PathVariable String id){
        eventService.deleteByEventId(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Event> getEventsByTimelineId(@RequestParam String timelineId){
        return eventService.getEventsByTimelineId(timelineId);
    }
}
