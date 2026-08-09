package org.example.springboot2.ai.model;

/**
 * 返回给前端的聊天响应
 */
public class ChatResponse {

    private String sessionId;
    private String answer;

    public ChatResponse() {
    }

    public ChatResponse(String sessionId, String answer) {
        this.sessionId = sessionId;
        this.answer = answer;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }
}
