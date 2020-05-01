package com.tl.backend.entities;

import org.springframework.data.annotation.Id;

import java.util.Date;

public class Event {

    @Id
    private String id;

    private String title;

    private Timeline timeline;

    private String description;

    private Date date;
}
