package com.tl.backend.controllers;

import com.tl.backend.models.Statistics;
import com.tl.backend.services.StatisticsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseEntity<?> allStats(){
        return new ResponseEntity<>(statisticsService.getAllStats(), HttpStatus.OK);
    }
}
