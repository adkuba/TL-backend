package com.tl.backend.controllers;

import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.tl.backend.models.Review;
import com.tl.backend.models.Statistics;
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

@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {

    private final StatisticsServiceImpl statisticsService;
    private final DeviceInfoServiceImpl deviceInfoService;

    @Autowired
    public StatisticsController(DeviceInfoServiceImpl deviceInfoService, StatisticsServiceImpl statisticsService){
        this.statisticsService = statisticsService;
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
        return new ResponseEntity<>(deviceInfoService.getLocations(id), HttpStatus.OK);
    }

    @GetMapping(value = "/timeline-views/{id}")
    public ResponseEntity<?> getViews(@PathVariable String id){
        return new ResponseEntity<>(deviceInfoService.getViews(id), HttpStatus.OK);
    }
}
