package com.sportradar.scoreboard;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Default thread-safe in-memory {@link Scoreboard} implementation.
 */
public class InMemoryScoreboard implements Scoreboard {

    private static final Comparator<Match> SUMMARY_ORDER = Comparator
            .comparingInt(Match::totalScore).reversed()
            .thenComparing(Comparator.comparingLong(Match::startOrder).reversed())
            .thenComparing(m -> m.id().value().toString());

    private final ConcurrentMap<MatchId, LiveEntry> matches = new ConcurrentHashMap<>();
    private final AtomicLong startSequence = new AtomicLong(0);
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final Clock clock;

    public InMemoryScoreboard() {
        this(Clock.systemUTC());
    }

    InMemoryScoreboard(Clock clock) {
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
    }

    @Override
    public MatchId startMatch(String homeTeam, String awayTeam) {
        String home = normalizeTeam(homeTeam, "homeTeam");
        String away = normalizeTeam(awayTeam, "awayTeam");
        if (home.equalsIgnoreCase(away)) {
            throw new ScoreboardException("homeTeam and awayTeam must be different");
        }

        lock.writeLock().lock();
        try {
            ensureTeamNotBusy(home);
            ensureTeamNotBusy(away);
            MatchId id = MatchId.random();
            long order = startSequence.incrementAndGet();
            matches.put(id, new LiveEntry(home, away, 0, 0, order, clock.instant()));
            return id;
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void updateScore(MatchId matchId, int homeScore, int awayScore) {
        Objects.requireNonNull(matchId, "matchId must not be null");
        if (homeScore < 0 || awayScore < 0) {
            throw new InvalidScoreException("Scores must be non-negative");
        }
        LiveEntry entry = matches.get(matchId);
        if (entry == null) {
            throw new MatchNotFoundException("No live match: " + matchId);
        }
        synchronized (entry) {
            // Re-check presence so update-after-finish cannot slip through.
            if (!matches.containsKey(matchId)) {
                throw new MatchNotFoundException("No live match: " + matchId);
            }
            entry.homeScore = homeScore;
            entry.awayScore = awayScore;
        }
    }

    @Override
    public void finishMatch(MatchId matchId) {
        Objects.requireNonNull(matchId, "matchId must not be null");
        lock.writeLock().lock();
        try {
            LiveEntry removed = matches.remove(matchId);
            if (removed == null) {
                throw new MatchNotFoundException("No live match: " + matchId);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public List<Match> getSummary() {
        lock.readLock().lock();
        try {
            List<Match> snapshot = snapshotLocked();
            snapshot.sort(SUMMARY_ORDER);
            return List.copyOf(snapshot);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public List<Match> findMatchesByTeam(String team) {
        if (team == null || team.isBlank()) {
            throw new ScoreboardException("team must be non-blank");
        }
        String wanted = team.strip();
        lock.readLock().lock();
        try {
            List<Match> snapshot = snapshotLocked();
            List<Match> filtered = new ArrayList<>();
            for (Match match : snapshot) {
                if (match.homeTeam().equalsIgnoreCase(wanted)
                        || match.awayTeam().equalsIgnoreCase(wanted)) {
                    filtered.add(match);
                }
            }
            filtered.sort(SUMMARY_ORDER);
            return List.copyOf(filtered);
        } finally {
            lock.readLock().unlock();
        }
    }

    private List<Match> snapshotLocked() {
        List<Match> snapshot = new ArrayList<>(matches.size());
        for (var e : matches.entrySet()) {
            LiveEntry live = e.getValue();
            int homeScore;
            int awayScore;
            synchronized (live) {
                homeScore = live.homeScore;
                awayScore = live.awayScore;
            }
            snapshot.add(new Match(
                    e.getKey(), live.homeTeam, live.awayTeam,
                    homeScore, awayScore, live.startOrder, live.startedAt));
        }
        return snapshot;
    }

    private void ensureTeamNotBusy(String team) {
        for (LiveEntry entry : matches.values()) {
            if (entry.homeTeam.equalsIgnoreCase(team) || entry.awayTeam.equalsIgnoreCase(team)) {
                throw new DuplicateMatchException("Team already plays a live match: " + team);
            }
        }
    }

    private static String normalizeTeam(String team, String field) {
        if (team == null || team.isBlank()) {
            throw new ScoreboardException(field + " must be non-blank");
        }
        return team.strip();
    }

    private static final class LiveEntry {
        final String homeTeam;
        final String awayTeam;
        final long startOrder;
        final Instant startedAt;
        int homeScore;
        int awayScore;

        LiveEntry(String homeTeam, String awayTeam, int homeScore, int awayScore,
                  long startOrder, Instant startedAt) {
            this.homeTeam = homeTeam;
            this.awayTeam = awayTeam;
            this.homeScore = homeScore;
            this.awayScore = awayScore;
            this.startOrder = startOrder;
            this.startedAt = startedAt;
        }
    }
}
