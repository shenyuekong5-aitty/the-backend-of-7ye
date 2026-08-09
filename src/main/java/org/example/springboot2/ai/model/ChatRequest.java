package org.example.springboot2.ai.model;

/**
 * 前端发送的聊天请求
 */
public class ChatRequest {

    private String question;
    private String sessionId;

    public ChatRequest() {
    }

    public ChatRequest(String question, String sessionId) {
        this.question = question;
        this.sessionId = sessionId;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
}
