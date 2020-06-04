package com.tl.backend.services;

import com.tl.backend.fileHandling.FileResource;
import com.tl.backend.fileHandling.FileServiceImpl;
import com.tl.backend.models.Event;
import com.tl.backend.models.Timeline;
import com.tl.backend.repositories.EventRepository;
import com.tl.backend.repositories.TimelineRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.SampleOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class TimelineServiceImpl implements TimelineService {

    private final TimelineRepository timelineRepository;
    private final EventRepository eventRepository;
    private final FileServiceImpl fileService;
    private final MongoTemplate mongoTemplate;

    @Autowired
    public TimelineServiceImpl(TimelineRepository timelineRepository, FileServiceImpl fileService, MongoTemplate mongoTemplate, EventRepository eventRepository){
        this.timelineRepository = timelineRepository;
        this.fileService = fileService;
        this.mongoTemplate = mongoTemplate;
        this.eventRepository = eventRepository;
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
    public void deleteMineTimelineById(String id) {
        Optional<Timeline> optionalTimeline = timelineRepository.findById(id);
        if (optionalTimeline.isPresent()){
            Timeline timeline = optionalTimeline.get();
            List<Event> events = eventRepository.findAllByTimelineId(timeline.getId());

            for (Event event : events){
                Optional<Timeline> optionalSubTimeline = timelineRepository.findOneByEventId(new ObjectId(event.getId()));
                if (optionalSubTimeline.isPresent()){
                    Timeline subTimeline = optionalSubTimeline.get();
                    List<Event> subEvents = eventRepository.findAllByTimelineId(subTimeline.getId());

                    for (Event subEvent : subEvents){
                        eventRepository.delete(subEvent);
                    }
                    timelineRepository.delete(subTimeline);
                }
                eventRepository.delete(event);
            }
            timelineRepository.delete(timeline);
        }
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

    @Override
    public List<Timeline> randomTimelines() {
        SampleOperation matchStage = Aggregation.sample(5);
        MatchOperation matchOperation = Aggregation.match(Criteria.where("user").exists(true));
        Aggregation aggregation = Aggregation.newAggregation(matchStage, matchOperation);
        AggregationResults<Timeline> timelines = mongoTemplate.aggregate(aggregation, "timelines", Timeline.class);
        return timelines.getMappedResults();
    }
}
