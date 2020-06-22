package com.tl.backend.models;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
public class InteractionEvent {
    private String userId;
    private LocalDate date = LocalDate.now();
    private String follow;
    private String like;
}
