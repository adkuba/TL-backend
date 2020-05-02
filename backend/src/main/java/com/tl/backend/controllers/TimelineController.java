package com.tl.backend.controllers;

import com.tl.backend.entities.Timeline;
import com.tl.backend.services.TimelineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Optional;

@RestController
@RequestMapping("/timelines")
public class TimelineController {

    private final TimelineService timelineService;

    @Autowired
    public TimelineController(TimelineService timelineService){
        this.timelineService = timelineService;
    }

    @PostMapping(consumes = {"application/json"})
    public ResponseEntity<Timeline> createTimeline(@RequestBody @Valid @NotNull Timeline timeline){
        timelineService.saveTimeline(timeline);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @DeleteMapping(value = "/{id}")
    public ResponseEntity<?> deleteTimeline(@PathVariable String id){
        timelineService.deleteByTimelineId(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Optional<Timeline> listTimeline(@RequestParam("id") String id){
        return timelineService.getByTimelineId(id);
    }
}
