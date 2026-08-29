package com.laptophub.auth.service;

public interface EmailVerificationService {
    void verify(String rawToken);
    void resend(String rawEmail);
}
