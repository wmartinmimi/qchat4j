package io.github.wmartinmimi.qchat4j.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;

import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint("/api/chat-session/{session_id}")
@ApplicationScoped
public class ChatSessionEndpoint {

    ConcurrentHashMap<String, ChatSession> sessions;

    private static final ChatSession emptySession = new ChatSession(null);

    @Inject
    public ChatSessionEndpoint() {
        sessions = new ConcurrentHashMap<>();
    }

    @Inject
    ObjectMapper mapper;

    @OnOpen
    public void onOpen(Session session, @PathParam("session_id") String sessionId) {
        sessions.putIfAbsent(sessionId, new ChatSession(mapper));
        sessions.get(sessionId).join(session);
    }

    @OnClose
    @Transactional
    public void onClose(Session session, @PathParam("session_id") String sessionId) {
        sessions.getOrDefault(sessionId, emptySession).leave(session);
    }

    @OnError
    @Transactional
    public void onError(Session session, @PathParam("session_id") String sessionId, Throwable throwable) {
        sessions.getOrDefault(sessionId, emptySession).leave(session);
    }

    @OnMessage
    @Transactional
    public void onMessage(Session session, String message, @PathParam("session_id") String sessionId) {
        sessions.getOrDefault(sessionId, emptySession).message(session, message);
    }
}
