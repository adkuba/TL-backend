package com.tl.backend.services;

import com.stripe.exception.StripeException;
import com.stripe.model.Subscription;
import com.tl.backend.models.User;
import com.tl.backend.request.SubscriptionRequest;
import com.tl.backend.response.SubscriptionResponse;
import org.springframework.http.ResponseEntity;

public interface UserService {

    void deleteByUserId(String id);

    boolean changeEmail(String username, String email);

    boolean changeFullName(String username, String fullName);

    boolean changePassword(String username, String oldPassword, String newPassword);

    ResponseEntity<?> createSubscription(SubscriptionRequest subscriptionRequest) throws StripeException;

    SubscriptionResponse getSubscription(String username) throws StripeException;

    boolean cancelSubscription(String username) throws StripeException;
}
