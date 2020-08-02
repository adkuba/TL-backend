package com.tl.backend.models;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
public class NotificationMessage {

    private String username;
    private String text;
    private LocalDate date = LocalDate.now();
}
