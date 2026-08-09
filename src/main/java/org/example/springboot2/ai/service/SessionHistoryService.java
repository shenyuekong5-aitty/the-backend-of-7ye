package org.example.springboot2.ai.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SessionHistoryService {

    private final Map<String, List<Message>> sessions = new ConcurrentHashMap<>();

    @Value("${ai.max-history-messages:10}")
    private int maxHistoryMessages;

    public String createSessionId() {
        return UUID.randomUUID().toString();
    }

    public void saveHistory(String sessionId, String userQuestion, String assistantAnswer) {
        List<Message> history = sessions.computeIfAbsent(sessionId, k -> new ArrayList<>());
        history.add(new Message("user", userQuestion));
        history.add(new Message("assistant", assistantAnswer));
        trim(history);
    }

    public List<Message> getHistory(String sessionId) {
        if (sessionId == null) return List.of();
        List<Message> history = sessions.get(sessionId);
        if (history == null) return List.of();
        return new ArrayList<>(history);
    }

    private void trim(List<Message> history) {
        int maxMessages = Math.max(maxHistoryMessages * 2, 2);
        while (history.size() > maxMessages) {
            history.remove(0);
        }
    }

    public static class Message {
        private final String role;
        private final String content;

        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }

        public String getRole() {
            return role;
        }

        public String getContent() {
            return content;
        }
    }
}