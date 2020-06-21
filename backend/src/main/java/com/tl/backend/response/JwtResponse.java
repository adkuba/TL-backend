package com.tl.backend.response;

import lombok.Data;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Data
public class JwtResponse {
    private String token;
    private String type = "Bearer";
    private String username;
    private String email;
    private String fullName;
    private List<String> roles;
    private Date creationTime;
    private LocalDate userDate;
    private List<String> likes;

    public JwtResponse(LocalDate userDate, List<String> likes, String accessToken, Date creationTime, String username, String email, List<String> roles, String fullName) {
        this.token = accessToken;
        this.creationTime = creationTime;
        this.userDate = userDate;
        this.username = username;
        this.email = email;
        this.roles = roles;
        this.fullName = fullName;
        this.likes = likes;
    }
}