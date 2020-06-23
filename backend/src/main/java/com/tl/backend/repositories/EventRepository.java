package com.tl.backend.repositories;

import com.tl.backend.models.Event;
import com.tl.backend.models.Timeline;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EventRepository extends MongoRepository<Event, String> {

    @Query("{ 'timelineId' : ?0 }")
    List<Event> findAllByTimelineId(String id);

}
