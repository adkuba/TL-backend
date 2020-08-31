package com.tl.backend.services;

import com.stripe.exception.CardException;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.PaymentMethod;
import com.stripe.model.PaymentMethodCollection;
import com.stripe.model.Subscription;
import com.stripe.param.PaymentMethodAttachParams;
import com.tl.backend.config.AppProperties;
import com.tl.backend.models.*;
import com.tl.backend.repositories.StatisticsRepository;
import com.tl.backend.repositories.TimelineRepository;
import com.tl.backend.repositories.UserRepository;
import com.tl.backend.request.SubscriptionRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.limit;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.sort;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder encoder;
    private final TimelineRepository timelineRepository;
    private final MongoTemplate mongoTemplate;
    private final JavaMailSender emailSender;
    private final AppProperties appProperties;
    private final DeviceInfoServiceImpl deviceInfoService;
    private final StatisticsServiceImpl statisticsService;
    private final StatisticsRepository statisticsRepository;
    private final NotificationServiceImpl notificationService;

    @Autowired
    public UserServiceImpl(NotificationServiceImpl notificationService, StatisticsRepository statisticsRepository, StatisticsServiceImpl statisticsService, DeviceInfoServiceImpl deviceInfoService, AppProperties appProperties, JavaMailSender emailSender, TimelineRepository timelineRepository, MongoTemplate mongoTemplate, PasswordEncoder passwordEncoder, UserRepository userRepository, AuthenticationManager authenticationManager){
        this.userRepository = userRepository;
        this.notificationService = notificationService;
        this.statisticsRepository = statisticsRepository;
        this.statisticsService = statisticsService;
        this.deviceInfoService = deviceInfoService;
        this.appProperties = appProperties;
        this.emailSender = emailSender;
        this.timelineRepository = timelineRepository;
        this.mongoTemplate = mongoTemplate;
        this.authenticationManager = authenticationManager;
        this.encoder = passwordEncoder;
    }

    @Override
    public List<InteractionEvent> followUser(String username, String followerUsername) {
        Optional<User> optionalUser = userRepository.findByUsername(username);
        Optional<User> optionalFollowerUser = userRepository.findByUsername(followerUsername);
        if (optionalUser.isPresent() && optionalFollowerUser.isPresent()){
            User user = optionalUser.get();
            User follower = optionalFollowerUser.get();
            for (InteractionEvent interactionEvent : user.getFollowers()) {
                if (interactionEvent.getUserId() != null){
                    if (interactionEvent.getUserId().equals(followerUsername)){
                        //already following need to unfollow
                        //deleting from user
                        List<InteractionEvent> followers = user.getFollowers();
                        followers.remove(interactionEvent);
                        user.setFollowers(followers);
                        userRepository.save(user);
                        //deleting from follower
                        followers = follower.getFollowers();
                        InteractionEvent event = new InteractionEvent();
                        event.setFollow(username);
                        event.setDate(interactionEvent.getDate());
                        followers.remove(event);
                        follower.setFollowers(followers);
                        userRepository.save(follower);
                        return followers;
                    }
                }
            }
            //need to follow
            //user
            List<InteractionEvent> followers = user.getFollowers();
            InteractionEvent event = new InteractionEvent();
            event.setUserId(followerUsername);
            followers.add(event);
            user.setFollowers(followers);
            userRepository.save(user);
            notificationService.addNotification(user.getUsername(), follower.getUsername(), "Is now following you.");
            //follower
            followers = follower.getFollowers();
            event = new InteractionEvent();
            event.setFollow(username);
            followers.add(event);
            follower.setFollowers(followers);
            userRepository.save(follower);
            return followers;
        }
        return null;
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public List<User> getUsersByTimelineViews(String timelineID) {
        MatchOperation matchOperation = Aggregation.match(Criteria.where("myViews").elemMatch(Criteria.where("timelineId").is(timelineID)));
        Aggregation aggregation = Aggregation.newAggregation(matchOperation);
        AggregationResults<User> users = mongoTemplate.aggregate(aggregation, "users", User.class);
        return users.getMappedResults();
    }

    @Override
    public ResponseEntity<?> deleteByUsername(String username) {
        Optional<User> optionalUser = userRepository.findByUsername(username);
        if (optionalUser.isPresent()){
            User user = optionalUser.get();
            try {
                Customer customer = Customer.retrieve(user.getStripeID());
                customer.delete();

            } catch (StripeException e) {
                return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
            }
            //delete following
            List<InteractionEvent> followers = user.getFollowers();
            for (InteractionEvent event : followers){
                //im following somebody
                if (event.getFollow() != null){
                    Optional<User> optionalUserFollow = userRepository.findByUsername(event.getFollow());
                    if (optionalUserFollow.isPresent()){
                        User followUser = optionalUserFollow.get();
                        List<InteractionEvent> userFollowing = followUser.getFollowers();
                        List<InteractionEvent> userFollowingNotNull = userFollowing.stream().filter(obj -> obj.getUserId() != null).collect(Collectors.toList());
                        for (InteractionEvent userFollowingObj : userFollowingNotNull){
                            if (userFollowingObj.getUserId().equals(user.getUsername())){
                                userFollowing.remove(userFollowingObj);
                            }
                        }
                        followUser.setFollowers(userFollowing);
                        userRepository.save(followUser);
                    }
                } else {
                    //somebody is following me
                    Optional<User> optionalUserFollow = userRepository.findByUsername(event.getUserId());
                    if (optionalUserFollow.isPresent()){
                        User followUser = optionalUserFollow.get();
                        List<InteractionEvent> userFollowing = followUser.getFollowers();
                        List<InteractionEvent> userFollowingNotNull = userFollowing.stream().filter(obj -> obj.getFollow() != null).collect(Collectors.toList());
                        for (InteractionEvent userFollowingNotNullObj : userFollowingNotNull){
                            if (userFollowingNotNullObj.getFollow().equals(user.getUsername())){
                                userFollowing.remove(userFollowingNotNullObj);
                            }
                        }
                        followUser.setFollowers(userFollowing);
                        userRepository.save(followUser);
                    }
                }
            }
            //delete timeline likes
            List<String> likes = user.getLikes();
            for (String like : likes){
                Optional<Timeline> optionalTimeline = timelineRepository.findById(like);
                if (optionalTimeline.isPresent()){
                    Timeline timeline = optionalTimeline.get();
                    List<InteractionEvent> likeEvents = timeline.getLikes();
                    likeEvents.removeIf(obj -> obj.getUserId().equals(username));
                    timeline.setLikes(likeEvents);
                    timelineRepository.save(timeline);
                }
            }
            //delete deviceInfo
            deviceInfoService.deleteByUsername(user.getUsername());

            userRepository.delete(user);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Override
    public User checkUser(String username) {
        checkSubscription(username);
        Optional<User> optionalUser = userRepository.findByUsername(username);
        return optionalUser.orElse(null);
    }

    @Override
    public ResponseEntity<?> changeEmail(String username, String email) {
        Optional<User> userOptional = userRepository.findByUsername(username);
        if(userOptional.isPresent()){
            User user = userOptional.get();
            user.setEmail(email);
            try {
                Customer customer = Customer.retrieve(user.getStripeID());
                Map<String, Object> params = new HashMap<>();
                params.put("email", email);
                customer.update(params);
                userRepository.save(user);
                return new ResponseEntity<>(HttpStatus.OK);

            } catch (StripeException e) {
                return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
            }
        }
        return new ResponseEntity<>("Can't find user", HttpStatus.BAD_REQUEST);
    }

    @Override
    public boolean changeFullName(String username, String fullName) {
        Optional<User> userOptional = userRepository.findByUsername(username);
        if(userOptional.isPresent()){
            User user = userOptional.get();
            user.setFullName(fullName);
            userRepository.save(user);
            return true;
        }
        return false;
    }

    @Override
    public boolean changePassword(String username, String oldPassword, String newPassword) {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, oldPassword));
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Optional<User> optionalUser = userRepository.findUserByEmail(userDetails.getEmail());

        if(optionalUser.isPresent()){
            User user = optionalUser.get();
            user.setPassword(encoder.encode(newPassword));
            userRepository.save(user);
            try {
                MimeMessage message = emailSender.createMimeMessage();
                message.setFrom(new InternetAddress("admin@tline.site", "Tline"));
                message.addRecipient(Message.RecipientType.TO, new InternetAddress(user.getEmail()));
                message.setSubject("Password changed");
                message.setContent(appProperties.getMailBeginning() + "New password " + appProperties.getMailMid() + "Your password has been changed." + "\n\n You didn't changed your password? Reset it! " + appProperties.getMailEnd(), "text/html");
                emailSender.send(message);
            } catch (MessagingException | UnsupportedEncodingException e) {
                //e.printStackTrace();
            }
            return true;
        }
        return false;
    }

    @Override
    public ResponseEntity<?> createSubscription(SubscriptionRequest subscriptionRequest) throws StripeException {
        Optional<User> optionalUser = userRepository.findByUsername(subscriptionRequest.getUsername());
        if (optionalUser.isPresent()){
            User user = optionalUser.get();
            Customer customer = Customer.retrieve(user.getStripeID());
            try {
                PaymentMethod pm = PaymentMethod.retrieve(subscriptionRequest.getPaymentMethodId());
                user.setCard(pm.getCard().getBrand() + " ****" + pm.getCard().getLast4());
                pm.attach(PaymentMethodAttachParams.builder().setCustomer(customer.getId()).build());

            } catch (CardException e){
                return new ResponseEntity<>(e.getLocalizedMessage(), HttpStatus.BAD_REQUEST);
            }

            Map<String, Object> userParams = new HashMap<>();
            userParams.put("name", subscriptionRequest.getFullName());
            customer.update(userParams);

            Map<String, Object> customerParams = new HashMap<String, Object>();
            Map<String, String> invoiceSettings = new HashMap<String, String>();
            invoiceSettings.put("default_payment_method", subscriptionRequest.getPaymentMethodId());
            customerParams.put("invoice_settings", invoiceSettings);
            customer.update(customerParams);

            Map<String, Object> item = new HashMap<>();
            Map<String, Object> items = new HashMap<>();
            items.put("0", item);
            Map<String, Object> params = new HashMap<>();
            params.put("customer", user.getStripeID());
            params.put("items", items);
            item.put("price", "price_1H9QHbG6mQST9KMb9bgzDj1Y");
            //check if there is active subscription
            if (user.getSubscriptionEnd() != null){
                if (user.getSubscriptionEnd().compareTo(LocalDate.now()) >= 0){
                    LocalDate nextPayment = user.getSubscriptionEnd();
                    Timestamp timestamp = Timestamp.valueOf(nextPayment.atStartOfDay());
                    params.put("trial_end", timestamp);
                }
            }

            List<String> expandList = new ArrayList<>();
            expandList.add("latest_invoice.payment_intent");
            params.put("expand", expandList);

            Subscription subscription = Subscription.create(params);
            Instant instant = Instant.ofEpochSecond(subscription.getCurrentPeriodEnd());
            user.setSubscriptionEnd(LocalDate.ofInstant(instant, ZoneOffset.UTC));
            user.setSubscriptionID(subscription.getId());

            activateTimelines(user.getUsername());
            userRepository.save(user);

            try {
                MimeMessage message = emailSender.createMimeMessage();
                message.setFrom(new InternetAddress("admin@tline.site", "Tline"));
                message.addRecipient(Message.RecipientType.TO, new InternetAddress(user.getEmail()));
                message.setSubject("Subscription");
                message.setContent(appProperties.getMailBeginning() + "Premium " + appProperties.getMailMid() + "You are now a premium user thank you!\n\n" + "Name: " + subscriptionRequest.getFullName() + "\nPrice: 3$\nCard: " + user.getCard() + "\n\n You don't recognize this action? Reset password! " + appProperties.getMailEnd(), "text/html");
                emailSender.send(message);
            } catch (MessagingException | UnsupportedEncodingException e) {
                //e.printStackTrace();
            }

            return new ResponseEntity<>(subscription.toJson(), HttpStatus.OK);
        }
        return new ResponseEntity<>("Can't find user", HttpStatus.BAD_REQUEST);
    }

    @Override
    public void checkSubscription(String username) {
        Optional<User> optionalUser = userRepository.findByUsername(username);
        if (optionalUser.isPresent()){
            User user = optionalUser.get();
            //user has subscription
            if (user.getSubscriptionEnd() != null){
                //if subscription ended
                if (user.getSubscriptionEnd().compareTo(LocalDate.now()) < 0){
                    //check again if subscription active
                    if (user.getSubscriptionID() != null) {
                        try {
                            Subscription subscription = Subscription.retrieve(user.getSubscriptionID());
                            //subscription active
                            if (subscription.getStatus().equals("active") || subscription.getStatus().equals("trialing")) {
                                Instant instant = Instant.ofEpochSecond(subscription.getCurrentPeriodEnd());
                                user.setSubscriptionEnd(LocalDate.ofInstant(instant, ZoneOffset.UTC));
                                activateTimelines(username);
                            } else {
                                //subscription not active
                                user.setSubscriptionID(null);
                                user.setSubscriptionEnd(null);
                                disableTimelines(username);
                            }
                            userRepository.save(user);
                        } catch (StripeException e) {
                            System.out.println(e.getMessage());
                        }
                    } else {
                        //subscription not active
                        user.setSubscriptionID(null);
                        user.setSubscriptionEnd(null);
                        disableTimelines(username);
                        userRepository.save(user);
                    }
                } else {
                    activateTimelines(username);
                }
            }
        }
    }

    @Override
    public boolean cancelSubscription(String username) throws StripeException {
        Optional<User> optionalUser = userRepository.findByUsername(username);
        if (optionalUser.isPresent()){
            User user = optionalUser.get();
            Subscription subscription = Subscription.retrieve(user.getSubscriptionID());
            //delete cards
            Map<String, Object> params = new HashMap<>();
            params.put("customer", user.getStripeID());
            params.put("type", "card");
            PaymentMethodCollection paymentMethods = PaymentMethod.list(params);
            for (PaymentMethod paymentMethod : paymentMethods.getData()){
                paymentMethod.detach();
            }

            Subscription deletedSubscription = subscription.cancel();
            user.setSubscriptionID(null);
            userRepository.save(user);
            try {
                MimeMessage message = emailSender.createMimeMessage();
                message.setFrom(new InternetAddress("admin@tline.site", "Tline"));
                message.addRecipient(Message.RecipientType.TO, new InternetAddress(user.getEmail()));
                message.setSubject("Subscription");
                message.setContent(appProperties.getMailBeginning() + "Premium canceled " + appProperties.getMailMid() + "Your subscription has been canceled, premium will end at " + user.getSubscriptionEnd().toString() + "\n\n You don't recognize this action? Reset password! " + appProperties.getMailEnd(), "text/html");
                emailSender.send(message);
            } catch (MessagingException | UnsupportedEncodingException e) {
                //e.printStackTrace();
            }
            return true;
        }
        return false;
    }

    @Override
    public List<User> getNewUsers() {
        SortOperation sortByDate = sort(Sort.by(Sort.Direction.ASC, "creationTime"));
        Aggregation aggregation = Aggregation.newAggregation(sortByDate);
        AggregationResults<User> users = mongoTemplate.aggregate(aggregation, "users", User.class);
        return users.getMappedResults();
    }

    @Override
    public List<User> getRandomUsers() {
        SampleOperation matchStage = Aggregation.sample(5);
        Aggregation aggregation = Aggregation.newAggregation(matchStage);
        AggregationResults<User> users = mongoTemplate.aggregate(aggregation, "users", User.class);
        return users.getMappedResults();
    }

    @Override
    public void blockUser(String username, String reason) {
        Optional<User> optionalUser = userRepository.findByUsername(username);
        if (optionalUser.isPresent()){
            User user = optionalUser.get();
            user.setBlocked(true);
            try {
                MimeMessage message = emailSender.createMimeMessage();
                message.setFrom(new InternetAddress("admin@tline.site", "Tline"));
                message.addRecipient(Message.RecipientType.TO, new InternetAddress(user.getEmail()));
                message.setSubject("Blocked");
                message.setContent(appProperties.getMailBeginning() + "Info " + appProperties.getMailMid() + "Your account has been blocked and your timelines have been deleted. \n\n Message: \n" + reason + "\n\n You can reply to this email. " + appProperties.getMailEnd(), "text/html");
                emailSender.send(message);
            } catch (MessagingException | UnsupportedEncodingException e) {
                //e.printStackTrace();
            }
            userRepository.save(user);
        }
    }

    @Override
    public void unBlockUser(String username) {
        Optional<User> optionalUser = userRepository.findByUsername(username);
        if (optionalUser.isPresent()){
            User user = optionalUser.get();
            user.setBlocked(false);
            try {
                MimeMessage message = emailSender.createMimeMessage();
                message.setFrom(new InternetAddress("admin@tline.site", "Tline"));
                message.addRecipient(Message.RecipientType.TO, new InternetAddress(user.getEmail()));
                message.setSubject("Unblocked");
                message.setContent(appProperties.getMailBeginning() + "Info " + appProperties.getMailMid() + "Your account has been unblocked" + appProperties.getMailEnd(), "text/html");
                emailSender.send(message);
            } catch (MessagingException | UnsupportedEncodingException e) {
                //e.printStackTrace();
            }
            userRepository.save(user);
        }
    }

    @Override
    public void disableTimelines(String username) {
        Optional<User> optionalUser = userRepository.findByUsername(username);
        if (optionalUser.isPresent()){
            List<Timeline> timelines = timelineRepository.findAllByUserId(optionalUser.get().getId());
            timelines.sort(Comparator.comparing(Timeline::getCreationDate));
            //deactivate last two timelines
            for (int i = timelines.size()-1; i > 1; i--){
                Timeline timeline = timelines.get(i);
                timeline.setActive(false);
                timeline.setPremium(false);
                timelineRepository.save(timeline);
            }

            for (int i=0; i<=1; i++){
                if (i >= timelines.size()){
                    break;
                }
                Timeline timeline = timelines.get(i);
                timeline.setActive(true);
                timeline.setPremium(false);
                timelineRepository.save(timeline);
            }
        }
    }

    @Override
    public void profileView(String username, HttpServletRequest request) {
        Optional<User> optionalUser = userRepository.findByUsername(username);
        if (optionalUser.isPresent()){
            User user = optionalUser.get();
            statisticsService.checkStatistics();
            Optional<Statistics> optionalStatistics = statisticsRepository.findByDay(LocalDate.now());
            if (optionalStatistics.isPresent()){
                Statistics statistics = optionalStatistics.get();
                statistics.setProfileViews(statistics.getProfileViews() + 1);
                statisticsRepository.save(statistics);
            }
            Map<LocalDate, Map<String, Long>> profileViews = user.getProfileViews();
            DeviceInfo deviceInfo = deviceInfoService.createInfo(request, null);

            if (profileViews.containsKey(LocalDate.now())){
                //has day
                Map<String, Long> devicesInDay = profileViews.get(LocalDate.now());
                if (devicesInDay.containsKey(deviceInfo.getId())){
                    //has device
                    devicesInDay.put(deviceInfo.getId(), devicesInDay.get(deviceInfo.getId()) + 1);
                } else {
                    //no device
                    devicesInDay.put(deviceInfo.getId(), 1L);
                }
                profileViews.put(LocalDate.now(), devicesInDay);
            } else {
                //first in day
                Map<String, Long> devicesInDay = new HashMap<>();
                devicesInDay.put(deviceInfo.getId(), 1L);
                profileViews.put(LocalDate.now(), devicesInDay);
            }
            user.setProfileViews(profileViews);
            user.profileViews();
            userRepository.save(user);
        }
    }

    void activateTimelines(String username){
        Optional<User> optionalUser = userRepository.findByUsername(username);
        if (optionalUser.isPresent()){
            List<Timeline> timelines = timelineRepository.findAllByUserId(optionalUser.get().getId());
            for (Timeline timeline : timelines) {
                timeline.setActive(true);
                timeline.setPremium(true);
                timelineRepository.save(timeline);
            }
        }
    }
}
