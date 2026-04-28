package org.example.springboot2.websocket;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import org.example.springboot2.user.entity.User;
import org.example.springboot2.user.service.UserService;
import org.example.springboot2.config.ApplicationContextProvider;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

@ServerEndpoint("/ws/{token}")
@Component
public class WebSocketServer {

    private static final Set<Session> sessions = new CopyOnWriteArraySet<>();

    // 存储 token → userId 映射（简单实现）
    private static final ConcurrentHashMap<Session, Long> sessionUserMap = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session, @PathParam("token") String token) {
        UserService userService = ApplicationContextProvider.getBean(UserService.class);
        if (userService != null) {
            User user = userService.getUserByToken(token);
            if (user != null) {
                sessionUserMap.put(session, user.getId());
                sessions.add(session);
                System.out.println("用户 " + user.getId() + " 已连接");
                return;
            }
        }
        // 验证失败则直接关闭
        try { session.close(); } catch (IOException e) { e.printStackTrace(); }
    }

    @OnClose
    public void onClose(Session session) {
        sessions.remove(session);
        sessionUserMap.remove(session);
    }

    @OnError
    public void onError(Session session, Throwable error) {
        sessions.remove(session);
        sessionUserMap.remove(session);
    }

    // 向所有在线用户广播
    public static void broadcast(String message) {
        for (Session session : sessions) {
            try {
                session.getBasicRemote().sendText(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // 按 userId 发送
    public static void sendToUser(Long userId, String message) {
        sessionUserMap.forEach((session, uid) -> {
            if (uid.equals(userId) && session.isOpen()) {
                try {
                    session.getBasicRemote().sendText(message);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}