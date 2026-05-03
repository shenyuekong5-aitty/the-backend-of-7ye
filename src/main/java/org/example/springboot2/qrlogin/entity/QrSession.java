package org.example.springboot2.qrlogin.entity;

import java.time.LocalDateTime;

public class QrSession {

    private String sessionId;
    private String status;          // WAITING / CONFIRMED / EXPIRED
    private Long userId;            // 确认后绑定的用户ID
    private String pcToken;         // PC 端最终拿到的 token
    private LocalDateTime createTime;

    // 过期时间（分钟）
    public static final int EXPIRE_MINUTES = 5;

    /**
     * 判断会话是否已过期
     */
    public boolean isExpired() {
        return createTime != null
                && LocalDateTime.now().isAfter(createTime.plusMinutes(EXPIRE_MINUTES));
    }

    // ========== Getter / Setter ==========
    public String getSessionId() {
        return sessionId;
    }
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public Long getUserId() {
        return userId;
    }
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    public String getPcToken() {
        return pcToken;
    }
    public void setPcToken(String pcToken) {
        this.pcToken = pcToken;
    }
    public LocalDateTime getCreateTime() {
        return createTime;
    }
    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}