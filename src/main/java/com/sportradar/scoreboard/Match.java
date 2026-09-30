package com.sportradar.scoreboard;

import java.time.Instant;
import java.util.Objects;

/**
 * Immutable snapshot of a match in progress.
 */
public record Match(
        MatchId id,
        String homeTeam,
        String awayTeam,
        int homeScore,
        int awayScore,
        long startOrder,
        Instant startedAt) {

    public Match {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(homeTeam, "homeTeam must not be null");
        Objects.requireNonNull(awayTeam, "awayTeam must not be null");
        Objects.requireNonNull(startedAt, "startedAt must not be null");
        if (homeScore < 0 || awayScore < 0) {
            throw new InvalidScoreException("Scores must be non-negative");
        }
    }

    public int totalScore() {
        return homeScore + awayScore;
    }
}
