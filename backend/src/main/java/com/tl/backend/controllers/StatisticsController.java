package com.tl.backend.controllers;

import com.tl.backend.models.Review;
import com.tl.backend.models.Statistics;
import com.tl.backend.services.StatisticsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;

@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {

    private final StatisticsServiceImpl statisticsService;

    @Autowired
    public StatisticsController(StatisticsServiceImpl statisticsService){
        this.statisticsService = statisticsService;
    }

    @PostMapping(value = "/public")
    public ResponseEntity<?> homepageView(){
        statisticsService.incrementHomepageViews();
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping(value = "/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> allStats(){
        return new ResponseEntity<>(statisticsService.getAllStats(), HttpStatus.OK);
    }

    @PostMapping(value = "/public/review")
    public ResponseEntity<?> createReview(@RequestBody @NotNull Review review){
        statisticsService.addReview(review);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
