package com.tl.backend.controllers;

import com.tl.backend.models.Timeline;
import com.tl.backend.elastic.TimelineElasticRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/test")
public class TestController {

    private final TimelineElasticRepository timelineElasticRepository;

    @Autowired
    public TestController(TimelineElasticRepository timelineElasticRepository){
        this.timelineElasticRepository = timelineElasticRepository;
    }

    @PostMapping("/timeline")
    public void saveTimeline(@RequestBody @Valid Timeline timeline){
        timelineElasticRepository.save(timeline);
    }


    @GetMapping("/timeline/{title}")
    public Page<Timeline> findByTitle(@PathVariable("title") String title){
        return timelineElasticRepository.findByDescriptionTitle(title, PageRequest.of(0, 10));
    }
}
