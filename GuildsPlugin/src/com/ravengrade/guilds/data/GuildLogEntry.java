package com.ravengrade.guilds.data;

import java.time.LocalDateTime;

public class GuildLogEntry {

    private final String message;
    private final LocalDateTime time;

    public GuildLogEntry(String message) {
        this.message = message;
        this.time = LocalDateTime.now();
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public String format() {
        return "[" + time + "] " + message;
    }
}
