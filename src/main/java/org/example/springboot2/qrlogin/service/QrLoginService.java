package org.example.springboot2.qrlogin.service;

import org.example.springboot2.qrlogin.entity.QrSession;
import org.example.springboot2.user.entity.User;
import org.example.springboot2.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class QrLoginService {

    @Autowired
    private UserRepository userRepository;

    // 会话存储
    private final ConcurrentHashMap<String, QrSession> sessionMap = new ConcurrentHashMap<>();

    // 临时 token 存储：key=pcToken, value=userId
    private final ConcurrentHashMap<String, Long> temporaryTokens = new ConcurrentHashMap<>();

    /**
     * 生成新的二维码会话
     */
    public String generateSession() {
        String sessionId = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        QrSession session = new QrSession();
        session.setSessionId(sessionId);
        session.setStatus("WAITING");
        session.setCreateTime(LocalDateTime.now());
        sessionMap.put(sessionId, session);
        return sessionId;
    }

    /**
     * 手机端确认登录（不再覆盖用户 token）
     */
    public void confirmSession(String sessionId, Long userId) {
        QrSession session = sessionMap.get(sessionId);
        if (session != null && !session.isExpired() && "WAITING".equals(session.getStatus())) {
            session.setStatus("CONFIRMED");
            session.setUserId(userId);

            // 直接使用用户当前有效的 token（手机端 token）
            User user = userRepository.findById(userId).orElse(null);
            if (user != null && user.getToken() != null) {
                session.setPcToken(user.getToken());   // 复用已有 token
            } else {
                // 兜底：用户没有 token 时生成新的（极少情况）
                String pcToken = UUID.randomUUID().toString();
                session.setPcToken(pcToken);
                if (user != null) {
                    user.setToken(pcToken);
                    userRepository.save(user);
                }
            }
        }
    }

    /**
     * PC 端轮询状态（同时检查过期）
     */
    public QrSession getSession(String sessionId) {
        QrSession session = sessionMap.get(sessionId);
        if (session != null && session.isExpired()) {
            session.setStatus("EXPIRED");
        }
        return session;
    }

    /**
     * 获取已确认的 PC 端 token（一次性使用）
     */
    public String getConfirmedToken(String sessionId) {
        QrSession session = sessionMap.get(sessionId);
        if (session != null && "CONFIRMED".equals(session.getStatus())) {
            String token = session.getPcToken();
            sessionMap.remove(sessionId);
            return token;
        }
        return null;
    }

    /**
     * 根据临时 token 查询用户 ID
     */
    public Long getUserIdByTemporaryToken(String token) {
        return temporaryTokens.get(token);
    }

    /**
     * 清除临时 token
     */
    public void removeTemporaryToken(String token) {
        temporaryTokens.remove(token);
    }

    /**
     * 定时清理过期会话，每 5 分钟执行一次
     */
    @Scheduled(fixedRate = 300000)
    public void cleanExpiredSessions() {
        sessionMap.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }
}