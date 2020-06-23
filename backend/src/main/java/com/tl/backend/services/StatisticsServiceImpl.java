package com.tl.backend.services;

import com.tl.backend.models.InteractionEvent;
import com.tl.backend.models.Statistics;
import com.tl.backend.models.Timeline;
import com.tl.backend.repositories.StatisticsRepository;
import com.tl.backend.repositories.TimelineRepository;
import com.tl.backend.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class StatisticsServiceImpl implements StatisticsService {

    private final StatisticsRepository statisticsRepository;

    @Autowired
    public StatisticsServiceImpl(StatisticsRepository statisticsRepository){
        this.statisticsRepository = statisticsRepository;
    }

    @Override
    public void incrementHomepageViews() {
        Optional<Statistics> optionalStatistics = statisticsRepository.findByDay(LocalDate.now());
        if (optionalStatistics.isPresent()){
            Statistics statistics = optionalStatistics.get();
            statistics.setMainPageViews(statistics.getMainPageViews() + 1);
            statisticsRepository.save(statistics);
        }
    }

    @Override
    public List<Statistics> getAllStats() {
        return statisticsRepository.findAll();
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
