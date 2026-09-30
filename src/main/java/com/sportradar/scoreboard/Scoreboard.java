package com.sportradar.scoreboard;

import java.util.List;

/**
 * Scoreboard library supporting multiple simultaneous matches.
 *
 * <p>All methods are thread-safe.
 */
public interface Scoreboard {

    /**
     * Starts a new match with an initial score of 0-0.
     *
     * @param homeTeam home team name, non-blank
     * @param awayTeam away team name, non-blank, different from home team
     * @return id of the newly started match
     * @throws ScoreboardException if team names are invalid or either team
     *                             already plays a live match
     */
    MatchId startMatch(String homeTeam, String awayTeam);

    /**
     * Updates the absolute score of a live match.
     *
     * @param matchId   id returned by {@link #startMatch}
     * @param homeScore non-negative home score
     * @param awayScore non-negative away score
     * @throws MatchNotFoundException if there is no live match with this id
     * @throws InvalidScoreException  if any score is negative
     */
    void updateScore(MatchId matchId, int homeScore, int awayScore);

    /**
     * Finishes a live match and removes it from the scoreboard.
     *
     * @param matchId id of the match to finish
     * @throws MatchNotFoundException if there is no live match with this id
     */
    void finishMatch(MatchId matchId);

    /**
     * Returns live matches ordered by total score (descending);
     * ties are broken by most recently started match first.
     *
     * @return immutable snapshot list, never null
     */
    List<Match> getSummary();

    /**
     * Extra operation: returns live matches involving the given team.
     *
     * <p>Matching is case-insensitive and ignores leading/trailing whitespace.
     * Ordering follows {@link #getSummary()}.
     *
     * @param team team name to search for, non-blank
     * @return immutable snapshot list, never null
     */
    List<Match> findMatchesByTeam(String team);
}
