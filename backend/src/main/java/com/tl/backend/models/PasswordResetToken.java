package com.tl.backend.models;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Date;

@Data
@NoArgsConstructor
public class PasswordResetToken {

    private String token;
    private LocalDate expiryDate = LocalDate.now().plusDays(1);

}
