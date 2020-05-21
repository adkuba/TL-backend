package com.tl.backend.repositories;

import com.tl.backend.models.Timeline;
import org.springframework.data.mongodb.repository.MongoRepository;


public interface TimelineRepository extends MongoRepository<Timeline, String> {
}
