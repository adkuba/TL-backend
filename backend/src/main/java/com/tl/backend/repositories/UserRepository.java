package com.tl.backend.repositories;

import com.tl.backend.entities.User;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRepository extends MongoRepository<User, String> {
    //zrobic np findby username
}
