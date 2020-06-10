package com.tl.backend.services;

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
    private final TimelineRepository timelineRepository;
    private final UserRepository userRepository;

    @Autowired
    public StatisticsServiceImpl(UserRepository userRepository, TimelineRepository timelineRepository, StatisticsRepository statisticsRepository){
        this.statisticsRepository = statisticsRepository;
        this.userRepository = userRepository;
        this.timelineRepository = timelineRepository;
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
        //WARNING im getting all timelines in database!
        List<Timeline> allTimelines = timelineRepository.findAll();
        long totalViews = 0;
        for (Timeline timeline : allTimelines){
            if (timeline.getUser() != null){
                totalViews += timeline.getViews();
                timeline.setTwoDaysBeforeViews(timeline.getDayBeforeViews());
                timeline.setDayBeforeViews(timeline.getViews());
                timeline.updateTrending();
                timelineRepository.save(timeline);
            }
        }
        Optional<Statistics> optionalStatistics = statisticsRepository.findByDay(now);
        if (optionalStatistics.isPresent()){
            Statistics oldStat = optionalStatistics.get();
            oldStat.setTotalTimelinesViews(totalViews);
            oldStat.setNumberOfUsers(userRepository.count());
            statisticsRepository.save(oldStat);
        }

        Statistics statistics = new Statistics();
        now = now.plusDays(1);
        statistics.setDay(now);
        statisticsRepository.save(statistics);
    }
}
