package com.tl.backend.services;

import com.stripe.exception.StripeException;
import com.stripe.model.Subscription;
import com.tl.backend.fileHandling.FileResource;
import com.tl.backend.fileHandling.FileResourceRepository;
import com.tl.backend.fileHandling.FileServiceImpl;
import com.tl.backend.models.Event;
import com.tl.backend.models.Timeline;
import com.tl.backend.models.User;
import com.tl.backend.repositories.EventRepository;
import com.tl.backend.repositories.TimelineRepository;
import com.tl.backend.repositories.UserRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.TextCriteria;
import org.springframework.data.mongodb.core.query.TextQuery;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.limit;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.sort;

@Service
public class TimelineServiceImpl implements TimelineService {

    private final TimelineRepository timelineRepository;
    private final EventRepository eventRepository;
    private final FileServiceImpl fileService;
    private final MongoTemplate mongoTemplate;
    private final UserRepository userRepository;
    private final FileResourceRepository fileResourceRepository;

    @Autowired
    public TimelineServiceImpl(UserRepository userRepository, FileResourceRepository fileResourceRepository, TimelineRepository timelineRepository, FileServiceImpl fileService, MongoTemplate mongoTemplate, EventRepository eventRepository){
        this.timelineRepository = timelineRepository;
        this.userRepository = userRepository;
        this.fileResourceRepository = fileResourceRepository;
        this.fileService = fileService;
        this.mongoTemplate = mongoTemplate;
        this.eventRepository = eventRepository;
    }

    @Override
    public Timeline getTimelineById(String id) throws StripeException {
        Optional<Timeline> optionalTimeline = timelineRepository.findById(id);
        if (optionalTimeline.isPresent()){
            Timeline timeline = optionalTimeline.get();
            //dodac wyjatek na admina!
            if (Period.between(timeline.getUser().getCreationTime(), LocalDate.now()).getDays() <= 30){
                return timeline;
            } else {
                Subscription subscription = Subscription.retrieve(timeline.getUser().getSubscriptionID());
                if (subscription.getStatus().equals("active")){
                    return timeline;
                }
            }
        }
        return null;
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
    public void deleteMineTimelineById(String id, Boolean delPictures) {
        Optional<Timeline> optionalTimeline = timelineRepository.findById(id);
        if (optionalTimeline.isPresent()){
            Timeline timeline = optionalTimeline.get();
            if (delPictures){
                deletePictures(timeline.getPictures());
            }

            List<Event> events = eventRepository.findAllByTimelineId(timeline.getId());
            for (Event event : events){
                Optional<Timeline> optionalSubTimeline = timelineRepository.findOneByEventId(new ObjectId(event.getId()));
                if (optionalSubTimeline.isPresent()){
                    Timeline subTimeline = optionalSubTimeline.get();
                    List<Event> subEvents = eventRepository.findAllByTimelineId(subTimeline.getId());

                    for (Event subEvent : subEvents){
                        if (delPictures){
                            deletePictures(subEvent.getPictures());
                        }
                        eventRepository.delete(subEvent);
                    }
                    timelineRepository.delete(subTimeline);
                }
                if (delPictures){
                    deletePictures(event.getPictures());
                }
                eventRepository.delete(event);
            }
            timelineRepository.delete(timeline);
        }
    }

    private void deletePictures(List<FileResource> pictures){
        if (pictures != null){
            for (FileResource fileResource : pictures){
                fileService.deleteFileResource(fileResource);
            }
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
    public Timeline setPicturesURL(String id, List<String> urls) {
        Optional<Timeline> optionalTimeline = timelineRepository.findById(id);
        if (optionalTimeline.isPresent()){
            Timeline timeline = optionalTimeline.get();
            List<FileResource> pictures = timeline.getPictures();
            for (String url : urls){
                String[] parts = url.split("/");
                Optional<FileResource> optionalFileResource = fileResourceRepository.findById(parts[parts.length-1]);
                if (optionalFileResource.isPresent()){
                    FileResource picture = optionalFileResource.get();
                    pictures.add(picture);
                }
            }
            timeline.setPictures(pictures);

            return timelineRepository.save(timeline);
        }
        return null;
    }

    @Override
    public List<Timeline> getUserTimelines(String username) {
        //NIEOPTYMALNE
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

    @Override
    public List<Timeline> newTimelines() {
        SortOperation sortByDate = sort(Sort.by(Sort.Direction.ASC, "creationDate"));
        LimitOperation limitTo = limit(10);
        MatchOperation matchOperation = Aggregation.match(Criteria.where("user").exists(true));
        Aggregation aggregation = Aggregation.newAggregation(sortByDate, limitTo, matchOperation);
        AggregationResults<Timeline> timelines = mongoTemplate.aggregate(aggregation, "timelines", Timeline.class);
        return timelines.getMappedResults();
    }

    @Override
    public List<Timeline> popularTimelines() {
        SortOperation sortByViews = sort(Sort.by(Sort.Direction.DESC, "views"));
        LimitOperation limitTo = limit(10);
        MatchOperation matchOperation = Aggregation.match(Criteria.where("user").exists(true));
        Aggregation aggregation = Aggregation.newAggregation(sortByViews, limitTo, matchOperation);
        AggregationResults<Timeline> timelines = mongoTemplate.aggregate(aggregation, "timelines", Timeline.class);
        return timelines.getMappedResults();
    }

    @Override
    public List<Timeline> trendingTimelines() {
        SortOperation sortByViews = sort(Sort.by(Sort.Direction.DESC, "trendingViews"));
        LimitOperation limitTo = limit(10);
        MatchOperation matchOperation = Aggregation.match(Criteria.where("user").exists(true));
        Aggregation aggregation = Aggregation.newAggregation(sortByViews, limitTo, matchOperation);
        AggregationResults<Timeline> timelines = mongoTemplate.aggregate(aggregation, "timelines", Timeline.class);
        return timelines.getMappedResults();
    }

    @Override
    public List<Timeline> getAllUserTimelines() {
        MatchOperation matchOperation = Aggregation.match(Criteria.where("user").exists(true));
        Aggregation aggregation = Aggregation.newAggregation(matchOperation);
        AggregationResults<Timeline> timelines = mongoTemplate.aggregate(aggregation, "timelines", Timeline.class);
        return timelines.getMappedResults();
    }

    @Override
    public List<String> likeTimeline(String timelineId, String username) {
        return likeOperation(1, timelineId, username);
    }

    @Override
    public List<String> dislikeTimeline(String timelineId, String username) {
        return likeOperation(-1, timelineId, username);
    }

    @Override
    public List<Timeline> searchTimelines(String text) {
        TextCriteria criteria = TextCriteria.forDefaultLanguage()
                .matchingAny(text);
        Query query = TextQuery.queryText(criteria)
                .sortByScore()
                .with(PageRequest.of(0, 5));
        return mongoTemplate.find(query, Timeline.class);
    }

    private List<String> likeOperation(Integer add, String timelineId, String username){
        Optional<Timeline> optionalTimeline = timelineRepository.findById(timelineId);
        if (optionalTimeline.isPresent()){
            Timeline timeline = optionalTimeline.get();
            timeline.setLikes(timeline.getLikes()+add);
            timelineRepository.save(timeline);

            Optional<User> optionalUser = userRepository.findByUsername(username);
            if (optionalUser.isPresent()){
                User user = optionalUser.get();
                List<String> likes = user.getLikes();
                if (add == 1){
                    likes.add(timelineId);

                } else {
                    likes.remove(timelineId);
                }
                user.setLikes(likes);
                userRepository.save(user);
                return likes;
            }
        }
        return null;
    }
}
