package com.tl.backend.request;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PasswordResetRequest {

    private String oldPassword;
    private String newPassword;
    private String token;
}
