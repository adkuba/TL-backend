package com.tl.backend.services;

import com.stripe.exception.StripeException;
import com.stripe.model.Subscription;
import com.tl.backend.fileHandling.FileResource;
import com.tl.backend.fileHandling.FileResourceRepository;
import com.tl.backend.fileHandling.FileServiceImpl;
import com.tl.backend.models.Event;
import com.tl.backend.models.InteractionEvent;
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
import java.util.*;

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
    private final UserServiceImpl userService;

    @Autowired
    public TimelineServiceImpl(UserServiceImpl userService, UserRepository userRepository, FileResourceRepository fileResourceRepository, TimelineRepository timelineRepository, FileServiceImpl fileService, MongoTemplate mongoTemplate, EventRepository eventRepository){
        this.timelineRepository = timelineRepository;
        this.userService = userService;
        this.userRepository = userRepository;
        this.fileResourceRepository = fileResourceRepository;
        this.fileService = fileService;
        this.mongoTemplate = mongoTemplate;
        this.eventRepository = eventRepository;
    }

    @Override
    public Timeline getTimelineById(String id, String username) throws StripeException {
        Optional<Timeline> optionalTimeline = timelineRepository.findById(id);
        optionalTimeline.ifPresent(timeline -> userService.checkSubscription(timeline.getUser().getUsername()));
        optionalTimeline = timelineRepository.findById(id);
        if (optionalTimeline.isPresent()){
            Timeline timeline = optionalTimeline.get();
            if (timeline.getActive()){
                if (username != null){
                    Optional<User> optionalUser = userRepository.findByUsername(username);
                    if (optionalUser.isPresent()){
                        User user = optionalUser.get();
                        List<InteractionEvent> myViews = user.getMyViews();
                        InteractionEvent view = new InteractionEvent();
                        view.setDate(LocalDate.now());
                        view.setTimelineId(id);
                        if (myViews.stream().noneMatch(o -> o.getTimelineId().equals(id))){
                            myViews.add(view);
                            user.setMyViews(myViews);
                            userRepository.save(user);
                        }
                    }
                }
                return timeline;
            }
        }
        return null;
    }

    @Override
    public Timeline getTimelineByEventId(String eventId) {
        Optional<Timeline> optionalTimeline = timelineRepository.findOneByEventId(eventId);
        return optionalTimeline.orElse(null);
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
                Optional<Timeline> optionalSubTimeline = timelineRepository.findOneByEventId(event.getId());
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
            Optional<User> optionalUser = userRepository.findById(timeline.getUser().getId());
            optionalUser.ifPresent(user -> userService.disableTimelines(user.getUsername()));
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
                String type = Objects.requireNonNull(file.getContentType()).split("/")[0];
                if (type.equals("image")){
                    fileResources.add(fileService.saveFileResource(file));
                }
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
        Optional<User> optionalUser = userRepository.findByUsername(username);
        return optionalUser.map(user -> timelineRepository.findAllByUserId(user.getId())).orElse(null);
    }

    @Override
    public List<Timeline> randomTimelines() {
        SampleOperation matchStage = Aggregation.sample(5);
        MatchOperation matchOperation = Aggregation.match(Criteria.where("user").exists(true));
        MatchOperation active = Aggregation.match(Criteria.where("active").is(true));
        Aggregation aggregation = Aggregation.newAggregation(matchStage, matchOperation, active);
        AggregationResults<Timeline> timelines = mongoTemplate.aggregate(aggregation, "timelines", Timeline.class);
        return timelines.getMappedResults();
    }

    @Override
    public List<Timeline> newTimelines() {
        SortOperation sortByDate = sort(Sort.by(Sort.Direction.ASC, "creationDate"));
        LimitOperation limitTo = limit(10);
        MatchOperation matchOperation = Aggregation.match(Criteria.where("user").exists(true));
        MatchOperation active = Aggregation.match(Criteria.where("active").is(true));
        Aggregation aggregation = Aggregation.newAggregation(sortByDate, limitTo, matchOperation, active);
        AggregationResults<Timeline> timelines = mongoTemplate.aggregate(aggregation, "timelines", Timeline.class);
        return timelines.getMappedResults();
    }

    @Override
    public List<Timeline> popularTimelines() {
        SortOperation sortByViews = sort(Sort.by(Sort.Direction.DESC, "views"));
        LimitOperation limitTo = limit(10);
        MatchOperation matchOperation = Aggregation.match(Criteria.where("user").exists(true));
        MatchOperation active = Aggregation.match(Criteria.where("active").is(true));
        Aggregation aggregation = Aggregation.newAggregation(sortByViews, limitTo, matchOperation, active);
        AggregationResults<Timeline> timelines = mongoTemplate.aggregate(aggregation, "timelines", Timeline.class);
        return timelines.getMappedResults();
    }

    @Override
    public List<Timeline> trendingTimelines() {
        SortOperation sortByViews = sort(Sort.by(Sort.Direction.DESC, "trendingViews"));
        LimitOperation limitTo = limit(10);
        MatchOperation matchOperation = Aggregation.match(Criteria.where("user").exists(true));
        MatchOperation active = Aggregation.match(Criteria.where("active").is(true));
        Aggregation aggregation = Aggregation.newAggregation(sortByViews, limitTo, matchOperation, active);
        AggregationResults<Timeline> timelines = mongoTemplate.aggregate(aggregation, "timelines", Timeline.class);
        return timelines.getMappedResults();
    }

    @Override
    public List<Timeline> premiumTimelines() {
        MatchOperation active = Aggregation.match(Criteria.where("active").is(true));
        MatchOperation premium = Aggregation.match(Criteria.where("premium").is(true));
        Aggregation aggregation = Aggregation.newAggregation(premium, active);
        AggregationResults<Timeline> timelines = mongoTemplate.aggregate(aggregation, "timelines", Timeline.class);
        return timelines.getMappedResults();
    }

    @Override
    public void addPremiumView(String timelineId) {
        Optional<Timeline> optionalTimeline = timelineRepository.findById(timelineId);
        if (optionalTimeline.isPresent()){
            Timeline timeline = optionalTimeline.get();
            timeline.setPremiumViews(timeline.getPremiumViews() + 1);
            timelineRepository.save(timeline);
        }
    }

    @Override
    public void reportTimeline(String timelineId) {
        Optional<Timeline> optionalTimeline = timelineRepository.findById(timelineId);
        if (optionalTimeline.isPresent()){
            Timeline timeline = optionalTimeline.get();
            timeline.setReported(true);
            timelineRepository.save(timeline);
        }
    }

    @Override
    public void unReportTimeline(String timelineId) {
        Optional<Timeline> optionalTimeline = timelineRepository.findById(timelineId);
        if (optionalTimeline.isPresent()){
            Timeline timeline = optionalTimeline.get();
            timeline.setReported(false);
            timelineRepository.save(timeline);
        }
    }

    @Override
    public void deleteUserTimelines(String username) {
        List<Timeline> timelines = getUserTimelines(username);
        for (Timeline timeline : timelines){
            deleteMineTimelineById(timeline.getId(), true);
        }
    }

    @Override
    public List<Timeline> getReported() {
        MatchOperation reported = Aggregation.match(Criteria.where("reported").is(true));
        Aggregation aggregation = Aggregation.newAggregation(reported);
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
        TextCriteria criteria = TextCriteria.forDefaultLanguage().matchingAny(text);
        Query query = TextQuery.queryText(criteria).sortByScore().with(PageRequest.of(0, 5));

        //searched text is in main timeline
        List<Timeline> timelines = mongoTemplate.find(query, Timeline.class);
        List<Event> events = mongoTemplate.find(query, Event.class);
        //in events
        for (Event event : events){
            //master timeline not in list
            if (timelines.stream().noneMatch(o -> o.getId().equals(event.getTimelineId()))){
                Optional<Timeline> optionalTimeline = timelineRepository.findById(event.getTimelineId());
                if (optionalTimeline.isPresent()){
                    Timeline timeline = optionalTimeline.get();
                    if (timeline.getUser() != null){
                        timelines.add(timeline);

                    } else {
                        //master timeline is sub-timeline
                        Optional<Event> optionalEvent = eventRepository.findById(timeline.getEventId());
                        if (optionalEvent.isPresent()){
                            Event masterEvent = optionalEvent.get();
                            //check again if master timeline is in list
                            if (timelines.stream().noneMatch(o -> o.getId().equals(masterEvent.getTimelineId()))){
                                Optional<Timeline> masterOptionalTimeline = timelineRepository.findById(masterEvent.getTimelineId());
                                masterOptionalTimeline.ifPresent(timelines::add);
                            }
                        }
                    }
                }
            }
        }
        //searched text is in username
        List<User> users = mongoTemplate.find(query, User.class);
        for (User user : users){
            timelines.addAll(getUserTimelines(user.getUsername()));
        }

        return timelines;
    }

    private List<String> likeOperation(Integer add, String timelineId, String username){
        Optional<Timeline> optionalTimeline = timelineRepository.findById(timelineId);
        Optional<User> optionalUser = userRepository.findByUsername(username);
        if (optionalTimeline.isPresent() && optionalUser.isPresent()){
            User user = optionalUser.get();
            Timeline timeline = optionalTimeline.get();

            if (add == 1){
                List<InteractionEvent> likes = timeline.getLikes();
                InteractionEvent like = new InteractionEvent();
                like.setUserId(username);
                likes.add(like);
                timeline.setLikes(likes);
                timelineRepository.save(timeline);

                List<String> likesUser = user.getLikes();
                likesUser.add(timelineId);
                user.setLikes(likesUser);
                userRepository.save(user);
                return  likesUser;

            } else {
                List<InteractionEvent> likes = timeline.getLikes();
                likes.removeIf(like -> like.getUserId().equals(username));
                timeline.setLikes(likes);
                timelineRepository.save(timeline);

                List<String> likesUser = user.getLikes();
                likesUser.removeIf(like -> like.equals(timelineId));
                user.setLikes(likesUser);
                userRepository.save(user);
                return  likesUser;
            }
        }
        return null;
    }
}
