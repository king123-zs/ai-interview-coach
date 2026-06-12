package com.example.aiinterviewcoach.service;

@FunctionalInterface
public interface StreamTokenHandler {

    void onToken(String token);
}
