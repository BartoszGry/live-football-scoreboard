# Live Football World Cup Scoreboard

Simple Java library managing a live football scoreboard for multiple simultaneous matches.
Built for the Sportradar coding exercise as a Maven package (`com.sportradar:live-football-scoreboard`).

## Quick start

```java
Scoreboard board = new InMemoryScoreboard();

MatchId mexicoCanada = board.startMatch("Mexico", "Canada");
MatchId spainBrazil = board.startMatch("Spain", "Brazil");

board.updateScore(mexicoCanada, 0, 5);
board.updateScore(spainBrazil, 10, 2);

List<Match> summary = board.getSummary();
// ordered by total score desc, then most recently started first

board.finishMatch(mexicoCanada);

// Extra operation
List<Match> mexico = board.findMatchesByTeam("mexico");
```

Build and test:

```bash
mvn clean verify
```

Requires JDK 17+ and Maven 3.8+.

## API

| Operation | Signature | Description |
|---|---|---|
| Start match | `MatchId startMatch(String homeTeam, String awayTeam)` | New match at 0–0, returns its id |
| Update score | `void updateScore(MatchId id, int homeScore, int awayScore)` | Sets absolute score |
| Finish match | `void finishMatch(MatchId id)` | Removes match from the board |
| Summary | `List<Match> getSummary()` | Live matches ordered by total score desc, then most recently started first |
| Find by team (extra) | `List<Match> findMatchesByTeam(String team)` | Live matches involving a team, same ordering |

`Match` is an immutable snapshot (`id, homeTeam, awayTeam, homeScore, awayScore, startOrder, startedAt`).
All errors are unchecked: `ScoreboardException` base, with `MatchNotFoundException`,
`DuplicateMatchException`, `InvalidScoreException`.

## Assumptions

1. **Match identity:** matches are identified by an opaque `MatchId` (random UUID), not by team names.
   Team names are not unique over time — the same pairing can be played again after `finishMatch`.
2. **Team names:** trimmed, non-blank; home and away must differ (case-insensitive).
   A team cannot play two live matches at once (case-insensitive check) — a deliberate domain rule.
3. **Scores:** `updateScore` sets the absolute score, not an increment. Any non-negative value is accepted,
   including corrections downward (common in sports-data feeds). Negative values are rejected.
4. **Finished matches:** removed from the board. Any later `updateScore`/`finishMatch` on the id fails
   with `MatchNotFoundException`. Finished teams become available again immediately.
5. **Ordering:** `totalScore = homeScore + awayScore` descending; ties broken by start order descending
   (monotonic `AtomicLong` sequence). A sequence counter is used instead of wall-clock time so two matches
   started within the same millisecond still have a deterministic order. A final UUID tie-break guarantees
   a total order.
6. **Snapshots:** `getSummary()` / `findMatchesByTeam()` return immutable copies (`List.copyOf`), so callers
   cannot corrupt internal state and iteration is safe under concurrency.

## Reasoning

- **Interface + in-memory implementation:** `Scoreboard` is an interface so alternative stores
  (e.g. Redis-backed for a distributed feed) can be added without changing callers.
  `InMemoryScoreboard` is the default.
- **Thread safety:** the library is expected to be fed by multiple threads (live data ingress).
  A `ConcurrentHashMap` holds live matches, a `ReentrantReadWriteLock` guards structural changes
  (start/finish/snapshot), and each mutable entry is additionally synchronized so a score update
  racing with a snapshot never produces a torn read. Snapshots are built under the read lock.
- **Validation as domain errors:** specific unchecked exceptions keep the happy path clean while letting
  feed consumers catch e.g. `DuplicateMatchException` separately from bad input.
- **No external dependencies** in `main` — only JUnit 5 for tests. Easy to embed in any service.

## Trade-offs

| Decision | Alternative | Why this way |
|---|---|---|
| `AtomicLong` start order instead of `Instant` only | Order by `startedAt` | Deterministic under same-millisecond starts; `startedAt` is still stored for consumers |
| Reject same team in two live matches | Allow it | Real World Cup constraint; prevents ambiguous feeds. Relaxing it later is a one-line change in `ensureTeamNotBusy` |
| Allow score decreases (corrections) | Enforce monotonic increase | Sports-data feeds issue corrections; rejecting them would drop valid updates. Documented in `AI.md`/tests |
| `ReadWriteLock` + per-entry lock | Plain `synchronized` methods | Better read scalability for summary polling; slightly more complex but contained |
| Unchecked exceptions | Checked exceptions | Library ergonomics; callers handling a fast live feed should not be forced through `try/catch` noise |

## Extra operation: `findMatchesByTeam`

Single additional operation, introduced in its own commit (`feat: find live matches by team name`).

```java
List<Match> findMatchesByTeam(String team)
```

- Case-insensitive, whitespace-tolerant lookup of live matches for one team.
- Reuses the summary ordering, returns an immutable list.
- **Why this one:** for a sports-data company the most common read after the full summary is
  "what is team X doing right now" (per-team widgets, alerts, commentary feeds). It is a natural,
  read-only extension that exercises the same snapshot/ordering path without changing write semantics.
  Alternatives considered (pause/resume, undo history) would have added state or a second method,
  violating the "exactly one operation" constraint or complicating the concurrency story.

## Project layout

```
pom.xml
src/main/java/com/sportradar/scoreboard/
  Scoreboard.java  InMemoryScoreboard.java
  Match.java  MatchId.java
  ScoreboardException.java  MatchNotFoundException.java
  DuplicateMatchException.java  InvalidScoreException.java
src/test/java/com/sportradar/scoreboard/
  InMemoryScoreboardTest.java  FindMatchesByTeamTest.java
README.md  AI.md
```
