package com.tl.backend.repositories;

import com.tl.backend.models.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {

    Optional<User> findByUsername(String username);

    Optional<User> findUserByRefreshToken(String refreshToken);

    @Query("{ 'passwordResetToken.token' : ?0 }")
    Optional<User> findByPasswordResetToken(String passwordRefreshToken);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    Optional<User> findUserByEmail(String email);
}
