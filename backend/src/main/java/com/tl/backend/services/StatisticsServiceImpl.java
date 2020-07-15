package com.tl.backend.services;

import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.tl.backend.models.*;
import com.tl.backend.repositories.StatisticsRepository;
import com.tl.backend.repositories.TimelineRepository;
import com.tl.backend.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class StatisticsServiceImpl implements StatisticsService {

    private final StatisticsRepository statisticsRepository;
    private final DeviceInfoServiceImpl deviceInfoService;

    @Autowired
    public StatisticsServiceImpl(DeviceInfoServiceImpl deviceInfoService, StatisticsRepository statisticsRepository){
        this.statisticsRepository = statisticsRepository;
        this.deviceInfoService = deviceInfoService;
    }

    @Override
    public void incrementHomepageViews(HttpServletRequest request) {
        Optional<Statistics> optionalStatistics = statisticsRepository.findByDay(LocalDate.now());
        if (optionalStatistics.isPresent()){
            Statistics statistics = optionalStatistics.get();
            DeviceInfo deviceInfo = deviceInfoService.createInfo(request, null);
            List<String> devices = statistics.getDevices();
            if (devices.stream().noneMatch(o -> o.equals(deviceInfo.getId()))){
                devices.add(deviceInfo.getId());
                statistics.setDevices(devices);
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
        Optional<Statistics> optionalStatistics = statisticsRepository.findByDay(LocalDate.now());
        if (optionalStatistics.isPresent()){
            Statistics statistics = optionalStatistics.get();
            List<Review> reviews = statistics.getReviews();
            reviews.add(review);
            statistics.setReviews(reviews);
            statisticsRepository.save(statistics);
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
}
