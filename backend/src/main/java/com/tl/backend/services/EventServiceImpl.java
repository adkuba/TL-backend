package com.tl.backend.services;

import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.tl.backend.fileHandling.FileResource;
import com.tl.backend.fileHandling.FileResourceRepository;
import com.tl.backend.fileHandling.FileService;
import com.tl.backend.fileHandling.FileServiceImpl;
import com.tl.backend.models.*;
import com.tl.backend.repositories.EventRepository;
import com.tl.backend.repositories.StatisticsRepository;
import com.tl.backend.repositories.TimelineRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

@Service
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final FileServiceImpl fileService;
    private final FileResourceRepository fileResourceRepository;
    private final TimelineRepository timelineRepository;
    private final StatisticsRepository statisticsRepository;
    private final DeviceInfoServiceImpl deviceInfoService;
    private final StatisticsServiceImpl statisticsService;

    @Autowired
    public EventServiceImpl(StatisticsServiceImpl statisticsService, DeviceInfoServiceImpl deviceInfoService, StatisticsRepository statisticsRepository, TimelineRepository timelineRepository, FileResourceRepository fileResourceRepository, EventRepository eventRepository, FileServiceImpl fileService){
        this.eventRepository = eventRepository;
        this.deviceInfoService = deviceInfoService;
        this.statisticsRepository = statisticsRepository;
        this.statisticsService = statisticsService;
        this.fileResourceRepository = fileResourceRepository;
        this.fileService = fileService;
        this.timelineRepository = timelineRepository;
    }


    @Override
    public List<Event> getEventsByTimelineId(String timelineId, Boolean view, HttpServletRequest request) {
        List<Event> events = eventRepository.findAllByTimelineId(timelineId);
        //stats
        if (view){
            Optional<Timeline> optionalTimeline = timelineRepository.findById(timelineId);
            if (optionalTimeline.isPresent()){
                Timeline timeline = optionalTimeline.get();
                DeviceInfo deviceInfo = deviceInfoService.createInfo(request, null);
                Map<LocalDate, Map<String, Long>> views = timeline.getViewsDetails();
                //first in day
                if (!views.containsKey(LocalDate.now())){
                    Map<String, Long> deviceInDay = new HashMap<>();
                    deviceInDay.put(deviceInfo.getId(), 1L);
                    views.put(LocalDate.now(), deviceInDay);
                } else {
                    //there is day
                    Map<String, Long> devicesInDay = views.get(LocalDate.now());
                    if (devicesInDay.containsKey(deviceInfo.getId())){
                        //this device exists
                        devicesInDay.put(deviceInfo.getId(), devicesInDay.get(deviceInfo.getId()) + 1);
                    } else {
                        //this device doesn't exists
                        devicesInDay.put(deviceInfo.getId(), 0L);
                    }
                    views.put(LocalDate.now(), devicesInDay);
                }
                timeline.setViewsDetails(views);
                timeline.viewsNumber();
                timelineRepository.save(timeline);

                //main stats
                statisticsService.checkStatistics();
                Optional<Statistics> optionalStatistics = statisticsRepository.findByDay(LocalDate.now());
                if (optionalStatistics.isPresent()){
                    Statistics statistics = optionalStatistics.get();
                    statistics.setTotalTimelinesViews(statistics.getTotalTimelinesViews() + 1);
                    Map<String, Long> devices = statistics.getDevices();
                    if (!devices.containsKey(deviceInfo.getId())){
                        devices.put(deviceInfo.getId(), 0L);
                        statistics.setDevices(devices);
                    } else {
                        devices.put(deviceInfo.getId(), devices.get(deviceInfo.getId())+1 );
                    }
                    statisticsRepository.save(statistics);
                }
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
                String type = Objects.requireNonNull(picture.getContentType()).split("/")[0];
                if (type.equals("image")) {
                    fileResources.add(fileService.saveFileResource(picture));
                }
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
