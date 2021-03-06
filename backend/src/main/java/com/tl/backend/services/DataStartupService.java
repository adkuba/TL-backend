package com.tl.backend.services;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.tl.backend.config.AppProperties;
import com.tl.backend.models.*;
import com.tl.backend.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.TextIndexDefinition;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.*;

@Service
public class DataStartupService {

    private final UserRepository userRepository;
    private final TimelineRepository timelineRepository;
    private final EventRepository eventRepository;
    private final RoleRepository roleRepository;
    private final StatisticsRepository statisticsRepository;
    private final MongoTemplate mongoTemplate;
    private final AppProperties appProperties;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    public DataStartupService(AppProperties appProperties, MongoTemplate mongoTemplate, StatisticsRepository statisticsRepository, UserRepository userRepository, TimelineRepository timelineRepository, EventRepository eventRepository, RoleRepository roleRepository){
        this.userRepository = userRepository;
        this.appProperties = appProperties;
        this.mongoTemplate = mongoTemplate;
        this.timelineRepository = timelineRepository;
        this.eventRepository = eventRepository;
        this.roleRepository = roleRepository;
        this.statisticsRepository = statisticsRepository;
    }

    private void createMe() throws StripeException {
        String lorem = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Phasellus gravida dui feugiat risus vehicula, vel placerat velit auctor. Sed lobortis tellus ut ullamcorper sagittis. Duis eget aliquam metus, ac sodales nunc. Integer imperdiet feugiat feugiat. Sed vel auctor nisi. Suspendisse potenti. Donec tellus velit, fringilla ac orci vitae, rhoncus placerat lacus. Integer in dictum nulla. Class aptent taciti sociosqu ad litora torquent per conubia nostra, per inceptos himenaeos. Vestibulum a risus nec eros pretium rhoncus. Integer a dignissim urna. Duis tempus odio eu elit consequat, eget placerat tortor sodales. Nulla finibus, nisi sed pellentesque laoreet, purus orci tincidunt sem, sit amet egestas sem dui in lorem.";

        //indexing
        TextIndexDefinition textIndex = new TextIndexDefinition.TextIndexDefinitionBuilder()
                .onField("description")
                .onField("descriptionTitle")
                .build();

        TextIndexDefinition eventTextIndex = new TextIndexDefinition.TextIndexDefinitionBuilder()
                .onField("description")
                .onField("title")
                .build();

        TextIndexDefinition userTextIndex = new TextIndexDefinition.TextIndexDefinitionBuilder()
                .onField("username")
                .onField("fullName")
                .build();

        mongoTemplate.indexOps(User.class).ensureIndex(userTextIndex);
        mongoTemplate.indexOps(Event.class).ensureIndex(eventTextIndex);
        mongoTemplate.indexOps(Timeline.class).ensureIndex(textIndex);

        //ROLE
        Role admin = new Role();
        admin.setName(ERole.ROLE_ADMIN);
        Role userR = new Role();
        userR.setName(ERole.ROLE_USER);
        if (roleRepository.findByName(ERole.ROLE_ADMIN).isEmpty()){
            roleRepository.save(admin);
        }
        if (roleRepository.findByName(ERole.ROLE_USER).isEmpty()){
            roleRepository.save(userR);
        }

        //stats are created when needed!

        //admin
        User kuba = new User();
        kuba.setFullName("Jakub Adamski");
        kuba.setEmail("akuba@exemplum.pl");
        kuba.setUsername("akuba");
        kuba.setPassword(encoder.encode("funia"));
        Set<Role> roles = new HashSet<>();
        roles.add(admin);
        kuba.setRoles(roles);
        Notification notification = new Notification();
        notification.setRead(true);
        List<NotificationMessage> notificationMessages = new ArrayList<>();
        notification.setMessages(notificationMessages);
        kuba.setNotification(notification);
        if (userRepository.findByUsername("akuba").isEmpty()){
            //STRIPE
            Map<String, Object> customerParams = new HashMap<String, Object>();
            customerParams.put("email", kuba.getEmail());
            Customer customer = Customer.create(customerParams);
            kuba.setStripeID(customer.getId());

            userRepository.save(kuba);
        }

        //timeline
        if (timelineRepository.findById("kubatl").isEmpty()){
            Timeline timeline = new Timeline();
            timeline.setUser(kuba);
            timeline.setDescriptionTitle("Me");
            timeline.setDescription(lorem);
            timeline.setId("kubatl");
            timeline.setPictures(new ArrayList<>());
            timelineRepository.save(timeline);

            Event trackingEvent = new Event();
            trackingEvent.setTitle("Object Tracking");
            trackingEvent.setDescription(lorem);
            trackingEvent.setDate(LocalDate.of(2019, Month.OCTOBER, 20));
            trackingEvent.setTimelineId(timeline.getId());
            eventRepository.save(trackingEvent);

            Timeline subTimelineTrackingEvent = new Timeline();
            subTimelineTrackingEvent.setId("kubatl-sub1");
            subTimelineTrackingEvent.setEventId(trackingEvent.getId());
            subTimelineTrackingEvent.setEventId(trackingEvent.getId());
            timelineRepository.save(subTimelineTrackingEvent);

            Event subTrackingEvent = new Event();
            subTrackingEvent.setTitle("Aplikacja");
            subTrackingEvent.setDescription(lorem.substring(0, lorem.length()/2));
            subTrackingEvent.setDate(LocalDate.of(2019, Month.FEBRUARY, 10));
            subTrackingEvent.setTimelineId(subTimelineTrackingEvent.getId());
            eventRepository.save(subTrackingEvent);

            //matko ale kiepskie nazwy
            Event subTrackingEvent2 = new Event();
            subTrackingEvent2.setTitle("AI");
            subTrackingEvent2.setDescription(lorem.substring(0, lorem.length()/2));
            subTrackingEvent2.setDate(LocalDate.of(2019, Month.JULY, 10));
            subTrackingEvent2.setTimelineId(subTimelineTrackingEvent.getId());
            eventRepository.save(subTrackingEvent2);

            Event gameEvent = new Event();
            gameEvent.setTitle("Gravity");
            gameEvent.setDescription(lorem);
            gameEvent.setDate(LocalDate.of(2020, Month.FEBRUARY, 20));
            gameEvent.setTimelineId(timeline.getId());
            eventRepository.save(gameEvent);
        }
    }

    @EventListener(ApplicationReadyEvent.class)
    public void createStartupData() throws StripeException {
        Stripe.apiKey = appProperties.getStripeKey();
        createMe();
    }
}
