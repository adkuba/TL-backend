package com.tl.backend.repositories;

import com.tl.backend.models.Event;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface EventRepository extends MongoRepository<Event, String> {
    //cos npisac?
}
