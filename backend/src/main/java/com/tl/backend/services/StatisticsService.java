package com.tl.backend.services;

import com.tl.backend.models.Statistics;

import java.util.List;

public interface StatisticsService {

    void incrementHomepageViews();

    List<Statistics> getAllStats();
}
