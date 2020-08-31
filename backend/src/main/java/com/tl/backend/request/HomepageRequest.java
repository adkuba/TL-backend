package com.tl.backend.request;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class HomepageRequest {
    private List<String> timelinesIDS;
    private String username;
}
