package com.tl.backend.controllers;

import com.tl.backend.models.Timeline;
import com.tl.backend.mappers.TimelineMapper;
import com.tl.backend.response.TimelineResponse;
import com.tl.backend.services.TimelineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/timelines")
public class TimelineController {

    private final TimelineService timelineService;
    private final TimelineMapper timelineMapper;

    @Autowired
    public TimelineController(TimelineService timelineService, TimelineMapper timelineMapper){
        this.timelineService = timelineService;
        this.timelineMapper = timelineMapper;
    }

    @PostMapping(consumes = {"application/json"})
    public ResponseEntity<Timeline> createTimeline(@RequestBody @Valid @NotNull Timeline timeline){
        Timeline createdTimeline = timelineService.saveTimeline(timeline);
        return new ResponseEntity<>(createdTimeline, HttpStatus.CREATED);
    }

    @PostMapping(value = "/{id}/pictures", consumes = {"multipart/form-data"})
    public ResponseEntity<?> addPictures(@PathVariable String id, @Valid @NotNull @RequestParam List<MultipartFile> pictures){
        Timeline picTimeline = timelineService.setPictures(id, pictures);
        return new ResponseEntity<>(picTimeline, HttpStatus.CREATED);
    }

    @DeleteMapping(value = "/{id}")
    public ResponseEntity<?> deleteTimeline(@PathVariable String id){
        timelineService.deleteByTimelineId(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping(value = "/public/{username}")
    public List<Timeline> userTimelines(@PathVariable String username){
        return timelineService.getUserTimelines(username);
    }

    @GetMapping(value = "/public/random")
    public List<TimelineResponse> randomTimelines(){
        return timelineMapper.timelinesResponse(timelineService.randomTimelines());
    }

    @GetMapping(value ="/public", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getTimelineById(@RequestParam String id){
        Optional<Timeline> optionalTimeline = timelineService.getTimelineById(id);
        if (optionalTimeline.isPresent()){
            return ResponseEntity.ok(timelineMapper.timelineResponse(optionalTimeline.get()));
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    @GetMapping(value = "/public/event", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getTimelineByEventId(@RequestParam String eventId){
        Timeline timeline = timelineService.getTimelineByEventId(eventId);
        if (timeline == null){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return ResponseEntity.ok(timelineMapper.timelineResponse(timeline));
    }
}
