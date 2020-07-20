package com.tl.backend.services;

import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.tl.backend.config.AppProperties;
import com.tl.backend.models.*;
import com.tl.backend.repositories.DeviceInfoRepository;
import com.tl.backend.repositories.StatisticsRepository;
import com.tl.backend.repositories.TimelineRepository;
import com.tl.backend.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
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
    private final UserRepository userRepository;
    private final JavaMailSender emailSender;
    private final AppProperties appProperties;

    @Autowired
    public StatisticsServiceImpl(AppProperties appProperties, JavaMailSender emailSender, UserRepository userRepository, DeviceInfoRepository deviceInfoRepository, DeviceInfoServiceImpl deviceInfoService, StatisticsRepository statisticsRepository){
        this.statisticsRepository = statisticsRepository;
        this.appProperties = appProperties;
        this.emailSender = emailSender;
        this.userRepository = userRepository;
        this.deviceInfoRepository = deviceInfoRepository;
        this.deviceInfoService = deviceInfoService;
    }

    @Override
    public void incrementHomepageViews(HttpServletRequest request) {
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
                message.setFrom(new InternetAddress("admin@tline.site", "Tline"));
                message.addRecipient(Message.RecipientType.TO, new InternetAddress("admin@tline.site"));
                message.setSubject("Review");
                message.setContent(appProperties.getMailBeginning() + "Review " + appProperties.getMailMid() + review.getOpinion() + "\n\n By: " + review.getUsername() + " " + user.getEmail() + appProperties.getMailEnd(), "text/html");
                emailSender.send(message);

            } catch (MessagingException | UnsupportedEncodingException e) {
                //e.printStackTrace();
            }
        }
    }

    //everyday at 23:00?
    @Scheduled(cron = "0 0 23 * * *")
    private void createStatistics (){
        LocalDate now = LocalDate.now();

        Statistics statistics = new Statistics();
        now = now.plusDays(1);
        statistics.setDay(now);
        statisticsRepository.save(statistics);
    }

    //everyday at 01:00?
    @Scheduled(cron = "0 0 1 * * *")
    private void activeUsers (){
        LocalDate yesterday = LocalDate.now().minusDays(1);

        Optional<Statistics> optionalStatistics = statisticsRepository.findByDay(yesterday);
        if(optionalStatistics.isPresent()){
            Statistics statistics = optionalStatistics.get();
            List<DeviceInfo> deviceInfos = deviceInfoRepository.findByLastLogged(yesterday);
            statistics.setActiveUsers(deviceInfos.size());
            statisticsRepository.save(statistics);
        }
    }
}
