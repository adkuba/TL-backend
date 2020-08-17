package com.tl.backend.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Date;

@Data
@NoArgsConstructor
public class PasswordResetToken {

    private String token;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate expiryDate = LocalDate.now().plusDays(1);

}
