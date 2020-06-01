package com.tl.backend.services;

import com.tl.backend.models.User;

public interface UserService {

    void deleteByUserId(String id);

    boolean changeEmail(String username, String email);

    boolean changeFullName(String username, String fullName);

    boolean changePassword(String username, String oldPassword, String newPassword);
}
