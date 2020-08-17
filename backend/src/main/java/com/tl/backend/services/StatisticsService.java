package com.tl.backend.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.tl.backend.models.Review;
import com.tl.backend.models.Statistics;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;

public interface StatisticsService {

    void incrementHomepageViews(HttpServletRequest request);

    List<Statistics> getAllStats();

    void addReview(Review review);

    void checkStatistics();

    void createBackup() throws IOException;

    void restoreFromBackup() throws JsonProcessingException;
}
