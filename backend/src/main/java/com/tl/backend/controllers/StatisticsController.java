package com.tl.backend.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.tl.backend.models.Review;
import com.tl.backend.models.Statistics;
import com.tl.backend.models.Timeline;
import com.tl.backend.models.User;
import com.tl.backend.repositories.TimelineRepository;
import com.tl.backend.repositories.UserRepository;
import com.tl.backend.services.DeviceInfoServiceImpl;
import com.tl.backend.services.StatisticsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.Optional;

@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {

    private final StatisticsServiceImpl statisticsService;
    private final DeviceInfoServiceImpl deviceInfoService;
    private final TimelineRepository timelineRepository;
    private final UserRepository userRepository;

    @Autowired
    public StatisticsController(UserRepository userRepository, TimelineRepository timelineRepository, DeviceInfoServiceImpl deviceInfoService, StatisticsServiceImpl statisticsService){
        this.statisticsService = statisticsService;
        this.userRepository = userRepository;
        this.timelineRepository = timelineRepository;
        this.deviceInfoService = deviceInfoService;
    }

    @PostMapping(value = "/public")
    public ResponseEntity<?> homepageView(HttpServletRequest request) {
        statisticsService.incrementHomepageViews(request);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping(value = "/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> allStats(){
        return new ResponseEntity<>(statisticsService.getAllStats(), HttpStatus.OK);
    }

    @GetMapping(value = "/all-devices")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> allDevices() {
        return new ResponseEntity<>(deviceInfoService.getAll(), HttpStatus.OK);
    }

    @PostMapping(value = "/public/review")
    public ResponseEntity<?> createReview(@RequestBody @NotNull Review review){
        statisticsService.addReview(review);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping(value = "/timeline/{id}")
    public ResponseEntity<?> getLocations(@PathVariable String id){
        Optional<Timeline> optionalTimeline = timelineRepository.findById(id);
        if (optionalTimeline.isPresent()){
            return new ResponseEntity<>(deviceInfoService.getLocations(optionalTimeline.get().getViewsDetails()), HttpStatus.OK);
        }
        return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
    }

    @GetMapping(value = "/timeline-views/{id}")
    public ResponseEntity<?> getViews(@PathVariable String id){
        Optional<Timeline> optionalTimeline = timelineRepository.findById(id);
        if (optionalTimeline.isPresent()){
            return new ResponseEntity<>(deviceInfoService.getViews(optionalTimeline.get().getViewsDetails()), HttpStatus.OK);
        }
        return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
    }

    @GetMapping(value = "/profile-locations/{username}")
    public ResponseEntity<?> getProfileLocations(@PathVariable String username){
        Optional<User> optionalUser = userRepository.findByUsername(username);
        if (optionalUser.isPresent()){
            return new ResponseEntity<>(deviceInfoService.getLocations(optionalUser.get().getProfileViews()), HttpStatus.OK);
        }
        return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
    }

    @GetMapping(value = "/profile-views/{username}")
    public ResponseEntity<?> getProfileViews(@PathVariable String username){
        Optional<User> optionalUser = userRepository.findByUsername(username);
        if (optionalUser.isPresent()){
            return new ResponseEntity<>(deviceInfoService.getViews(optionalUser.get().getProfileViews()), HttpStatus.OK);
        }
        return new ResponseEntity<>(null, HttpStatus.OK);
    }

    @GetMapping(value = "/create-backup")
    public ResponseEntity<?> createBackup() throws IOException {
        statisticsService.createBackup();
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping(value = "/restore-backup")
    public ResponseEntity<?> restoreBackup() throws JsonProcessingException {
        statisticsService.restoreFromBackup();
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
