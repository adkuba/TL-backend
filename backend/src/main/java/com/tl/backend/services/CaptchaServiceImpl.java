package com.tl.backend.services;

import com.tl.backend.config.AppProperties;
import com.tl.backend.models.GoogleResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

@Service
public class CaptchaServiceImpl implements CaptchaService {

    private final AppProperties appProperties;
    private final RestTemplate restTemplate;

    @Autowired
    public CaptchaServiceImpl(AppProperties appProperties, RestTemplate restTemplate){
        this.appProperties = appProperties;
        this.restTemplate = restTemplate;
    }

    @Override
    public boolean processResponse(String recaptchaToken) {
        URI verifyUri = URI.create(String.format("https://www.google.com/recaptcha/api/siteverify?secret=%s&response=%s", appProperties.getGoogleRecaptcha(), recaptchaToken));
        GoogleResponse googleResponse = restTemplate.getForObject(verifyUri, GoogleResponse.class);
        assert googleResponse != null;
        return googleResponse.isSuccess();
    }
}
