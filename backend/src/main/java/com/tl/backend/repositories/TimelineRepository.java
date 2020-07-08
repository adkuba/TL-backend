package com.tl.backend.repositories;

import com.tl.backend.models.Timeline;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;


public interface TimelineRepository extends MongoRepository<Timeline, String> {

    @Query("{ 'eventId' : ?0 }")
    Optional<Timeline> findOneByEventId(String id);

    @Query("{ 'user.id' : ?0 }")
    List<Timeline> findAllByUserId(String id);

    boolean existsById(String id);
}
