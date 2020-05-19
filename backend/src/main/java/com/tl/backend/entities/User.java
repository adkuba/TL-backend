package com.tl.backend.entities;


import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;


@Document(collection = "users")
@Data
@NoArgsConstructor
public class User {

    @Id
    private String username;

    private String fullName;

    private String email;

    @NotNull
    @JsonIgnore
    private String password;

}
