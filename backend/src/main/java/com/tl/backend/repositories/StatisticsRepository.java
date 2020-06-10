package com.tl.backend.repositories;

import com.tl.backend.models.Statistics;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface StatisticsRepository extends MongoRepository<Statistics, String> {

    Optional<Statistics> findByDay(LocalDate day);
}
