package com.tl.backend.services;

import com.tl.backend.entities.Timeline;
import com.tl.backend.entities.User;
import com.tl.backend.repositories.TimelineRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TimelineServiceImpl implements TimelineService {

    private final TimelineRepository timelineRepository;

    @Autowired
    public TimelineServiceImpl(TimelineRepository timelineRepository){
        this.timelineRepository = timelineRepository;
    }

    @Override
    public Timeline getTimelineByUsername(String username) {
        List<Timeline> timelines = timelineRepository.findAll();
        for(Timeline timeline : timelines){
            if (timeline.getUser().getUsername().equals(username)){
                return timeline;
            }
        }
        return null;
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
