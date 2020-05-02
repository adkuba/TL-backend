package com.tl.backend.services;

import com.tl.backend.entities.User;

public interface UserService {

    User saveUser(User user);

    void deleteByUserId(String id);
}
