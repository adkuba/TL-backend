package com.tl.backend.services;

import com.tl.backend.models.Notification;

public interface NotificationService {

    void createNotification(String username);

    void addNotification(String toUsername, String fromUsername, String text);

    Notification getNotification(String username);

    void markRead(String username);
}
