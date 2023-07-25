package io.github.wmartinmimi.qchat4j.api;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;

import java.time.Instant;

@Entity
@RegisterForReflection
public class Message extends PanacheEntity {

    @GeneratedValue
    public long id;

    public String username;
    public String message;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "UTC")
    public Instant instant;

    public Message() {}

    public Message(String username, String message, Instant instant) {
        this.username = username;
        this.message = message;
        this.instant = instant;

        this.persist();
    }
}
