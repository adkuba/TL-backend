package com.tl.backend.services;

import com.tl.backend.models.Notification;
import com.tl.backend.models.NotificationMessage;
import com.tl.backend.models.User;
import com.tl.backend.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final UserRepository userRepository;

    @Autowired
    public NotificationServiceImpl(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    @Override
    public void createNotification(String username) {
        Optional<User> optionalUser = userRepository.findByUsername(username);
        if (optionalUser.isPresent()){
            User user = optionalUser.get();
            Notification notification = new Notification();
            NotificationMessage notificationMessage = new NotificationMessage();
            notificationMessage.setUsername("tline");
            notificationMessage.setText("Welcome on Tline!");
            List<NotificationMessage> notificationMessages = new ArrayList<>();
            notificationMessages.add(notificationMessage);
            notification.setMessages(notificationMessages);
            user.setNotification(notification);
            userRepository.save(user);
        }
    }

    @Override
    public void addNotification(String toUsername, String fromUsername, String text) {
        Optional<User> optionalToUser = userRepository.findByUsername(toUsername);
        if (optionalToUser.isPresent()){
            User user = optionalToUser.get();
            Notification notification = user.getNotification();
            List<NotificationMessage> messages = notification.getMessages();
            NotificationMessage notificationMessage = new NotificationMessage();
            notificationMessage.setUsername(fromUsername);
            notificationMessage.setText(text);
            messages.add(notificationMessage);
            if (messages.size() > 10){
                messages.remove(0);
            }
            notification.setMessages(messages);
            notification.setRead(false);
            user.setNotification(notification);
            userRepository.save(user);
        }
    }

    @Override
    public Notification getNotification(String username) {
        Optional<User> optionalUser = userRepository.findByUsername(username);
        return optionalUser.map(User::getNotification).orElse(null);
    }

    @Override
    public void markRead(String username) {
        Optional<User> optionalUser = userRepository.findByUsername(username);
        if (optionalUser.isPresent()){
            User user = optionalUser.get();
            Notification notification = user.getNotification();
            notification.setRead(true);
            user.setNotification(notification);
            userRepository.save(user);
        }
    }
}
