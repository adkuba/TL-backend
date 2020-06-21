package com.tl.backend.controllers;

import com.stripe.exception.StripeException;
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

    @Autowired
    public UserController(UserService userService){
        this.userService = userService;
    }

    @DeleteMapping(value = "/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable String id){
        userService.deleteByUserId(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping(value = "/email")
    public ResponseEntity<?> changeEmail(Authentication authentication, @RequestParam String email){
        if (userService.changeEmail(authentication.getName(), email)){
            return new ResponseEntity<>(HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    @PostMapping(value = "/create-subscription")
    public ResponseEntity<?> createSubscription(Authentication authentication, @RequestBody SubscriptionRequest subscriptionRequest) throws StripeException {
        subscriptionRequest.setUsername(authentication.getName());
        return userService.createSubscription(subscriptionRequest);
    }

    @GetMapping(value = "/get-subscription")
    public ResponseEntity<?> getSubscription(Authentication authentication) throws StripeException {
        return new ResponseEntity<>(userService.getSubscription(authentication.getName()) , HttpStatus.OK);
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
}
