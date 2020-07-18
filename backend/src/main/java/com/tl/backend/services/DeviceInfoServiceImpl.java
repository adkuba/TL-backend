package com.tl.backend.services;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CountryResponse;
import com.tl.backend.config.AppProperties;
import com.tl.backend.models.DeviceInfo;
import com.tl.backend.models.InteractionEvent;
import com.tl.backend.models.Timeline;
import com.tl.backend.models.User;
import com.tl.backend.repositories.DeviceInfoRepository;
import com.tl.backend.repositories.TimelineRepository;
import com.tl.backend.repositories.UserRepository;
import com.tl.backend.response.StatResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import ua_parser.Client;
import ua_parser.Parser;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.time.LocalDate;
import java.util.*;


@Service
public class DeviceInfoServiceImpl implements DeviceInfoService {

    private final Parser parser;
    private final DatabaseReader databaseReader;
    private final DeviceInfoRepository deviceInfoRepository;
    private final JavaMailSender emailSender;
    private final UserRepository userRepository;
    private final AppProperties appProperties;
    private final TimelineRepository timelineRepository;

    @Autowired
    public DeviceInfoServiceImpl(TimelineRepository timelineRepository, AppProperties appProperties, UserRepository userRepository, JavaMailSender emailSender, Parser parser, DatabaseReader databaseReader, DeviceInfoRepository deviceInfoRepository){
        this.parser = parser;
        this.timelineRepository = timelineRepository;
        this.appProperties = appProperties;
        this.userRepository = userRepository;
        this.emailSender = emailSender;
        this.deviceInfoRepository = deviceInfoRepository;
        this.databaseReader = databaseReader;
    }

    @Override
    public DeviceInfo createInfo(HttpServletRequest request, String username) {

        String ip = extractIp(request);
        String location = getIpLocation(ip);
        String deviceDetails = getDeviceDetails(request.getHeader("user-agent"));

        if (username != null){
            //device info for user login
            DeviceInfo existingDevice = findExistingDevice(username, deviceDetails, location);
            //existing
            if (existingDevice != null){
                existingDevice.setLastLogged(LocalDate.now());
                deviceInfoRepository.save(existingDevice);
                return existingDevice;

            } else {
                //don't exists SEND NOTIFICATION
                try {
                    Optional<User> optionalUser = userRepository.findByUsername(username);
                    if (optionalUser.isPresent()){
                        User user = optionalUser.get();
                        MimeMessage message = emailSender.createMimeMessage();
                        message.setFrom(new InternetAddress("admin@tline.site", "Tline"));
                        message.addRecipient(Message.RecipientType.TO, new InternetAddress(user.getEmail()));
                        message.setSubject("New login");
                        message.setContent(appProperties.getMailBeginning() + "Detected new login! " + appProperties.getMailMid() + "Device: " + deviceDetails + "\n Location: " + location + "\n Locale: " + request.getLocale().toString() + "\n\n You don't recognize this login? Reset password! " + appProperties.getMailEnd(), "text/html");
                        emailSender.send(message);
                    }
                } catch (MessagingException | UnsupportedEncodingException e) {
                    //e.printStackTrace();
                }
                DeviceInfo deviceInfo = new DeviceInfo();
                deviceInfo.setUsername(username);
                deviceInfo.setIp(ip);
                deviceInfo.setLocation(location);
                deviceInfo.setDeviceDetails(deviceDetails);
                deviceInfo.setLastLogged(LocalDate.now());
                deviceInfoRepository.save(deviceInfo);
                return deviceInfo;
            }
        } else {
            //device info for InteractionEvent
            DeviceInfo existingDevice = findExistingDeviceNoUser(deviceDetails, location);
            if (existingDevice != null){
                return existingDevice;

            } else {
                DeviceInfo deviceInfo = new DeviceInfo();
                deviceInfo.setIp(ip);
                deviceInfo.setLocation(location);
                deviceInfo.setDeviceDetails(deviceDetails);
                deviceInfoRepository.save(deviceInfo);
                return deviceInfo;
            }
        }
    }

    @Override
    public List<DeviceInfo> getAll() {
        return deviceInfoRepository.findAll();
    }

