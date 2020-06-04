package com.tl.backend.services;

import com.tl.backend.models.*;
import com.tl.backend.repositories.EventRepository;
import com.tl.backend.repositories.RoleRepository;
import com.tl.backend.repositories.TimelineRepository;
import com.tl.backend.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

@Service
public class DataStartupService {

    private final UserRepository userRepository;
    private final TimelineRepository timelineRepository;
    private final EventRepository eventRepository;
    private final RoleRepository roleRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    public DataStartupService(UserRepository userRepository, TimelineRepository timelineRepository, EventRepository eventRepository, RoleRepository roleRepository){
        this.userRepository = userRepository;
        this.timelineRepository = timelineRepository;
        this.eventRepository = eventRepository;
        this.roleRepository = roleRepository;
    }

    private void createMe(){
        String lorem = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Phasellus gravida dui feugiat risus vehicula, vel placerat velit auctor. Sed lobortis tellus ut ullamcorper sagittis. Duis eget aliquam metus, ac sodales nunc. Integer imperdiet feugiat feugiat. Sed vel auctor nisi. Suspendisse potenti. Donec tellus velit, fringilla ac orci vitae, rhoncus placerat lacus. Integer in dictum nulla. Class aptent taciti sociosqu ad litora torquent per conubia nostra, per inceptos himenaeos. Vestibulum a risus nec eros pretium rhoncus. Integer a dignissim urna. Duis tempus odio eu elit consequat, eget placerat tortor sodales. Nulla finibus, nisi sed pellentesque laoreet, purus orci tincidunt sem, sit amet egestas sem dui in lorem.";

        if (userRepository.findByUsername("akuba").isEmpty()){
            Role admin = new Role();
            admin.setName(ERole.ROLE_ADMIN);
            roleRepository.save(admin);
            Role userR = new Role();
            userR.setName(ERole.ROLE_USER);
            roleRepository.save(userR);

            User kuba = new User();
            kuba.setFullName("Jakub Adamski");
            kuba.setEmail("akuba@exemplum.pl");
            kuba.setUsername("akuba");
            kuba.setPassword(encoder.encode("funia"));
            Set<Role> roles = new HashSet<>();
            roles.add(admin);
            kuba.setRoles(roles);
            userRepository.save(kuba);


            //main
            Timeline timeline = new Timeline();
            timeline.setUser(kuba);
            timeline.setDescriptionTitle("Me");
            timeline.setDescription(lorem);
            timeline.setId("kubatl");
            timelineRepository.save(timeline);
            //2
            Timeline timeline2 = new Timeline();
            timeline2.setUser(kuba);
            timeline2.setDescriptionTitle("Pictures");
            timeline2.setDescription(lorem);
            timeline2.setId("kubatl2");
            timelineRepository.save(timeline2);
            //3
            Timeline timeline3 = new Timeline();
            timeline3.setUser(kuba);
            timeline3.setDescriptionTitle("Projects");
            timeline3.setDescription(lorem);
            timeline3.setId("kubatl3");
            timelineRepository.save(timeline3);
            //4
            Timeline timeline4 = new Timeline();
            timeline4.setUser(kuba);
            timeline4.setDescriptionTitle("Books");
            timeline4.setDescription(lorem);
            timeline4.setId("kubatl4");
            timelineRepository.save(timeline4);
            //5
            Timeline timeline5 = new Timeline();
            timeline5.setUser(kuba);
            timeline5.setDescriptionTitle("Company");
            timeline5.setDescription(lorem);
            timeline5.setId("kubatl5");
            timelineRepository.save(timeline5);
            //6
            Timeline timeline6 = new Timeline();
            timeline6.setUser(kuba);
            timeline6.setDescriptionTitle("Sport");
            timeline6.setDescription(lorem);
            timeline6.setId("kubatl6");
            timelineRepository.save(timeline6);



            Event trackingEvent = new Event();
            trackingEvent.setTitle("Object Tracking");
            trackingEvent.setShortDescription("Project using machine learning.");
            trackingEvent.setDescription(lorem);
            trackingEvent.setDate(LocalDate.of(2019, Month.OCTOBER, 20));
            HashMap<String, String> trackingEventLinks = new HashMap<String, String>();
            trackingEventLinks.put("Repozytorium", "link do repoo");
            trackingEventLinks.put("Artykul", "link go medium");
            trackingEvent.setLinks(trackingEventLinks);
            trackingEvent.setTimeline(timeline);
            eventRepository.save(trackingEvent);

            Timeline subTimelineTrackingEvent = new Timeline();
            subTimelineTrackingEvent.setId("kubatl-sub1");
            subTimelineTrackingEvent.setEvent(trackingEvent);
            timelineRepository.save(subTimelineTrackingEvent);

            Event subTrackingEvent = new Event();
            subTrackingEvent.setTitle("Aplikacja");
            subTrackingEvent.setShortDescription("Aplikacja na IOSa");
            subTrackingEvent.setDescription(lorem.substring(0, lorem.length()/2));
            subTrackingEvent.setDate(LocalDate.of(2019, Month.FEBRUARY, 10));
            HashMap<String, String> subTrackingEventLinks = new HashMap<String, String>();
            subTrackingEvent.setLinks(subTrackingEventLinks);
            subTrackingEvent.setTimeline(subTimelineTrackingEvent);
            eventRepository.save(subTrackingEvent);

            //matko ale kiepskie nazwy
            Event subTrackingEvent2 = new Event();
            subTrackingEvent2.setTitle("AI");
            subTrackingEvent2.setShortDescription("Wykorzystanie uczenia maszynowego");
            subTrackingEvent2.setDescription(lorem.substring(0, lorem.length()/2));
            subTrackingEvent2.setDate(LocalDate.of(2019, Month.JULY, 10));
            HashMap<String, String> subTrackingEventLinks2 = new HashMap<String, String>();
            subTrackingEventLinks2.put("Demo", "link do demo");
            subTrackingEvent2.setLinks(subTrackingEventLinks2);
            subTrackingEvent2.setTimeline(subTimelineTrackingEvent);
            eventRepository.save(subTrackingEvent2);

            Event gameEvent = new Event();
            gameEvent.setTitle("Gravity");
            gameEvent.setShortDescription("Game created in Unity");
            gameEvent.setDescription(lorem);
            gameEvent.setDate(LocalDate.of(2020, Month.FEBRUARY, 20));
            HashMap<String, String> gameEventLinks = new HashMap<String, String>();
            gameEventLinks.put("Repozytorium", "link do repoo");
            gameEventLinks.put("Google Play", "link go playa");
            gameEvent.setLinks(gameEventLinks);
            gameEvent.setTimeline(timeline);
            eventRepository.save(gameEvent);
        }
    }

    @EventListener(ApplicationReadyEvent.class)
    public void createStartupData(){
        createMe();
    }
}
