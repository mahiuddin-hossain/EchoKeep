package com.suitexen.ecokeep.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class GroqChatResponse {
    private List<Choice> choices;

    public List<Choice> getChoices() { return choices; }

    public static class Choice {
        private Message message;

        public Message getMessage() { return message; }
    }

    public static class Message {
        private String content;

        public String getContent() { return content; }
    }
}