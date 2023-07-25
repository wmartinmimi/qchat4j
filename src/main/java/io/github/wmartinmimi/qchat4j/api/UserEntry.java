package io.github.wmartinmimi.qchat4j.api;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;

@Entity
public class UserEntry extends PanacheEntity {
    public String username;
    public String passwordHash;

    public static boolean isUniqueUsername(String username) {
        return UserEntry.count("username", username) == 0;
    }

    public static String username(UserEntry entry) {
        return entry.username;
    }
}
