package com.sportradar.scoreboard;

/**
 * Base class for all scoreboard failures. Unchecked so the library
 * stays easy to use; specific subclasses allow precise handling.
 */
public class ScoreboardException extends IllegalArgumentException {

    public ScoreboardException(String message) {
        super(message);
    }
}
