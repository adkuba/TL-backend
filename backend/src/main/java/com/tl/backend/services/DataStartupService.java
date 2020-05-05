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
        User kuba = new User();
        kuba.setEmail("akuba@exemplum.pl");
        kuba.setUsername("akuba");
        kuba.setPassword("funia");
        userRepository.save(kuba);

        Timeline timeline = new Timeline();
        timeline.setUser(kuba);
        timelineRepository.save(timeline);

        Event trackingEvent = new Event();
        trackingEvent.setTitle("Object Tracking");
        trackingEvent.setShortDescription("Project using machine learning.");
        trackingEvent.setDescription("HEHEHEHEHEHEHEH nie wiem co tutaj wpisac.");
        trackingEvent.setDate(LocalDateTime.of(2019, Month.OCTOBER, 20, 0, 0));
        trackingEvent.setTimeline(timeline);
        eventRepository.save(trackingEvent);

        Event gameEvent = new Event();
        gameEvent.setTitle("Gravity");
        gameEvent.setShortDescription("Game created in Unity");
        gameEvent.setDescription("HEHEHEHEHEHEHEH nie wiem co tutaj wpisac2.");
        gameEvent.setDate(LocalDateTime.of(2020, Month.FEBRUARY, 20, 0, 0));
        gameEvent.setTimeline(timeline);
        eventRepository.save(gameEvent);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void createStartupData(){
        createMe();
    }
}
