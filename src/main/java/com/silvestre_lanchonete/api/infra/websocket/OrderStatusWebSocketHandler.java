package com.silvestre_lanchonete.api.infra.websocket;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class OrderStatusWebSocketHandler extends TextWebSocketHandler {

    private static final Map<String, WebSocketSession> userSessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {

        HttpSession httpSession = (HttpSession) session.getAttributes().get("HTTP_SESSION");

        if (httpSession == null) {
            System.out.println("⚠ Erro: Sessão HTTP não encontrada!");
            try {
                session.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }

        String userEmail = (String) httpSession.getAttribute("userEmail");

        if (userEmail != null) {
            userSessions.put(userEmail, session);
            System.out.println("✅ Usuário conectado ao WebSocket: " + userEmail);
        } else {
            System.out.println("⚠ Erro: Email do usuário não encontrado na sessão!");
            try {
                session.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        userSessions.values().remove(session);
    }

    public void sendNotification(String userEmail, String message) {
        WebSocketSession session = userSessions.get(userEmail);
        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(message));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
