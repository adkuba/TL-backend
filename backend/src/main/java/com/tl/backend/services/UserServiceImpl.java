package com.tl.backend.services;

import com.tl.backend.models.Timeline;
import com.tl.backend.models.User;
import com.tl.backend.repositories.TimelineRepository;
import com.tl.backend.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final TimelineService timelineService;
    private final TimelineRepository timelineRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, TimelineService timelineService, TimelineRepository timelineRepository){
        this.userRepository = userRepository;
        this.timelineService = timelineService;
        this.timelineRepository = timelineRepository;
    }

    @Override
    public void deleteByUserId(String id) {
        userRepository.deleteById(id);
    }

    @Override
    public boolean changeEmail(String username, String email) {
        Optional<User> userOptional = userRepository.findByUsername(username);
        if(userOptional.isPresent()){
            User user = userOptional.get();
            user.setEmail(email);
            userRepository.save(user);
            updateTimelinesUser(user);
            return true;
        }
        return false;
    }

    @Override
    public boolean changeFullName(String username, String fullName) {
        Optional<User> userOptional = userRepository.findByUsername(username);
        if(userOptional.isPresent()){
            User user = userOptional.get();
            user.setFullName(fullName);
            userRepository.save(user);
            updateTimelinesUser(user);
            return true;
        }
        return false;
    }

    private void updateTimelinesUser(User user){
        List<Timeline> timelines = timelineService.getUserTimelines(user.getUsername());
        for (Timeline timeline : timelines){
            timeline.setUser(user);
            timelineRepository.save(timeline);
        }
    }

}
