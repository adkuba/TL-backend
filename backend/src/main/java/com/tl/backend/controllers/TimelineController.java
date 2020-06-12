package com.tl.backend.controllers;

import com.tl.backend.models.Timeline;
import com.tl.backend.mappers.TimelineMapper;
import com.tl.backend.models.User;
import com.tl.backend.repositories.TimelineRepository;
import com.tl.backend.repositories.UserRepository;
import com.tl.backend.response.TimelineResponse;
import com.tl.backend.services.TimelineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.*;

@RestController
@RequestMapping("/api/timelines")
public class TimelineController {

    private final TimelineService timelineService;
    private final TimelineMapper timelineMapper;
    private final UserRepository userRepository;
    private final TimelineRepository timelineRepository;

    @Autowired
    public TimelineController(TimelineService timelineService, TimelineMapper timelineMapper, UserRepository userRepository, TimelineRepository timelineRepository){
        this.timelineService = timelineService;
        this.timelineMapper = timelineMapper;
        this.userRepository = userRepository;
        this.timelineRepository = timelineRepository;
    }

    @PostMapping(consumes = {"application/json"})
    public ResponseEntity<Timeline> createTimeline(Authentication authentication, @RequestBody @Valid @NotNull Timeline timeline, @RequestParam Boolean findUser, @RequestParam Boolean withDelete, @RequestParam Boolean add){
        if (add){
            if (timelineRepository.existsById(timeline.getId())){
                return new ResponseEntity<>(HttpStatus.CONFLICT);
            }
        }
        if (withDelete){
            timelineService.deleteMineTimelineById(timeline.getId(), false);
        }
        if (findUser){
            Optional<User> optionalUser = userRepository.findByUsername(authentication.getName());
            optionalUser.ifPresent(timeline::setUser);
        }
        Timeline createdTimeline = timelineService.saveTimeline(timeline);
        return new ResponseEntity<>(createdTimeline, HttpStatus.CREATED);
    }

    @PostMapping(value = "/multiple", consumes = {"application/json"})
    public ResponseEntity<List<TimelineResponse>> createTimelines(@RequestBody @Valid @NotNull List<Timeline> timelines){
        List<Timeline> createdTimelines = new ArrayList<>();
        for (Timeline timeline : timelines){
            createdTimelines.add(timelineService.saveTimeline(timeline));
        }
        return new ResponseEntity<>(timelineMapper.timelinesResponse(createdTimelines), HttpStatus.OK);
    }

    @PostMapping(value = "/{id}/pictures", consumes = {"multipart/form-data"})
    public ResponseEntity<?> addPictures(@PathVariable String id, @Valid @NotNull @RequestParam List<MultipartFile> pictures){
        Timeline picTimeline = timelineService.setPictures(id, pictures);
        return new ResponseEntity<>(picTimeline, HttpStatus.CREATED);
    }

    @PostMapping(value = "/{id}/picturesURL")
    public ResponseEntity<?> addPicturesURL(@PathVariable String id, @Valid @RequestParam List<String> picturesURL){
        Timeline picTimeline = timelineService.setPicturesURL(id, picturesURL);
        return new ResponseEntity<>(picTimeline, HttpStatus.CREATED);
    }

    @PostMapping(value = "/{id}/like")
    public ResponseEntity<?> likeTiemline(Authentication authentication, @PathVariable String id){
        List<String> likes = timelineService.likeTimeline(id, authentication.getName());
        if (likes == null){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(likes, HttpStatus.OK);
    }

    @PostMapping(value = "/{id}/dislike")
    public ResponseEntity<?> dislikeTiemline(Authentication authentication, @PathVariable String id){
        List<String> likes = timelineService.dislikeTimeline(id, authentication.getName());
        if (likes == null){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(likes, HttpStatus.OK);
    }

    @DeleteMapping(value = "/{id}")
    public ResponseEntity<?> deleteMainTimeline(@PathVariable String id){
        timelineService.deleteMineTimelineById(id, true);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping(value = "/public/{username}")
    public List<Timeline> userTimelines(@PathVariable String username){
        return timelineService.getUserTimelines(username);
    }

    @GetMapping(value = "/public/homepage")
    public List<TimelineResponse> homepageTimelines(){
        List<TimelineResponse> timelineResponses = new ArrayList<>();
        Random rand = new Random();
        List<TimelineResponse> randomTimelines = timelineMapper.timelinesResponse(timelineService.randomTimelines());
        List<TimelineResponse> newTimelines = timelineMapper.timelinesResponse(timelineService.newTimelines());
        List<TimelineResponse> popularTimelines = timelineMapper.timelinesResponse(timelineService.popularTimelines());
        List<TimelineResponse> trendingTimelines = timelineMapper.timelinesResponse(timelineService.trendingTimelines());

        for (TimelineResponse timelineResponse : randomTimelines){
            timelineResponse.setCategory("RANDOM");
            timelineResponses.add(timelineResponse);
        }
        for (int i=0; i<2; i++){
            if (newTimelines.size() > 0){
                int randomIndex = rand.nextInt(newTimelines.size());
                TimelineResponse timelineResponse = newTimelines.get(randomIndex);
                timelineResponse.setCategory("NEW");
                timelineResponses.add(timelineResponse);
                newTimelines.remove(randomIndex);
            }
        }
        for (int i=0; i<2; i++){
            if (popularTimelines.size() > 0){
                int randomIndex = rand.nextInt(popularTimelines.size());
                TimelineResponse timelineResponse = popularTimelines.get(randomIndex);
                timelineResponse.setCategory("POPULAR");
                timelineResponses.add(timelineResponse);
                popularTimelines.remove(randomIndex);
            }
        }
        for (int i=0; i<2; i++){
            if (trendingTimelines.size() > 0){
                int randomIndex = rand.nextInt(trendingTimelines.size());
                TimelineResponse timelineResponse = trendingTimelines.get(randomIndex);
                timelineResponse.setCategory("TRENDING");
                timelineResponses.add(timelineResponse);
                trendingTimelines.remove(randomIndex);
            }
        }

        Collections.shuffle(timelineResponses);
        return timelineResponses;
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
        return ResponseEntity.ok(timelineMapper.timelineResponse(timeline));
    }

    @GetMapping(value = "/public/search")
    public ResponseEntity<?> searchTimelines(@RequestParam String text){
        return ResponseEntity.ok(timelineMapper.timelinesResponse(timelineService.searchTimelines(text)));
    }

    @GetMapping(value = "/all", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getAllTimelines(){
        return new ResponseEntity<>(timelineMapper.timelinesResponse(timelineService.getAllUserTimelines()), HttpStatus.OK);
    }
}
