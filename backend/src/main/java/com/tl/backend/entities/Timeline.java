package com.tl.backend.entities;

import org.springframework.data.annotation.Id;

public class Timeline {

    @Id
    private String id;

    private User user;

    private Event event;
}
