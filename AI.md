# AI.md — How AI Was Used

## Short summary

AI assistance (OpenCode agent, powered by Muse Spark) was used as a pair-programmer for
design, code generation, and documentation:

- Drafted the domain design (interface vs. implementation, `MatchId`, immutable `Match` snapshot,
  exception hierarchy, ordering comparator, thread-safety approach).
- Generated the initial implementation (`Scoreboard`, `InMemoryScoreboard`, `Match`, `MatchId`,
  exceptions) and the JUnit 5 test suites, including the exact example scenario from the task.
- Proposed three candidates for the extra operation and recommended `findMatchesByTeam`
  as the safest single-method extension.
- Drafted `README.md` (assumptions, reasoning, trade-offs) and this file.
- All AI-generated code was reviewed and verified by building with Maven (`mvn clean verify`)
  and by inspecting the git history for the required distinct feature commit.

Human decisions kept: Maven coordinates, Java 17 baseline, `AtomicLong` tie-break over wall-clock,
allowing downward score corrections, rejecting a busy team, and the final choice of the extra feature.

## Prompt history (condensed)

1. User (PL): "zaplanuj to i zrob nowy projket" + pasted Sportradar task (scoreboard requirements,
   example ordering, README/AI deliverables).
2. Assistant (plan mode): proposed location `live-football-scoreboard`, Java 17 + JUnit 5,
   package layout, 5-commit git plan, three extra-operation options; asked for location/Java/feature choice.
3. User: "ok buduj" (mode switched from plan to build).
4. Assistant: checked environment (found bundled Maven in IntelliJ + JDK 25, no system Maven/Java),
   scaffolded `pom.xml`/`.gitignore`, implemented core domain, committed in steps
   (`chore: scaffold` → `feat: core` → `test: core`), then added `findMatchesByTeam` in a distinct
   commit (`feat: find live matches by team name`), wrote docs, ran `mvn clean verify`.

No additional hidden prompts; the full task text above was the only specification input.

## Contextual information guiding the implementation

- Task text: Sportradar "Live Football World Cup Scoreboard", core ops
  (start / update / finish / summary ordered by total desc, then most recent first),
  the 5-match example (Mexico 0–5, Spain 10–2, Germany 2–2, Uruguay 6–6, Argentina 3–1
  → expected order Uruguay, Spain, Mexico, Argentina, Germany), exactly one extra operation
  with a distinct git commit, `README.md` + `AI.md` deliverables.
- Environment: Windows 11, `C:\PROJEKT` workspace (not a git repo; new repo created inside
  `C:\PROJEKT\live-football-scoreboard`), `git 2.45.1`, Maven 3.9.11 via IntelliJ bundle,
  JDK 25 toolchain compiling with `maven.compiler.release=17`, local `~/.m2` nearly empty
  (JUnit downloaded from Maven Central during the build).
- Key artifacts: this repo itself — `git log --oneline` shows the distinct feature commit;
  `mvn clean verify` output (all tests green) is the verification artifact.

## Verification

```bash
mvn clean verify
git log --oneline
```

Expected: `BUILD SUCCESS`, tests in `InMemoryScoreboardTest` + `FindMatchesByTeamTest` pass,
including the example-scenario ordering test.
