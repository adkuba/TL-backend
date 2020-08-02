package com.tl.backend.controllers;

import com.stripe.exception.StripeException;
import com.tl.backend.mappers.UserMapper;
import com.tl.backend.models.Notification;
import com.tl.backend.models.User;
import com.tl.backend.request.PasswordResetRequest;
import com.tl.backend.request.SubscriptionRequest;
import com.tl.backend.services.NotificationServiceImpl;
import com.tl.backend.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Optional;

@RestController
@RequestMapping(value = "api/users", method = {RequestMethod.POST, RequestMethod.DELETE})
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;
    private final NotificationServiceImpl notificationService;

    @Autowired
    public UserController(NotificationServiceImpl notificationService, UserMapper userMapper, UserService userService){
        this.userMapper = userMapper;
        this.notificationService = notificationService;
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
    public ResponseEntity<?> changePassword(Authentication authentication, @RequestBody @Valid PasswordResetRequest passwordResetRequest){
        if (userService.changePassword(authentication.getName(), passwordResetRequest.getOldPassword(), passwordResetRequest.getNewPassword())){
            return new ResponseEntity<>(HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    @GetMapping(value = "/check-subscription")
    public ResponseEntity<?> checkSubscription(Authentication authentication) throws StripeException {
        userService.checkSubscription(authentication.getName());
        return null;
    }

    @GetMapping(value = "/notifications")
    public ResponseEntity<?> getNotifications(Authentication authentication){
        Notification notification = notificationService.getNotification(authentication.getName());
        notificationService.markRead(authentication.getName());
        return new ResponseEntity<>(notification, HttpStatus.OK);
    }

    @PostMapping(value = "/block")
    public ResponseEntity<?> blockUser(@RequestParam String username, @RequestParam String reason){
        userService.blockUser(username, reason);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping(value = "/unblock")
    public ResponseEntity<?> unBlockUser(@RequestParam String username){
        userService.unBlockUser(username);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping(value = "/public/{username}")
    public ResponseEntity<?> checkUser(HttpServletRequest request, @PathVariable String username, @RequestParam(required = false) Boolean profile){
        User user = userService.checkUser(username);
        if (user == null){
            return new ResponseEntity<>(null, HttpStatus.OK);
        } else {
            //profile view
            if (profile != null){
                if (profile){
                    userService.profileView(username, request);
                }
            }
            return new ResponseEntity<>(userMapper.userResponse(user), HttpStatus.OK);
        }
    }

    @PostMapping(value = "/follow/{username}")
    public ResponseEntity<?> followUser(Authentication authentication, @PathVariable String username){
        return new ResponseEntity<>(userService.followUser(username, authentication.getName()), HttpStatus.OK);
    }
}
