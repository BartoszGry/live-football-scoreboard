package com.sportradar.scoreboard;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class InMemoryScoreboardTest {

    private Scoreboard board;

    @BeforeEach
    void setUp() {
        board = new InMemoryScoreboard();
    }

    @Test
    void startMatchCreatesZeroZeroMatch() {
        MatchId id = board.startMatch("Mexico", "Canada");

        List<Match> summary = board.getSummary();
        assertEquals(1, summary.size());
        Match match = summary.get(0);
        assertEquals(id, match.id());
        assertEquals("Mexico", match.homeTeam());
        assertEquals("Canada", match.awayTeam());
        assertEquals(0, match.homeScore());
        assertEquals(0, match.awayScore());
    }

    @Test
    void startMatchRejectsInvalidTeams() {
        assertThrows(ScoreboardException.class, () -> board.startMatch("  ", "Canada"));
        assertThrows(ScoreboardException.class, () -> board.startMatch("Mexico", null));
        assertThrows(ScoreboardException.class, () -> board.startMatch("Mexico", "mexico"));
    }

    @Test
    void startMatchRejectsBusyTeam() {
        board.startMatch("Mexico", "Canada");

        assertThrows(DuplicateMatchException.class, () -> board.startMatch("Mexico", "Brazil"));
        assertThrows(DuplicateMatchException.class, () -> board.startMatch("Spain", "canada"));
    }

    @Test
    void updateScoreSetsAbsoluteScore() {
        MatchId id = board.startMatch("Mexico", "Canada");

        board.updateScore(id, 0, 5);
        board.updateScore(id, 1, 5);

        Match match = board.getSummary().get(0);
        assertEquals(1, match.homeScore());
        assertEquals(5, match.awayScore());
    }

    @Test
    void updateScoreRejectsNegativeScores() {
        MatchId id = board.startMatch("Mexico", "Canada");

        assertThrows(InvalidScoreException.class, () -> board.updateScore(id, -1, 0));
        assertThrows(InvalidScoreException.class, () -> board.updateScore(id, 0, -2));
    }

    @Test
    void updateScoreRejectsUnknownMatch() {
        assertThrows(MatchNotFoundException.class,
                () -> board.updateScore(MatchId.random(), 1, 0));
    }

    @Test
    void finishMatchRemovesItFromSummary() {
        MatchId id = board.startMatch("Mexico", "Canada");
        board.finishMatch(id);

        assertTrue(board.getSummary().isEmpty());
    }

    @Test
    void finishMatchRejectsUnknownMatch() {
        assertThrows(MatchNotFoundException.class, () -> board.finishMatch(MatchId.random()));
    }

    @Test
    void updateAfterFinishIsRejected() {
        MatchId id = board.startMatch("Mexico", "Canada");
        board.finishMatch(id);

        assertThrows(MatchNotFoundException.class, () -> board.updateScore(id, 1, 0));
    }

    @Test
    void finishedTeamsCanStartAgain() {
        MatchId first = board.startMatch("Mexico", "Canada");
        board.finishMatch(first);

        MatchId second = board.startMatch("Mexico", "Brazil");
        assertEquals(1, board.getSummary().size());
        assertEquals(second, board.getSummary().get(0).id());
    }

    @Test
    void summaryIsOrderedByTotalScoreThenMostRecentStart() {
        // Start order matters for the tie-break.
        board.startMatch("Mexico", "Canada");
        board.startMatch("Spain", "Brazil");
        board.startMatch("Germany", "France");
        board.startMatch("Uruguay", "Italy");
        board.startMatch("Argentina", "Australia");

        setScore("Mexico", 0, 5);
        setScore("Spain", 10, 2);
        setScore("Germany", 2, 2);
        setScore("Uruguay", 6, 6);
        setScore("Argentina", 3, 1);

        List<Match> summary = board.getSummary();

        assertEquals(5, summary.size());
        assertMatch(summary.get(0), "Uruguay", "Italy", 6, 6);
        assertMatch(summary.get(1), "Spain", "Brazil", 10, 2);
        assertMatch(summary.get(2), "Mexico", "Canada", 0, 5);
        assertMatch(summary.get(3), "Argentina", "Australia", 3, 1);
        assertMatch(summary.get(4), "Germany", "France", 2, 2);
    }

    @Test
    void summaryIsEmptyInitiallyAndImmutable() {
        List<Match> summary = board.getSummary();
        assertTrue(summary.isEmpty());
        assertThrows(UnsupportedOperationException.class, () -> summary.add(null));
    }

    private void setScore(String homeTeam, int homeScore, int awayScore) {
        MatchId id = board.getSummary().stream()
                .filter(m -> m.homeTeam().equals(homeTeam))
                .map(Match::id)
                .findFirst()
                .orElseThrow();
        board.updateScore(id, homeScore, awayScore);
    }

    private static void assertMatch(Match match, String home, String away, int homeScore, int awayScore) {
        assertEquals(home, match.homeTeam());
        assertEquals(away, match.awayTeam());
        assertEquals(homeScore, match.homeScore());
        assertEquals(awayScore, match.awayScore());
    }
}
