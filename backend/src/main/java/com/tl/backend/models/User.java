package com.tl.backend.models;


import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.*;


@Document(collection = "users")
@Data
@NoArgsConstructor
public class User {

    @Id
    private String id;
    private String username;
    private String fullName = "";
    private List<InteractionEvent> followers = new ArrayList<>();
    private String subscriptionID;
    private Boolean blocked = false;
    private LocalDate creationTime = LocalDate.now();
    @JsonIgnore
    private String refreshToken;
    private String email;
    private List<String> likes = new ArrayList<>();
    @NotNull
    @JsonIgnore
    private String password;
    @DBRef
    private Set<Role> roles = new HashSet<>();
    private String stripeID;
    private LocalDate subscriptionEnd;
    private String card;
    private List<InteractionEvent> myViews = new ArrayList<>();

    public User(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }
}
