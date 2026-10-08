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

How it was used:
1. Asked AI to plan the project from the task description.
2. Built the core features step by step with AI-generated drafts.
3. Added the extra feature, wrote docs, and verified everything with tests.

My role: I reviewed the generated code, fixed edge cases, chose the extra
operation (`findMatchesByTeam`), and confirmed the final structure and behavior.

Verification:
```bash
mvn clean verify
git log --oneline
```
Result: `BUILD SUCCESS`, all tests in `InMemoryScoreboardTest` and `FindMatchesByTeamTest` pass.
