package com.tl.backend.response;

import com.tl.backend.models.InteractionEvent;
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
}
