package com.tl.backend.services;

import com.tl.backend.fileHandling.FileResource;
import com.tl.backend.repositories.FileResourceRepository;
import com.tl.backend.fileHandling.FileServiceImpl;
import com.tl.backend.models.Event;
import com.tl.backend.models.Timeline;
import com.tl.backend.repositories.EventRepository;
import com.tl.backend.repositories.TimelineRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final FileServiceImpl fileService;
    private final FileResourceRepository fileResourceRepository;
    private final TimelineRepository timelineRepository;

    @Autowired
    public EventServiceImpl(TimelineRepository timelineRepository, FileResourceRepository fileResourceRepository, EventRepository eventRepository, FileServiceImpl fileService){
        this.eventRepository = eventRepository;
        this.fileResourceRepository = fileResourceRepository;
        this.fileService = fileService;
        this.timelineRepository = timelineRepository;
    }


    @Override
    public List<Event> getEventsByTimelineId(String timelineId, Boolean view) {
        List<Event> events = eventRepository.findAllByTimelineId(timelineId);
        //stats
        if (view){
            Optional<Timeline> optionalTimeline = timelineRepository.findById(timelineId);
            if (optionalTimeline.isPresent()){
                Timeline timeline = optionalTimeline.get();
                timeline.setViews(timeline.getViews() + 1);
                timelineRepository.save(timeline);
            }
        }
        return events;
    }

    @Override
    public Event setPictures(String id, List<MultipartFile> pictures){
        Optional<Event> opPicEvent = eventRepository.findById(id);
        if (opPicEvent.isPresent()){
            Event picEvent = opPicEvent.get();
            List<FileResource> fileResources = new ArrayList<>();
            for (MultipartFile picture : pictures){
                fileResources.add(fileService.saveFileResource(picture));
            }
            picEvent.setPictures(fileResources);
            return eventRepository.save(picEvent);
        }
        return null;
    }

    @Override
    public Event setPicturesURL(String id, List<String> urls) {
        Optional<Event> opPicEvent = eventRepository.findById(id);
        if (opPicEvent.isPresent()){
            Event picEvent = opPicEvent.get();
            List<FileResource> pictures = picEvent.getPictures();
            for (String url : urls){
                String[] parts = url.split("/");
                Optional<FileResource> optionalFileResource = fileResourceRepository.findById(parts[parts.length-1]);
                if (optionalFileResource.isPresent()){
                    FileResource picture = optionalFileResource.get();
                    pictures.add(picture);
                }
            }
            picEvent.setPictures(pictures);

            return eventRepository.save(picEvent);
        }
        return null;
    }

    @Override
    public Event saveEvent(Event event) {
        return eventRepository.save(event);
    }

    @Override
    public void deleteByEventId(String id) {
        eventRepository.deleteById(id);
    }
}
