package com.tl.backend.services;

import com.stripe.exception.StripeException;
import com.stripe.model.Subscription;
import com.tl.backend.models.InteractionEvent;
import com.tl.backend.models.User;
import com.tl.backend.request.SubscriptionRequest;
import com.tl.backend.response.SubscriptionResponse;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface UserService {

    List<InteractionEvent> followUser(String username, String followerUsername);

    List<User> getAllUsers();

    ResponseEntity<?> deleteByUsername(String username);

    User checkUser(String username);

    ResponseEntity<?> changeEmail(String username, String email);

    boolean changeFullName(String username, String fullName);

    boolean changePassword(String username, String oldPassword, String newPassword);

    ResponseEntity<?> createSubscription(SubscriptionRequest subscriptionRequest) throws StripeException;

    ResponseEntity<?> getSubscription(String username) throws StripeException;

    boolean cancelSubscription(String username) throws StripeException;

    List<User> getNewUsers();

    List<User> getRandomUsers();
}
