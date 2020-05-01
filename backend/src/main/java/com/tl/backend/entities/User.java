package com.tl.backend.entities;


import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;

import javax.validation.constraints.NotNull;

public class User {

    @Id
    private String id;

    //indeksowanie usprawnia wykonywanie queries
    @Indexed(unique = true)
    private String username;

    @NotNull
    private String password;

}
