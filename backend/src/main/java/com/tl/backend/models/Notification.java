package com.tl.backend.models;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class Notification {

    private Boolean read = false;
    private List<NotificationMessage> messages = new ArrayList<>();
}
