# AI Usage

This project was built with AI assistance (OpenCode coding agent) as a pair-programming tool.
AI drafted code and docs; all output was reviewed, adjusted, and tested by the author.

What AI helped with:
- Domain design: `Scoreboard` interface vs `InMemoryScoreboard`, immutable `Match` snapshot,
  `MatchId`, exception hierarchy, ordering (total score desc, then most recent first),
  thread-safety with `ConcurrentHashMap` + `ReadWriteLock`.
- Implementation and JUnit 5 tests, including the 5-match example ordering from the task.
- Extra operation proposal: `findMatchesByTeam` as a read-only extension in a separate commit.
- Drafts of `README.md` (assumptions, trade-offs) and this file.

Prompt flow (condensed):
1. Pasted Sportradar task, asked for project plan (Java 17, Maven, JUnit 5).
2. Approved build; agent scaffolded repo, implemented core ops in staged commits.
3. Added extra operation, docs, ran full verification.

Human decisions kept: Java 17 baseline, Maven coordinates, `AtomicLong` start-order tie-break,
allowing downward score corrections, rejecting a busy team, final choice of extra operation.

Verification:
```bash
mvn clean verify
git log --oneline
```
Result: `BUILD SUCCESS`, all tests in `InMemoryScoreboardTest` and `FindMatchesByTeamTest` pass.
