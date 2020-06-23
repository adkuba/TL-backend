package com.tl.backend.response;

import lombok.Data;
import java.util.Date;

@Data
public class JwtResponse {
    private String token;
    private Date creationTime;
    private UserResponse user;

    public JwtResponse(String accessToken, Date creationTime, UserResponse userResponse) {
        this.token = accessToken;
        this.creationTime = creationTime;
        this.user = userResponse;
    }
}