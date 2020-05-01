package com.tl.backend.controllers;

import com.tl.backend.services.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/events")
public class EventController {

    private final EventService eventService;

    //autowired to wstrzykiwanie obiektow
    @Autowired
    public EventController(EventService eventService){
        this.eventService = eventService;
    }
}
