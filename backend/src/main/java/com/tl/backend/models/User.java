package com.tl.backend.models;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
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
    private String fullName = "New User";
    private List<InteractionEvent> followers = new ArrayList<>();
    private String subscriptionID;
    private Boolean blocked = false;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate creationTime = LocalDate.now();
    private String refreshToken;
    private PasswordResetToken passwordResetToken;
    private String email;
    private List<String> likes = new ArrayList<>();

    @NotNull
    private String password;
    private Notification notification;

    @DBRef
    private Set<Role> roles = new HashSet<>();
    private String stripeID;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate subscriptionEnd;
    private String card;
    private List<InteractionEvent> myViews = new ArrayList<>();
    private Map<LocalDate, Map<String, Long>> profileViews = new HashMap<>();
    private Long profileViewsNumber = 0L;

    public void profileViews(){
        long count = 0;
        for (Map<String, Long> devicesInDay : profileViews.values()){
            for (Long views : devicesInDay.values()){
                count += views;
            }
        }
        profileViewsNumber = count;
    }

    public User(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }
}
