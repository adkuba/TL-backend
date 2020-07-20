package com.tl.backend.response;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
public class StatResponse {

    private LocalDate date;
    private String location;
    private Long number;
}
