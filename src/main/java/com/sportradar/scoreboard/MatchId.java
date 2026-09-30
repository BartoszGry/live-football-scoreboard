package com.sportradar.scoreboard;

import java.util.Objects;
import java.util.UUID;

/**
 * Unique identifier of a match on the scoreboard.
 */
public record MatchId(UUID value) {

    public MatchId {
        Objects.requireNonNull(value, "value must not be null");
    }

    public static MatchId random() {
        return new MatchId(UUID.randomUUID());
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
