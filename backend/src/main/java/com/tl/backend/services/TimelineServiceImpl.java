package com.tl.backend.services;

import com.stripe.exception.StripeException;
import com.stripe.model.Subscription;
import com.tl.backend.config.AppProperties;
import com.tl.backend.fileHandling.FileResource;
import com.tl.backend.fileHandling.FileResourceRepository;
import com.tl.backend.fileHandling.FileServiceImpl;
import com.tl.backend.mappers.TimelineMapper;
import com.tl.backend.models.Event;
import com.tl.backend.models.InteractionEvent;
import com.tl.backend.models.Timeline;
import com.tl.backend.models.User;
import com.tl.backend.repositories.EventRepository;
import com.tl.backend.repositories.TimelineRepository;
import com.tl.backend.repositories.UserRepository;
import com.tl.backend.request.HomepageRequest;
import com.tl.backend.response.TimelineResponse;
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
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.time.LocalDate;
import java.time.Period;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    private final JavaMailSender emailSender;
    private final AppProperties appProperties;
    private final NotificationServiceImpl notificationService;
    private final TimelineMapper timelineMapper;

    @Autowired
    public TimelineServiceImpl(TimelineMapper timelineMapper, NotificationServiceImpl notificationService, AppProperties appProperties, JavaMailSender emailSender, UserServiceImpl userService, UserRepository userRepository, FileResourceRepository fileResourceRepository, TimelineRepository timelineRepository, FileServiceImpl fileService, MongoTemplate mongoTemplate, EventRepository eventRepository){
        this.timelineRepository = timelineRepository;
        this.timelineMapper = timelineMapper;
        this.notificationService = notificationService;
        this.appProperties = appProperties;
        this.emailSender = emailSender;
        this.userService = userService;
        this.userRepository = userRepository;
        this.fileResourceRepository = fileResourceRepository;
        this.fileService = fileService;
        this.mongoTemplate = mongoTemplate;
        this.eventRepository = eventRepository;
    }

    @Override
    public Timeline getTimelineById(String id, String username) {
        Optional<Timeline> optionalTimeline = timelineRepository.findById(id);
        optionalTimeline.ifPresent(timeline -> userService.checkSubscription(timeline.getUser().getUsername()));
        optionalTimeline = timelineRepository.findById(id);
        if (optionalTimeline.isPresent()){
            Timeline timeline = optionalTimeline.get();
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
    public void deleteMineTimelineById(String id, Boolean delPictures, String reason) {
        Optional<Timeline> optionalTimeline = timelineRepository.findById(id);
        if (optionalTimeline.isPresent()){
            Timeline timeline = optionalTimeline.get();
            if (delPictures){
                deletePictures(timeline.getPictures());
                //edit timeline doesn't delete pictures
                //so if delPictures=true timeline is permanently deleted
                //need to delete likes
                List<InteractionEvent> likes = timeline.getLikes();
                for (InteractionEvent like : likes){
                    Optional<User> optionalUser = userRepository.findByUsername(like.getUserId());
                    if (optionalUser.isPresent()){
                        User user = optionalUser.get();
                        List<String> userLikes = user.getLikes();
                        userLikes.remove(timeline.getId());
                        user.setLikes(userLikes);
                        userRepository.save(user);
                    }
                }
                //need to delete user views
                List<User> users = userService.getUsersByTimelineViews(timeline.getId());
                for (User user : users){
                    List<InteractionEvent> views = user.getMyViews();
                    views = views.stream().filter(o -> !o.getTimelineId().equals(timeline.getId())).collect(Collectors.toList());
                    user.setMyViews(views);
                    userRepository.save(user);
                }
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
            if (optionalUser.isPresent()){
                User user = optionalUser.get();
                userService.disableTimelines(user.getUsername());

                if (reason != null){
                    try {
                        MimeMessage message = emailSender.createMimeMessage();
                        message.setFrom(new InternetAddress("quicpos@gmail.com", "Tline"));
                        message.addRecipient(Message.RecipientType.TO, new InternetAddress(user.getEmail()));
                        message.setSubject("Timeline");
                        message.setContent(appProperties.getMailBeginning() + "Deleted " + appProperties.getMailMid() + "Your timeline has been deleted. \n\n Message: \n" + reason + "\n\n You can reply to this email. " + appProperties.getMailEnd(), "text/html");
                        emailSender.send(message);
                    } catch (MessagingException | UnsupportedEncodingException e) {
                        //e.printStackTrace();
                    }
                }
            }
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
    public List<Timeline> randomTimelines(List<String> seenIDS) {
        SampleOperation matchStage = Aggregation.sample(5);
        MatchOperation matchOperation = Aggregation.match(Criteria.where("user").exists(true));
        MatchOperation active = Aggregation.match(Criteria.where("active").is(true));
        MatchOperation seen = Aggregation.match(Criteria.where("_id").nin(seenIDS));
        Aggregation aggregation = Aggregation.newAggregation(matchStage, matchOperation, active, seen);
        AggregationResults<Timeline> timelines = mongoTemplate.aggregate(aggregation, "timelines", Timeline.class);
        return timelines.getMappedResults();
    }

    @Override
    public List<Timeline> newTimelines(List<String> seenIDS) {
        SortOperation sortByDate = sort(Sort.by(Sort.Direction.ASC, "creationDate"));
        LimitOperation limitTo = limit(10);
        MatchOperation matchOperation = Aggregation.match(Criteria.where("user").exists(true));
        MatchOperation active = Aggregation.match(Criteria.where("active").is(true));
        MatchOperation seen = Aggregation.match(Criteria.where("_id").nin(seenIDS));
        Aggregation aggregation = Aggregation.newAggregation(sortByDate, limitTo, matchOperation, active, seen);
        AggregationResults<Timeline> timelines = mongoTemplate.aggregate(aggregation, "timelines", Timeline.class);
        return timelines.getMappedResults();
    }

    @Override
    public List<Timeline> popularTimelines(List<String> seenIDS) {
        SortOperation sortByViews = sort(Sort.by(Sort.Direction.DESC, "views"));
        LimitOperation limitTo = limit(10);
        MatchOperation matchOperation = Aggregation.match(Criteria.where("user").exists(true));
        MatchOperation active = Aggregation.match(Criteria.where("active").is(true));
        MatchOperation seen = Aggregation.match(Criteria.where("_id").nin(seenIDS));
        Aggregation aggregation = Aggregation.newAggregation(sortByViews, limitTo, matchOperation, active, seen);
        AggregationResults<Timeline> timelines = mongoTemplate.aggregate(aggregation, "timelines", Timeline.class);
        return timelines.getMappedResults();
    }

    @Override
    public List<Timeline> trendingTimelines(List<String> seenIDS) {
        SortOperation sortByViews = sort(Sort.by(Sort.Direction.DESC, "trendingViews"));
        LimitOperation limitTo = limit(10);
        MatchOperation matchOperation = Aggregation.match(Criteria.where("user").exists(true));
        MatchOperation active = Aggregation.match(Criteria.where("active").is(true));
        MatchOperation seen = Aggregation.match(Criteria.where("_id").nin(seenIDS));
        Aggregation aggregation = Aggregation.newAggregation(sortByViews, limitTo, matchOperation, active, seen);
        AggregationResults<Timeline> timelines = mongoTemplate.aggregate(aggregation, "timelines", Timeline.class);
        return timelines.getMappedResults();
    }

    @Override
    public List<TimelineResponse> getHomepageTimelines(HomepageRequest homepageRequest) {
        List<String> seenIDS = null;
        if (homepageRequest.getUsername() == null){
            //timelines for non logged-in user
            seenIDS = homepageRequest.getTimelinesIDS();
        } else {
            //timelines for logged-in users
            Optional<User> optionalUser = userRepository.findByUsername(homepageRequest.getUsername());
            if (optionalUser.isPresent()){
                List<String> userSeen = optionalUser.get().getMyViews().stream().map(InteractionEvent::getTimelineId).collect(Collectors.toList());
                seenIDS = Stream.concat(homepageRequest.getTimelinesIDS().stream(), userSeen.stream()).collect(Collectors.toList());
            }
        }
        assert seenIDS != null;
        List<TimelineResponse> timelineResponses = new ArrayList<>();
        Random rand = new Random();
        List<TimelineResponse> randomTimelines = timelineMapper.timelinesResponse(randomTimelines(seenIDS));

        List<String> added = new ArrayList<>();
        for (TimelineResponse timelineResponse : randomTimelines){
            timelineResponse.setCategory("SUGGESTED");
            timelineResponses.add(timelineResponse);
            added.add(timelineResponse.getId());
        }

        seenIDS = Stream.concat(seenIDS.stream(), added.stream()).collect(Collectors.toList());
        added.clear();
        List<TimelineResponse> newTimelines = timelineMapper.timelinesResponse(newTimelines(seenIDS));

        for (int i=0; i<2; i++){
            if (newTimelines.size() > 0){
                int randomIndex = rand.nextInt(newTimelines.size());
                TimelineResponse timelineResponse = newTimelines.get(randomIndex);
                timelineResponse.setCategory("NEW");
                timelineResponses.add(timelineResponse);
                newTimelines.remove(randomIndex);
                added.add(timelineResponse.getId());
            }
        }

        seenIDS = Stream.concat(seenIDS.stream(), added.stream()).collect(Collectors.toList());
        added.clear();
        List<TimelineResponse> popularTimelines = timelineMapper.timelinesResponse(popularTimelines(seenIDS));

        for (int i=0; i<2; i++){
            if (popularTimelines.size() > 0){
                int randomIndex = rand.nextInt(popularTimelines.size());
                TimelineResponse timelineResponse = popularTimelines.get(randomIndex);
                timelineResponse.setCategory("POPULAR");
                timelineResponses.add(timelineResponse);
                popularTimelines.remove(randomIndex);
                added.add(timelineResponse.getId());
            }
        }

        seenIDS = Stream.concat(seenIDS.stream(), added.stream()).collect(Collectors.toList());
        added.clear();
        List<TimelineResponse> trendingTimelines = timelineMapper.timelinesResponse(trendingTimelines(seenIDS));

        for (int i=0; i<2; i++){
            if (trendingTimelines.size() > 0){
                int randomIndex = rand.nextInt(trendingTimelines.size());
                TimelineResponse timelineResponse = trendingTimelines.get(randomIndex);
                timelineResponse.setCategory("TRENDING");
                timelineResponses.add(timelineResponse);
                trendingTimelines.remove(randomIndex);
                added.add(timelineResponse.getId());
            }
        }

        seenIDS = Stream.concat(seenIDS.stream(), added.stream()).collect(Collectors.toList());
        added.clear();
        List<TimelineResponse> premiumTimelines = timelineMapper.timelinesResponse(premiumTimelines(seenIDS));

        for (int i=0; i<2; i++){
            if (premiumTimelines.size() > 0){
                int randomIndex = rand.nextInt(premiumTimelines.size());
                TimelineResponse timelineResponse = premiumTimelines.get(randomIndex);
                timelineResponse.setCategory("PREMIUM");
                timelineResponses.add(timelineResponse);
                premiumTimelines.remove(randomIndex);
            }
        }

        if (timelineResponses.size() == 0){
            int randomIndex = rand.nextInt(seenIDS.size());
            Optional<Timeline> optionalTimeline = timelineRepository.findById(seenIDS.get(randomIndex));
            if (optionalTimeline.isPresent()){
                TimelineResponse timelineResponse = timelineMapper.timelineResponse(optionalTimeline.get());
                timelineResponse.setCategory("READ AGAIN");
                timelineResponses.add(timelineResponse);
            }
        }

        Collections.shuffle(timelineResponses);
        return timelineResponses;
    }

    @Override
    public List<Timeline> premiumTimelines(List<String> seenIDS) {
        MatchOperation active = Aggregation.match(Criteria.where("active").is(true));
        MatchOperation premium = Aggregation.match(Criteria.where("premium").is(true));
        MatchOperation seen = Aggregation.match(Criteria.where("_id").nin(seenIDS));
        Aggregation aggregation = Aggregation.newAggregation(premium, active, seen);
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
            timeline.setNumberOfReports(timeline.getNumberOfReports()+1);
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
            deleteMineTimelineById(timeline.getId(), true, null);
        }
    }

    @Override
    public void makeActive(List<String> active, String username) {
        List<Timeline> userTimelines = getUserTimelines(username);
        for (Timeline timeline : userTimelines){
            timeline.setActive(false);
            timelineRepository.save(timeline);
        }
        for (String timelineId : active){
            Optional<Timeline> optionalTimeline = timelineRepository.findById(timelineId);
            if (optionalTimeline.isPresent()){
                Timeline timeline = optionalTimeline.get();
                timeline.setActive(true);
                timelineRepository.save(timeline);
            }
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
            for (Timeline userTimeline : getUserTimelines(user.getUsername())){
                //check if already is in list
                if (timelines.stream().noneMatch(o -> o.getId().equals(userTimeline.getId()))){
                    timelines.add(userTimeline);
                }
            }
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
                notificationService.addNotification(timeline.getUser().getUsername(), username, "Likes " + timelineId);

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
