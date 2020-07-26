package com.tl.backend.response;

import com.tl.backend.models.InteractionEvent;
import com.tl.backend.models.Role;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
public class UserResponse {
    private String username;
    private String email;
    private String fullName;
    private LocalDate creationTime;
    private List<InteractionEvent> followers;
    private List<String> roles;
    private List<String> likes;
    private String subscriptionEnd;
    private String subscriptionID;
    private String card;
    private Boolean blocked;
    private Long profileViewsNumber;
}
