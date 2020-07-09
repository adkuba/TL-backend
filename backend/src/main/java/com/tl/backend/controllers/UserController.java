package com.tl.backend.controllers;

import com.stripe.exception.StripeException;
import com.tl.backend.mappers.UserMapper;
import com.tl.backend.models.User;
import com.tl.backend.request.SubscriptionRequest;
import com.tl.backend.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Optional;

@RestController
@RequestMapping(value = "api/users", method = {RequestMethod.POST, RequestMethod.DELETE})
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;

    @Autowired
    public UserController(UserMapper userMapper, UserService userService){
        this.userMapper = userMapper;
        this.userService = userService;
    }

    @DeleteMapping(value = "/{username}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable String username){
        userService.deleteByUsername(username);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping(value = "/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllUsers(){
        return new ResponseEntity<>(userMapper.usersResponse(userService.getAllUsers()), HttpStatus.OK);
    }

    @PutMapping(value = "/email")
    public ResponseEntity<?> changeEmail(Authentication authentication, @RequestParam String email){
        return userService.changeEmail(authentication.getName(), email);
    }

    @PostMapping(value = "/create-subscription")
    public ResponseEntity<?> createSubscription(Authentication authentication, @RequestBody SubscriptionRequest subscriptionRequest) throws StripeException {
        subscriptionRequest.setUsername(authentication.getName());
        return userService.createSubscription(subscriptionRequest);
    }

    @PostMapping(value = "/cancel-subscription")
    public ResponseEntity<?> cancelSubscription(Authentication authentication) throws StripeException {
        return new ResponseEntity<>(userService.cancelSubscription(authentication.getName()), HttpStatus.OK);
    }

    @PutMapping(value = "/name")
    public ResponseEntity<?> changeName(Authentication authentication, @RequestParam String name){
        if (userService.changeFullName(authentication.getName(), name)){
            return new ResponseEntity<>(HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    @PutMapping(value = "/password")
    public ResponseEntity<?> changePassword(Authentication authentication, @RequestParam String oldPassword, @RequestParam String newPassword){
        if (userService.changePassword(authentication.getName(), oldPassword, newPassword)){
            return new ResponseEntity<>(HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    @GetMapping(value = "/check-subscription")
    public ResponseEntity<?> checkSubscription(Authentication authentication) throws StripeException {
        userService.checkSubscription(authentication.getName());
        return null;
    }

    @PostMapping(value = "/block")
    public ResponseEntity<?> blockUser(@RequestParam String username){
        userService.blockUser(username);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping(value = "/public/{username}")
    public ResponseEntity<?> checkUser(@PathVariable String username){
        User user = userService.checkUser(username);
        if (user == null){
            return new ResponseEntity<>(null, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(userMapper.userResponse(user), HttpStatus.OK);
        }
    }

    @PostMapping(value = "/follow/{username}")
    public ResponseEntity<?> followUser(Authentication authentication, @PathVariable String username){
        return new ResponseEntity<>(userService.followUser(username, authentication.getName()), HttpStatus.OK);
    }
}
