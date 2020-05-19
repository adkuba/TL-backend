package com.tl.backend.services;

import com.tl.backend.entities.Event;
import com.tl.backend.entities.Timeline;
import com.tl.backend.entities.User;
import com.tl.backend.repositories.EventRepository;
import com.tl.backend.repositories.TimelineRepository;
import com.tl.backend.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.HashMap;

@Service
public class DataStartupService {

    private final UserRepository userRepository;
    private final TimelineRepository timelineRepository;
    private final EventRepository eventRepository;

    @Autowired
    public DataStartupService(UserRepository userRepository, TimelineRepository timelineRepository, EventRepository eventRepository){
        this.userRepository = userRepository;
        this.timelineRepository = timelineRepository;
        this.eventRepository = eventRepository;
    }

    private void createMe(){
        String lorem = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Phasellus gravida dui feugiat risus vehicula, vel placerat velit auctor. Sed lobortis tellus ut ullamcorper sagittis. Duis eget aliquam metus, ac sodales nunc. Integer imperdiet feugiat feugiat. Sed vel auctor nisi. Suspendisse potenti. Donec tellus velit, fringilla ac orci vitae, rhoncus placerat lacus. Integer in dictum nulla. Class aptent taciti sociosqu ad litora torquent per conubia nostra, per inceptos himenaeos. Vestibulum a risus nec eros pretium rhoncus. Integer a dignissim urna. Duis tempus odio eu elit consequat, eget placerat tortor sodales. Nulla finibus, nisi sed pellentesque laoreet, purus orci tincidunt sem, sit amet egestas sem dui in lorem.";

        User kuba = new User();
        kuba.setFullName("Jakub Adamski");
        kuba.setEmail("akuba@exemplum.pl");
        kuba.setUsername("akuba");
        kuba.setPassword("funia");
        userRepository.save(kuba);

        Timeline timeline = new Timeline();
        timeline.setUser(kuba);
        timeline.setDescriptionTitle("O mnie");
        timeline.setDescription(lorem);
        timeline.setId("kubatl");
        timelineRepository.save(timeline);

        Event trackingEvent = new Event();
        trackingEvent.setTitle("Object Tracking");
        trackingEvent.setShortDescription("Project using machine learning.");
        trackingEvent.setDescription(lorem);
        trackingEvent.setDate(LocalDateTime.of(2019, Month.OCTOBER, 20, 0, 0));
        HashMap<String, String> trackingEventLinks = new HashMap<String, String>();
        trackingEventLinks.put("Repozytorium", "link do repoo");
        trackingEventLinks.put("Artykul", "link go medium");
        trackingEvent.setLinks(trackingEventLinks);
        trackingEvent.setTimeline(timeline);
        eventRepository.save(trackingEvent);

        Timeline subTimelineTrackingEvent = new Timeline();
        subTimelineTrackingEvent.setEvent(trackingEvent);
        timelineRepository.save(subTimelineTrackingEvent);

        Event subTrackingEvent = new Event();
        subTrackingEvent.setTitle("Aplikacja");
        subTrackingEvent.setShortDescription("Aplikacja na IOSa");
        subTrackingEvent.setDescription(lorem.substring(0, lorem.length()/2));
        subTrackingEvent.setDate(LocalDateTime.of(2019, Month.FEBRUARY, 10, 0, 0));
        HashMap<String, String> subTrackingEventLinks = new HashMap<String, String>();
        subTrackingEvent.setLinks(subTrackingEventLinks);
        subTrackingEvent.setTimeline(subTimelineTrackingEvent);
        eventRepository.save(subTrackingEvent);

        //matko ale kiepskie nazwy
        Event subTrackingEvent2 = new Event();
        subTrackingEvent2.setTitle("AI");
        subTrackingEvent2.setShortDescription("Wykorzystanie uczenia maszynowego");
        subTrackingEvent2.setDescription(lorem.substring(0, lorem.length()/2));
        subTrackingEvent2.setDate(LocalDateTime.of(2019, Month.JULY, 10, 0, 0));
        HashMap<String, String> subTrackingEventLinks2 = new HashMap<String, String>();
        subTrackingEventLinks2.put("Demo", "link do demo");
        subTrackingEvent2.setLinks(subTrackingEventLinks2);
        subTrackingEvent2.setTimeline(subTimelineTrackingEvent);
        eventRepository.save(subTrackingEvent2);

        Event gameEvent = new Event();
        gameEvent.setTitle("Gravity");
        gameEvent.setShortDescription("Game created in Unity");
        gameEvent.setDescription(lorem);
        gameEvent.setDate(LocalDateTime.of(2020, Month.FEBRUARY, 20, 0, 0));
        HashMap<String, String> gameEventLinks = new HashMap<String, String>();
        gameEventLinks.put("Repozytorium", "link do repoo");
        gameEventLinks.put("Google Play", "link go playa");
        gameEvent.setLinks(gameEventLinks);
        gameEvent.setTimeline(timeline);
        eventRepository.save(gameEvent);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void createStartupData(){
        createMe();
    }
}
