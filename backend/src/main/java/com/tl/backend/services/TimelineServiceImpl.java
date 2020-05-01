package com.tl.backend.services;

import com.tl.backend.repositories.TimelineRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TimelineServiceImpl implements TimelineService {

    private final TimelineRepository timelineRepository;

    @Autowired
    public TimelineServiceImpl(TimelineRepository timelineRepository){
        this.timelineRepository = timelineRepository;
    }
}
