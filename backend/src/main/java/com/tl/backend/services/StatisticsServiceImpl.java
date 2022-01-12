package com.tl.backend.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Storage;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.tl.backend.config.AppProperties;
import com.tl.backend.fileHandling.FileResource;
import com.tl.backend.fileHandling.FileResourceRepository;
import com.tl.backend.fileHandling.FileServiceImpl;
import com.tl.backend.models.*;
import com.tl.backend.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class StatisticsServiceImpl implements StatisticsService {

    private final StatisticsRepository statisticsRepository;
    private final DeviceInfoServiceImpl deviceInfoService;
    private final DeviceInfoRepository deviceInfoRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final FileServiceImpl fileService;
    private final FileResourceRepository fileResourceRepository;
    private final TimelineRepository timelineRepository;
    private final RoleRepository roleRepository;
    private final JavaMailSender emailSender;
    private final AppProperties appProperties;
    private final ObjectMapper objectMapper;
    private final Storage storage;

    @Autowired
    public StatisticsServiceImpl(EventRepository eventRepository, RoleRepository roleRepository, TimelineRepository timelineRepository, FileResourceRepository fileResourceRepository, FileServiceImpl fileService, Storage storage, AppProperties appProperties, JavaMailSender emailSender, UserRepository userRepository, DeviceInfoRepository deviceInfoRepository, DeviceInfoServiceImpl deviceInfoService, StatisticsRepository statisticsRepository){
        this.statisticsRepository = statisticsRepository;
        this.eventRepository = eventRepository;
        this.timelineRepository = timelineRepository;
        this.roleRepository = roleRepository;
        this.fileResourceRepository = fileResourceRepository;
        this.fileService = fileService;
        this.appProperties = appProperties;
        this.emailSender = emailSender;
        this.userRepository = userRepository;
        this.deviceInfoRepository = deviceInfoRepository;
        this.deviceInfoService = deviceInfoService;

        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        this.storage = storage;
    }

    @Override
    public void incrementHomepageViews(HttpServletRequest request) {
        checkStatistics();
        Optional<Statistics> optionalStatistics = statisticsRepository.findByDay(LocalDate.now());
        if (optionalStatistics.isPresent()){
            Statistics statistics = optionalStatistics.get();
            DeviceInfo deviceInfo = deviceInfoService.createInfo(request, null);
            Map<String, Long> devices = statistics.getDevices();
            if (!devices.containsKey(deviceInfo.getId())){
                devices.put(deviceInfo.getId(), 0L);
                statistics.setDevices(devices);
            } else {
                devices.put(deviceInfo.getId(), devices.get(deviceInfo.getId())+1 );
            }
            statistics.setMainPageViews(statistics.getMainPageViews() + 1);
            statisticsRepository.save(statistics);
        }
    }

    @Override
    public List<Statistics> getAllStats() {
        return statisticsRepository.findAll();
    }

    @Override
    public void addReview(Review review) {
        Optional<User> optionalUser = userRepository.findByUsername(review.getUsername());
        if (optionalUser.isPresent()){
            User user = optionalUser.get();
            try {
                MimeMessage message = emailSender.createMimeMessage();
                message.setFrom(new InternetAddress("quicpos@gmail.com", "Tline"));
                message.addRecipient(Message.RecipientType.TO, new InternetAddress("quicpos@gmail.com"));
                message.setSubject("Review");
                message.setContent(appProperties.getMailBeginning() + "Review " + appProperties.getMailMid() + review.getOpinion() + "\n\n By: " + review.getUsername() + " " + user.getEmail() + appProperties.getMailEnd(), "text/html");
                emailSender.send(message);

            } catch (MessagingException | UnsupportedEncodingException e) {
                //e.printStackTrace();
            }
        }
    }

    @Override
    public void checkStatistics() {
        Optional<Statistics> optionalStatistics = statisticsRepository.findByDay(LocalDate.now());
        if (optionalStatistics.isEmpty()){
            Statistics statistics = new Statistics();
            statistics.setDay(LocalDate.now());
            statisticsRepository.save(statistics);
        }
    }

    @Override
    public void createBackup() throws IOException {
        //users
        List<User> users = userRepository.findAll();
        String usersJson = objectMapper.writeValueAsString(users);
        storage.delete("tline-files", "usersBackup.json");
        fileService.deleteFileResource("usersBackup.json");
        fileService.saveFileResource(usersJson, "usersBackup.json");

        //timelines
        List<Timeline> timelines = timelineRepository.findAll();
        String timelinesJson = objectMapper.writeValueAsString(timelines);
        storage.delete("tline-files", "timelinesBackup.json");
        fileService.deleteFileResource("timelinesBackup.json");
        fileService.saveFileResource(timelinesJson, "timelinesBackup.json");

        //statistics
        List<Statistics> statistics = statisticsRepository.findAll();
        String statisticsJson = objectMapper.writeValueAsString(statistics);
        storage.delete("tline-files", "statisticsBackup.json");
        fileService.deleteFileResource("statisticsBackup.json");
        fileService.saveFileResource(statisticsJson, "statisticsBackup.json");

        //roles
        List<Role> roles = roleRepository.findAll();
        String rolesJson = objectMapper.writeValueAsString(roles);
        storage.delete("tline-files", "rolesBackup.json");
        fileService.deleteFileResource("rolesBackup.json");
        fileService.saveFileResource(rolesJson, "rolesBackup.json");

        //files
        List<FileResource> files = fileResourceRepository.findAll();
        String filesJson = objectMapper.writeValueAsString(files);
        storage.delete("tline-files", "filesBackup.json");
        fileService.deleteFileResource("filesBackup.json");
        fileService.saveFileResource(filesJson, "filesBackup.json");

        //events
        List<Event> events = eventRepository.findAll();
        String eventsJson = objectMapper.writeValueAsString(events);
        storage.delete("tline-files", "eventsBackup.json");
        fileService.deleteFileResource("eventsBackup.json");
        fileService.saveFileResource(eventsJson, "eventsBackup.json");

        //deviceInfo
        List<DeviceInfo> devices = deviceInfoRepository.findAll();
        String devicesJson = objectMapper.writeValueAsString(devices);
        storage.delete("tline-files", "devicesBackup.json");
        fileService.deleteFileResource("devicesBackup.json");
        fileService.saveFileResource(devicesJson, "devicesBackup.json");
    }

    @Override
    public void restoreFromBackup() throws JsonProcessingException {
        //files
        Blob blob = storage.get("tline-files", "filesBackup.json");
        String filesJson = new String(blob.getContent());
        List<FileResource> files = objectMapper.readValue(filesJson, objectMapper.getTypeFactory().constructCollectionType(List.class, FileResource.class));
        fileResourceRepository.deleteAll();
        fileResourceRepository.saveAll(files);
        fileResourceRepository.save(fileService.createFileResource(filesJson, "filesBackup.json"));

        //users
        blob = storage.get("tline-files", "usersBackup.json");
        String usersJson = new String(blob.getContent());
        List<User> users = objectMapper.readValue(usersJson, objectMapper.getTypeFactory().constructCollectionType(List.class, User.class));
        userRepository.deleteAll();
        userRepository.saveAll(users);
        fileResourceRepository.save(fileService.createFileResource(usersJson, "usersBackup.json"));

        //timelines
        blob = storage.get("tline-files", "timelinesBackup.json");
        String timelinesJson = new String(blob.getContent());
        List<Timeline> timelines = objectMapper.readValue(timelinesJson, objectMapper.getTypeFactory().constructCollectionType(List.class, Timeline.class));
        timelineRepository.deleteAll();
        timelineRepository.saveAll(timelines);
        fileResourceRepository.save(fileService.createFileResource(timelinesJson, "timelinesBackup.json"));

        //statistics
        blob = storage.get("tline-files", "statisticsBackup.json");
        String statisticsJson = new String(blob.getContent());
        List<Statistics> statistics = objectMapper.readValue(statisticsJson, objectMapper.getTypeFactory().constructCollectionType(List.class, Statistics.class));
        statisticsRepository.deleteAll();
        statisticsRepository.saveAll(statistics);
        fileResourceRepository.save(fileService.createFileResource(statisticsJson, "statisticsBackup.json"));

        //roles
        blob = storage.get("tline-files", "rolesBackup.json");
        String rolesJson = new String(blob.getContent());
        List<Role> roles = objectMapper.readValue(rolesJson, objectMapper.getTypeFactory().constructCollectionType(List.class, Role.class));
        roleRepository.deleteAll();
        roleRepository.saveAll(roles);
        fileResourceRepository.save(fileService.createFileResource(rolesJson, "rolesBackup.json"));

        //events
        blob = storage.get("tline-files", "eventsBackup.json");
        String eventsJson = new String(blob.getContent());
        List<Event> events = objectMapper.readValue(eventsJson, objectMapper.getTypeFactory().constructCollectionType(List.class, Event.class));
        eventRepository.deleteAll();
        eventRepository.saveAll(events);
        fileResourceRepository.save(fileService.createFileResource(eventsJson, "eventsBackup.json"));

        //deviceInfo
        blob = storage.get("tline-files", "devicesBackup.json");
        String devicesJson = new String(blob.getContent());
        List<DeviceInfo> devices = objectMapper.readValue(devicesJson, objectMapper.getTypeFactory().constructCollectionType(List.class, DeviceInfo.class));
        deviceInfoRepository.deleteAll();
        deviceInfoRepository.saveAll(devices);
        fileResourceRepository.save(fileService.createFileResource(devicesJson, "devicesBackup.json"));
    }
}
