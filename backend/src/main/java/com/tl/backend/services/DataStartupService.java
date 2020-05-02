package com.tl.backend.services;

import com.tl.backend.entities.User;
import com.tl.backend.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
public class DataStartupService {

    private final UserRepository userRepository;

    @Autowired
    public DataStartupService(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    private void createMe(){
        User kuba = new User();
        kuba.setEmail("akuba@exemplum.pl");
        kuba.setUsername("akuba");
        kuba.setPassword("funia");
        userRepository.save(kuba);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void createStartupData(){
        createMe();
    }
}
