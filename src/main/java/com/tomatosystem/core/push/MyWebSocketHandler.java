package com.tomatosystem.core.push;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.cleopatra.json.JSONObject;
import com.tomatosystem.core.util.StringUtil;

@Component
public class MyWebSocketHandler extends TextWebSocketHandler {

    private static final Map<String, WebSocketSession> activeSessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String queryString = session.getUri().getQuery();
        System.out.println("WebSocket Connected: " + session.getAttributes());
        if (queryString != null && queryString.startsWith("userId=")) {
            String userId = queryString.split("=")[1];
            activeSessions.put(userId, session);
            session.getAttributes().put("userId", userId);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        JSONObject json = new JSONObject(payload);
        String receiverId = StringUtil.nullToEmpty(json.getString("receiver"));

        WebSocketSession receiverSession = activeSessions.get(receiverId);
        if (receiverSession != null && receiverSession.isOpen()) {
            try {
                receiverSession.sendMessage(new TextMessage(payload));
            } catch (IOException e) {
                // 예외 처리 로직 필요 시 작성
            }
        }
    }

    public static WebSocketSession getSessionByUserId(String userId) {
        return activeSessions.get(userId);
    }

    public static Map<String, WebSocketSession> getActiveSessions() {
        return activeSessions;
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String userId = (String) session.getAttributes().get("userId");
        if (userId != null) {
            activeSessions.remove(userId);
        }
    }
}