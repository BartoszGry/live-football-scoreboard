package com.sportradar.scoreboard;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FindMatchesByTeamTest {

    private Scoreboard board;

    @BeforeEach
    void setUp() {
        board = new InMemoryScoreboard();
    }

    @Test
    void returnsOnlyMatchesOfGivenTeam() {
        board.startMatch("Mexico", "Canada");
        board.startMatch("Spain", "Brazil");

        List<Match> result = board.findMatchesByTeam("Mexico");

        assertEquals(1, result.size());
        assertEquals("Mexico", result.get(0).homeTeam());
    }

    @Test
    void matchingIsCaseInsensitiveAndTrimmed() {
        board.startMatch("Mexico", "Canada");

        assertEquals(1, board.findMatchesByTeam("  meXico ").size());
        assertEquals(1, board.findMatchesByTeam("CANADA").size());
    }

    @Test
    void returnsEmptyListForUnknownTeam() {
        board.startMatch("Mexico", "Canada");

        assertTrue(board.findMatchesByTeam("Germany").isEmpty());
    }

    @Test
    void rejectsBlankTeam() {
        assertThrows(ScoreboardException.class, () -> board.findMatchesByTeam("  "));
        assertThrows(ScoreboardException.class, () -> board.findMatchesByTeam(null));
    }
}
