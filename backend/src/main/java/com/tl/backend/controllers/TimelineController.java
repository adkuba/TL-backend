package com.tl.backend.controllers;

import com.stripe.exception.StripeException;
import com.tl.backend.mappers.UserMapper;
import com.tl.backend.models.Timeline;
import com.tl.backend.mappers.TimelineMapper;
import com.tl.backend.models.User;
import com.tl.backend.repositories.TimelineRepository;
import com.tl.backend.repositories.UserRepository;
import com.tl.backend.response.TimelineResponse;
import com.tl.backend.response.UserResponse;
import com.tl.backend.services.TimelineService;
import com.tl.backend.services.UserService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
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
    private final UserService userService;
    private final UserMapper userMapper;

    @Autowired
    public TimelineController(UserMapper userMapper, UserService userService, TimelineService timelineService, TimelineMapper timelineMapper, UserRepository userRepository, TimelineRepository timelineRepository){
        this.timelineService = timelineService;
        this.userMapper = userMapper;
        this.userService = userService;
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
            timelineService.deleteMineTimelineById(timeline.getId(), false, null);
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
    public ResponseEntity<?> deleteMainTimeline(Authentication authentication, @PathVariable String id, @RequestParam(required = false) String reason){
        boolean isAdmin = false;
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        for (GrantedAuthority grantedAuthority : authorities){
            if (grantedAuthority.getAuthority().equals("ROLE_ADMIN")){
                isAdmin = true;
                break;
            }
        }
        Optional<Timeline> optionalTimeline = timelineRepository.findById(id);
        if (optionalTimeline.isPresent()){
            if (isAdmin || optionalTimeline.get().getUser().getUsername().equals(authentication.getName())){
                timelineService.deleteMineTimelineById(id, true, reason);
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    @DeleteMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteUserTimelines(@RequestParam String username){
        timelineService.deleteUserTimelines(username);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping(value = "/get-reported")
    public ResponseEntity<?> getReported(){
        return new ResponseEntity<>(timelineMapper.timelinesResponse(timelineService.getReported()), HttpStatus.OK);
    }

    @GetMapping(value = "/public/{username}")
    public List<TimelineResponse> userTimelines(@PathVariable String username){
        return timelineMapper.timelinesResponse(timelineService.getUserTimelines(username));
    }

    @GetMapping(value = "/public/homepage/special")
    public ResponseEntity<?> homepageSpecial(){
        Random r = new Random();
        switch (r.nextInt((3 - 1) + 1) + 1){
            case 1:
                List<TimelineResponse> popularTimelines = timelineMapper.timelinesResponse(timelineService.popularTimelines());
                Collections.shuffle(popularTimelines);
                if (popularTimelines.size() > 5){
                    popularTimelines = popularTimelines.subList(0, 5);
                }
                return new ResponseEntity<>(popularTimelines, HttpStatus.OK);
            case 2:
                List<UserResponse> newUsers = userMapper.usersResponse(userService.getNewUsers());
                Collections.shuffle(newUsers);
                if (newUsers.size() > 5){
                    newUsers = newUsers.subList(0, 5);
                }
                return new ResponseEntity<>(newUsers, HttpStatus.OK);
            case 3:
                List<UserResponse> randomUsers = userMapper.usersResponse(userService.getRandomUsers());
                Collections.shuffle(randomUsers);
                return new ResponseEntity<>(randomUsers, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    @GetMapping(value = "/public/homepage")
    public List<TimelineResponse> homepageTimelines(){
        List<TimelineResponse> timelineResponses = new ArrayList<>();
        Random rand = new Random();
        List<TimelineResponse> randomTimelines = timelineMapper.timelinesResponse(timelineService.randomTimelines());
        List<TimelineResponse> newTimelines = timelineMapper.timelinesResponse(timelineService.newTimelines());
        List<TimelineResponse> popularTimelines = timelineMapper.timelinesResponse(timelineService.popularTimelines());
        List<TimelineResponse> trendingTimelines = timelineMapper.timelinesResponse(timelineService.trendingTimelines());
        List<TimelineResponse> premiumTimelines = timelineMapper.timelinesResponse(timelineService.premiumTimelines());

        for (TimelineResponse timelineResponse : randomTimelines){
            timelineResponse.setCategory("SUGGESTED");
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
        for (int i=0; i<2; i++){
            if (premiumTimelines.size() > 0){
                int randomIndex = rand.nextInt(premiumTimelines.size());
                TimelineResponse timelineResponse = premiumTimelines.get(randomIndex);
                timelineResponse.setCategory("PREMIUM");
                timelineResponses.add(timelineResponse);
                premiumTimelines.remove(randomIndex);
            }
        }

        Collections.shuffle(timelineResponses);
        return timelineResponses;
    }

    @GetMapping(value ="/public", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getTimelineById(@RequestParam(required = false) String username, @NotNull @RequestParam String id) throws StripeException {
        return ResponseEntity.ok(timelineMapper.timelineResponse(timelineService.getTimelineById(id, username)));
    }

    @GetMapping(value ="/editor", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getTimelineByIdEditor(@RequestParam(required = false) String username, @NotNull @RequestParam String id) throws StripeException {
        return ResponseEntity.ok(timelineMapper.fullTimelineResponse(timelineService.getTimelineById(id, username)));
    }

    @PostMapping(value = "/public/report")
    public void reportTimeline(@RequestParam String id){
        timelineService.reportTimeline(id);
    }

    @PostMapping(value = "/un-report")
    public void unReportTimeline(@RequestParam String id){
        timelineService.unReportTimeline(id);
    }

    @PostMapping(value = "/public/premium-view")
    public void premiumView(@RequestParam String id){
        timelineService.addPremiumView(id);
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
