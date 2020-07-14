package com.tl.backend.services;

import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.tl.backend.models.Event;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;

public interface EventService {

    List<Event> getEventsByTimelineId(String timelineId, Boolean view, HttpServletRequest request);

    Event saveEvent(Event event);

    void deleteByEventId(String id);

    Event setPictures(String id, List<MultipartFile> picture);

    Event setPicturesURL(String id, List<String> urls);
}
