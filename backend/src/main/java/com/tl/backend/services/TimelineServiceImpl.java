package com.tl.backend.services;

import com.tl.backend.fileHandling.FileResource;
import com.tl.backend.fileHandling.FileServiceImpl;
import com.tl.backend.models.Timeline;
import com.tl.backend.repositories.TimelineRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class TimelineServiceImpl implements TimelineService {

    private final TimelineRepository timelineRepository;
    private final FileServiceImpl fileService;

    @Autowired
    public TimelineServiceImpl(TimelineRepository timelineRepository, FileServiceImpl fileService){
        this.timelineRepository = timelineRepository;
        this.fileService = fileService;
    }

    @Override
    public Optional<Timeline> getTimelineById(String id) {
        return timelineRepository.findById(id);
    }

    @Override
    public Timeline getTimelineByEventId(String eventId) {
        List<Timeline> timelines = timelineRepository.findAll();
        for (Timeline timeline : timelines){
            if (timeline.getEvent() != null) {
                if (timeline.getEvent().getId().equals(eventId)){
                    return timeline;
                }
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

    @Override
    public Timeline setPictures(String id, List<MultipartFile> multipartFiles) {
        Optional<Timeline> optionalTimeline = timelineRepository.findById(id);
        if (optionalTimeline.isPresent()){
            Timeline timeline = optionalTimeline.get();
            List<FileResource> fileResources = new ArrayList<>();
            for (MultipartFile file : multipartFiles){
                fileResources.add(fileService.saveFileResource(file));
            }
            timeline.setPictures(fileResources);
            return timelineRepository.save(timeline);
        }
        return null;
    }

    @Override
    public List<Timeline> getUserTimelines(String username) {
        List<Timeline> allTimelines = timelineRepository.findAll();
        List<Timeline> timelines = new ArrayList<>();
        for (Timeline timeline : allTimelines){
            if (timeline.getUser() != null){
                if (timeline.getUser().getUsername().equals(username)) {
                    timelines.add(timeline);
                }
            }
        }
        return timelines;
    }
}
