package com.tl.backend.services;

public interface CaptchaService {
    boolean processResponse(String recaptchaToken);
}
