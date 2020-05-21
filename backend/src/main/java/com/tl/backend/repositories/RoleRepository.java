package com.tl.backend.repositories;

import com.tl.backend.models.ERole;
import com.tl.backend.models.Role;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface RoleRepository extends MongoRepository<Role, String> {
    Optional<Role> findByName(ERole name);
}
