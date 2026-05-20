package com.suitexen.ecokeep.models;

import java.util.List;
import java.util.Map;

public class GroqChatRequest {
    private String model;
    private List<Message> messages;
    private Map<String, String> response_format;
    private double temperature;

    public GroqChatRequest(String model, List<Message> messages, Map<String, String> responseFormat, double temperature) {
        this.model = model;
        this.messages = messages;
        this.response_format = responseFormat;
        this.temperature = temperature;
    }

    public static class Message {
        private String role;
        private String content;

        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }
    }
}