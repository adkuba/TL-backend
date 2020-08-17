package com.tl.backend.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
public class InteractionEvent {
    private String userId;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date = LocalDate.now();
    private String follow;
    private String like;
    private String timelineId;
    private String deviceId;
}