    @Override
    public List<StatResponse> getLocations(String timelineId) {
        Optional<Timeline> optionalTimeline = timelineRepository.findById(timelineId);
        if (optionalTimeline.isPresent()){
            Timeline timeline = optionalTimeline.get();
            List<DeviceInfo> devices = new ArrayList<>();
            for(InteractionEvent view : timeline.getViewsDetails()){
                Optional<DeviceInfo> optionalDeviceInfo = deviceInfoRepository.findById(view.getDeviceId());
                optionalDeviceInfo.ifPresent(devices :: add);
            }
            List<String> locations = new ArrayList<>();
            for (DeviceInfo deviceInfo : devices){
                locations.add(deviceInfo.getLocation());
            }
            HashSet<String> locationsHashSet = new HashSet<>(locations);
            List<StatResponse> statRespons = new ArrayList<>();
            for (String location : locationsHashSet){
                StatResponse statResponse = new StatResponse();
                statResponse.setLocation(location);
                statResponse.setNumber(Collections.frequency(locations, location));
                statRespons.add(statResponse);
            }
            return statRespons;
        }
        return null;
    }

    @Override
    public List<StatResponse> getViews(String timelineId) {
        Optional<Timeline> optionalTimeline = timelineRepository.findById(timelineId);
        if (optionalTimeline.isPresent()){
            Timeline timeline = optionalTimeline.get();
            List<LocalDate> dates = new ArrayList<>();
            for (InteractionEvent interactionEvent : timeline.getViewsDetails()){
                dates.add(interactionEvent.getDate());
            }
            HashSet<LocalDate> localDateHashSet = new HashSet<>(dates);
            List<StatResponse> statResponses = new ArrayList<>();
            for (LocalDate localDate : localDateHashSet){
                StatResponse statResponse = new StatResponse();
                statResponse.setDate(localDate);
                statResponse.setNumber(Collections.frequency(dates, localDate));
                statResponses.add(statResponse);
            }
            return statResponses;
        }
        return null;
    }

    private DeviceInfo findExistingDeviceNoUser(String deviceDetails, String location){
        List<DeviceInfo> knownDevices = deviceInfoRepository.findByLocation(location);
        for (DeviceInfo deviceInfo : knownDevices){
            if (deviceInfo.getDeviceDetails().equals(deviceDetails)) {
                return deviceInfo;
            }
        }
        return null;
    }

    private DeviceInfo findExistingDevice(String username, String deviceDetails, String location){

        List<DeviceInfo> knownDevices = deviceInfoRepository.findByUsername(username);
        for (DeviceInfo deviceInfo : knownDevices){
            if (deviceInfo.getDeviceDetails().equals(deviceDetails) && deviceInfo.getLocation().equals(location)) {
                return deviceInfo;
            }
        }
        return null;
    }

    private String extractIp(HttpServletRequest request) {
        String clientIp;
        String clientXForwardedForIp = request.getHeader("x-forwarded-for");
        if (clientXForwardedForIp != null) {
            clientIp = parseXForwardedHeader(clientXForwardedForIp);

        } else {
            clientIp = request.getRemoteAddr();
        }
        return clientIp;
    }

    private String parseXForwardedHeader(String header) {
        return header.split(" *, *")[0];
    }

    private String getIpLocation(String ip) {
        String location = "UNKNOWN";
        InetAddress ipAddress = null;
        CountryResponse countryResponse = null;

        try {
            ipAddress = InetAddress.getByName(ip);
            countryResponse = databaseReader.country(ipAddress);
        } catch (IOException | GeoIp2Exception e) {
            //e.printStackTrace();
        }

        if (countryResponse != null && countryResponse.getCountry() != null && countryResponse.getCountry().getName() != null && !countryResponse.getCountry().getName().equals("")) {
            location = countryResponse.getCountry().getName();
        }
        return location;
    }

    private String getDeviceDetails(String userAgent) {
        String deviceDetails = "UNKNOWN";

        Client client = parser.parse(userAgent);
        if (client != null) {
            deviceDetails = client.userAgent.family
                    + " " + client.userAgent.major + "."
                    + client.userAgent.minor + " - "
                    + client.os.family + " " + client.os.major
                    + "." + client.os.minor;
        }
        return deviceDetails;
    }
}
