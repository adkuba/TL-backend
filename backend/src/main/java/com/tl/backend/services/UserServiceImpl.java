package com.tl.backend.services;

import com.stripe.exception.CardException;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.PaymentMethod;
import com.stripe.model.Subscription;
import com.stripe.param.PaymentMethodAttachParams;
import com.tl.backend.models.InteractionEvent;
import com.tl.backend.models.Timeline;
import com.tl.backend.models.User;
import com.tl.backend.repositories.TimelineRepository;
import com.tl.backend.repositories.UserRepository;
import com.tl.backend.request.SubscriptionRequest;
import com.tl.backend.response.SubscriptionResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final TimelineService timelineService;
    private final TimelineRepository timelineRepository;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder encoder;

    @Autowired
    public UserServiceImpl(PasswordEncoder passwordEncoder, UserRepository userRepository, TimelineService timelineService, TimelineRepository timelineRepository, AuthenticationManager authenticationManager){
        this.userRepository = userRepository;
        this.timelineService = timelineService;
        this.timelineRepository = timelineRepository;
        this.authenticationManager = authenticationManager;
        this.encoder = passwordEncoder;
    }

    @Override
    public List<InteractionEvent> followUser(String username, String followerUsername) {
        Optional<User> optionalUser = userRepository.findByUsername(username);
        Optional<User> optionalFollowerUser = userRepository.findByUsername(followerUsername);
        if (optionalUser.isPresent() && optionalFollowerUser.isPresent()){
            User user = optionalUser.get();
            User follower = optionalFollowerUser.get();
            for (InteractionEvent interactionEvent : user.getFollowers()) {
                //already following need to unfollow
                if (interactionEvent.getUserId().equals(followerUsername)){
                    //deleting from user
                    List<InteractionEvent> followers = user.getFollowers();
                    followers.remove(interactionEvent);
                    user.setFollowers(followers);
                    userRepository.save(user);
                    //deleting from follower
                    followers = follower.getFollowers();
                    InteractionEvent event = new InteractionEvent();
                    event.setFollow(username);
                    event.setDate(interactionEvent.getDate());
                    followers.remove(event);
                    follower.setFollowers(followers);
                    userRepository.save(follower);
                    return followers;
                }
            }
            //need to follow
            //user
            List<InteractionEvent> followers = user.getFollowers();
            InteractionEvent event = new InteractionEvent();
            event.setUserId(followerUsername);
            followers.add(event);
            user.setFollowers(followers);
            userRepository.save(user);
            //follower
            followers = follower.getFollowers();
            event = new InteractionEvent();
            event.setFollow(username);
            followers.add(event);
            follower.setFollowers(followers);
            userRepository.save(follower);
            return followers;
        }
        return null;
    }

    @Override
    public void deleteByUserId(String id) {
        userRepository.deleteById(id);
    }

    @Override
    public User checkUser(String username) {
        Optional<User> optionalUser = userRepository.findByUsername(username);
        return optionalUser.orElse(null);
    }

    @Override
    public boolean changeEmail(String username, String email) {
        Optional<User> userOptional = userRepository.findByUsername(username);
        if(userOptional.isPresent()){
            User user = userOptional.get();
            user.setEmail(email);
            userRepository.save(user);
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
            return true;
        }
        return false;
    }

    @Override
    public boolean changePassword(String username, String oldPassword, String newPassword) {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, oldPassword));
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Optional<User> optionalUser = userRepository.findUserByEmail(userDetails.getEmail());

        if(optionalUser.isPresent()){
            User user = optionalUser.get();
            user.setPassword(encoder.encode(newPassword));
            userRepository.save(user);
            return true;
        }
        return false;
    }

    @Override
    public ResponseEntity<?> createSubscription(SubscriptionRequest subscriptionRequest) throws StripeException {
        Optional<User> optionalUser = userRepository.findByUsername(subscriptionRequest.getUsername());
        if (optionalUser.isPresent()){
            User user = optionalUser.get();
            Customer customer = Customer.retrieve(user.getStripeID());
            try {
                PaymentMethod pm = PaymentMethod.retrieve(subscriptionRequest.getPaymentMethodId());
                pm.attach(PaymentMethodAttachParams.builder().setCustomer(customer.getId()).build());

            } catch (CardException e){
                return new ResponseEntity<>(e.getLocalizedMessage(), HttpStatus.BAD_REQUEST);
            }

            Map<String, Object> customerParams = new HashMap<String, Object>();
            Map<String, String> invoiceSettings = new HashMap<String, String>();
            invoiceSettings.put("default_payment_method", subscriptionRequest.getPaymentMethodId());
            customerParams.put("invoice_settings", invoiceSettings);
            customer.update(customerParams);

            Map<String, Object> item = new HashMap<>();
            item.put("price", "price_1GvluCG6mQST9KMbBo0u3t74");
            Map<String, Object> items = new HashMap<>();
            items.put("0", item);
            Map<String, Object> params = new HashMap<>();
            params.put("customer", user.getStripeID());
            params.put("items", items);

            List<String> expandList = new ArrayList<>();
            expandList.add("latest_invoice.payment_intent");
            params.put("expand", expandList);

            Subscription subscription = Subscription.create(params);
            user.setSubscriptionID(subscription.getId());
            userRepository.save(user);

            return new ResponseEntity<>(subscription.toJson(), HttpStatus.OK);
        }
        return new ResponseEntity<>("Can't find user", HttpStatus.BAD_REQUEST);
    }

    @Override
    public SubscriptionResponse getSubscription(String username) throws StripeException {
        Optional<User> optionalUser = userRepository.findByUsername(username);
        if (optionalUser.isPresent()){
            User user = optionalUser.get();
            Subscription subscription = Subscription.retrieve(user.getSubscriptionID());
            SubscriptionResponse subscriptionResponse = new SubscriptionResponse();
            subscriptionResponse.setStatus(subscription.getStatus());
            return subscriptionResponse;
        }
        return null;
    }

    @Override
    public boolean cancelSubscription(String username) throws StripeException {
        Optional<User> optionalUser = userRepository.findByUsername(username);
        if (optionalUser.isPresent()){
            User user = optionalUser.get();
            Subscription subscription = Subscription.retrieve(user.getSubscriptionID());
            Subscription deletedSubscription = subscription.cancel();
            user.setSubscriptionID("");
            userRepository.save(user);
            return true;
        }
        return false;
    }

}
