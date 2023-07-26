package io.github.wmartinmimi.qchat4j.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.elytron.security.common.BcryptUtil;
import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.transaction.Transactional;
import jakarta.websocket.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

public class ChatSession {

    private static final Logger logger = LoggerFactory.getLogger(ChatSession.class);

    private final ConcurrentHashMap<Session, UserEntry> users;
    private final ObjectMapper mapper;
    private final String sessionId;

    public ChatSession(String sessionId, ObjectMapper mapper) {
        users = new ConcurrentHashMap<>(2);

        this.sessionId = sessionId;
        this.mapper = mapper;
    }

    void join(Session session) {
        users.putIfAbsent(session, new UserEntry());
    }

    @Transactional
    void message(Session session, String message) {
        var data = message.split("\n", 2);
        switch (data[0]) {
            case "__join_as__" -> {
                try {
                    var joinAs = mapper.readValue(data[1], JoinAs.class);
                    UserEntry user = UserEntry.find("username", joinAs.username).firstResult();
                    if (user == null) {
                        logger.info("User {} not found", joinAs.username);
                        session.getAsyncRemote().sendText("not found");
                        return;
                    }
                    if (BcryptUtil.matches(joinAs.password, user.passwordHash)) {
                        logger.info("User {} joined", joinAs.username);
                        users.replace(session, user);
                        session.getAsyncRemote().sendText("success");
                        broadcast(new Message("System", MessageFormat.format("User {0} has joined", joinAs.username), sessionId, Instant.now()));
                    } else {
                        logger.info("User {} entered incorrect password", joinAs.username);
                        session.getAsyncRemote().sendText("invalid password");
                    }
                } catch (JsonProcessingException e) {
                    logger.error("invalid user join request: {}", data[1], e);
                }
            }
            case "__get_all__" -> {
                try {
                    var messages = Message.listAllFromSession(sessionId);
                    for (var message1 : messages) {
                        session.getAsyncRemote().sendText(mapper.writeValueAsString(message1));
                    }
                } catch (JsonProcessingException e) {
                    logger.error("Message parsing error, message: {}", message, e);
                }
            }
            case "__message__" -> {
                var instant = Instant.now();
                try {
                    broadcast(new Message(users.get(session).username,  data[1], sessionId, instant));
                } catch (Exception e) {
                    logger.error("Transaction failed", e);
                }
            }
            default -> {
                logger.error("invalid message received: {}", message);
            }
        }
    }

    @RegisterForReflection
    record JoinAs(String username, String password) {
    }

    @Transactional
    void leave(Session session) {
        var user = users.get(session);
        if (user != null) {
            broadcast(new Message("System", MessageFormat.format("User {0} has left", user.username), sessionId, Instant.now()));
        }
        users.remove(session);
    }

    private void broadcast(Message message) {
        try {
            String data = mapper.writeValueAsString(message);
            users.keySet().forEach(session -> session.getAsyncRemote().sendText(data));
        } catch (JsonProcessingException e) {
            logger.error("message parsing error", e);
        }
    }
}
