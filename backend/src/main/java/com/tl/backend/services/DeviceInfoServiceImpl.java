package com.tl.backend.services;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CityResponse;
import com.tl.backend.config.AppProperties;
import com.tl.backend.models.DeviceInfo;
import com.tl.backend.models.User;
import com.tl.backend.repositories.DeviceInfoRepository;
import com.tl.backend.repositories.UserRepository;
import com.tl.backend.response.MessageResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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
import java.net.UnknownHostException;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;


@Service
public class DeviceInfoServiceImpl implements DeviceInfoService {

    private final Parser parser;
    private final DatabaseReader databaseReader;
    private final DeviceInfoRepository deviceInfoRepository;
    private final JavaMailSender emailSender;
    private final UserRepository userRepository;
    private final AppProperties appProperties;

    @Autowired
    public DeviceInfoServiceImpl(AppProperties appProperties, UserRepository userRepository, JavaMailSender emailSender, Parser parser, DatabaseReader databaseReader, DeviceInfoRepository deviceInfoRepository){
        this.parser = parser;
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
                existingDevice.setLastLoggedIn(LocalDate.now());
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
                deviceInfo.setLastLoggedIn(LocalDate.now());
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
        CityResponse cityResponse = null;

        try {
            ipAddress = InetAddress.getByName(ip);
            cityResponse = databaseReader.city(ipAddress);
        } catch (IOException | GeoIp2Exception e) {
            //e.printStackTrace();
        }

        if (cityResponse != null && cityResponse.getCity() != null && cityResponse.getCity().getName() != null && !cityResponse.getCity().getName().equals("")) {
            location = cityResponse.getCity().getName();
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
