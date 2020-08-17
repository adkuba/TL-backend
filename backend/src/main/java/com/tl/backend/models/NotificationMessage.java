package com.tl.backend.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
public class NotificationMessage {

    private String username;
    private String text;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date = LocalDate.now();
}
