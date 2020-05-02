package com.tl.backend.services;

import com.tl.backend.entities.Timeline;
import com.tl.backend.repositories.TimelineRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class TimelineServiceImpl implements TimelineService {

    private final TimelineRepository timelineRepository;

    @Autowired
    public TimelineServiceImpl(TimelineRepository timelineRepository){
        this.timelineRepository = timelineRepository;
    }

    @Override
    public Optional<Timeline> getByTimelineId(String id) {
        return timelineRepository.findById(id);
    }

    @Override
    public Timeline saveTimeline(Timeline timeline) {
        return timelineRepository.save(timeline);
    }

    @Override
    public void deleteByTimelineId(String id) {
        timelineRepository.deleteById(id);
    }
}
