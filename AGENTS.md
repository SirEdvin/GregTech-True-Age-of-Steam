# AGENTS.md

Guidance for AI agents working in this repository.

## Project Overview

This is `GT: True age of Steam`, a Minecraft Forge 1.20.1 addon for GregTech Modern/GTCEu.

The mod id is `gttruesteam`, the Java package root is `site.siredvin.gttruesteam`, and the project builds with Gradle using Java 17 and `net.neoforged.moddev.legacyforge`.

Primary technologies:

- Java 17
- Minecraft Forge 47.4.x for Minecraft 1.20.1
- GregTech CEu Modern
- KubeJS/Rhino integration
- Sponge Mixin
- Spotless formatting

## Repository Layout

- `src/main/java/site/siredvin/gttruesteam/`: mod Java sources.
- `src/main/resources/`: runtime resources, `mods.toml`, mixin config, assets, and KubeJS plugin registration.
- `src/generated/resources/`: generated data output from the Gradle data run.
- `gradle.properties`: mod metadata and dependency versions.
- `gradle/scripts/spotless.gradle`: Java formatting configuration.
- `spotless/`: import order and Eclipse formatter files.

## Common Commands

Use the Gradle wrapper from the repository root.

```bash
./gradlew build
./gradlew spotlessApply
./gradlew runClient
./gradlew runServer
./gradlew runData
```

For formatting-only validation, use:

```bash
./gradlew spotlessCheck
```

`runData` writes generated resources to `src/generated/resources/` and uses existing resources from `src/main/resources/`.

## Coding Conventions

- Keep Java code under the existing `site.siredvin.gttruesteam` package hierarchy.
- Preserve existing GTCEu, Registrate, Forge, KubeJS, and Mixin patterns instead of introducing unrelated abstractions.
- Prefer small, focused changes that fit the current registration/data-generation structure.
- Run `./gradlew spotlessApply` after Java edits when feasible.
- Do not manually reformat files beyond what is needed for the change.
- Keep comments rare and useful; explain non-obvious game/modding behavior rather than restating code.

## Resources And Data

- Assets live under `src/main/resources/assets/gttruesteam/`.
- Generated recipes, tags, models, or other data should go through data generation when possible.
- If generated files change, make sure the source Java/data provider change that produces them is included.
- Keep `mod_id`, resource namespaces, mixin config names, and asset paths aligned with `gttruesteam`.

## Dependency Notes

- Dependency versions are centralized in `gradle.properties`; avoid hardcoding duplicate versions in source or docs.
- Minecraft, Forge, GTCEu, KubeJS, Rhino, Architectury, JEI, EMI, LDLib, and Registrate versions are intentionally pinned.
- Do not upgrade dependencies unless the task explicitly requires it.
- When dependency source lookup is needed and intellij-index MCP is not enough, try GitHub MCP/code search for the relevant mod source before accessing raw jar files directly.

## Testing And Verification

Before handing off code changes, prefer the narrowest useful verification:

- Java/source changes: `./gradlew build`
- Formatting-sensitive Java changes: `./gradlew spotlessCheck` or `./gradlew spotlessApply`
- Data provider/resource generation changes: `./gradlew runData` followed by `./gradlew build`
- Runtime behavior changes: `./gradlew runClient` or `./gradlew runServer` when practical

If a command cannot be run because of time, environment, or dependency constraints, report that clearly.

## Git And Workspace Safety

- The worktree may contain user changes. Do not revert or overwrite unrelated changes.
- Do not run destructive git commands unless explicitly requested.
- Do not create commits unless explicitly requested.
- Avoid committing generated artifacts unless they are expected project outputs for the change.

IMPORTANT: When applicable, prefer using intellij-index MCP tools for code navigation and refactoring.

## Java process output and token budget

When work in this repository requires Java, Minecraft, a mod loader, Gradle, or an installer process:

- Treat complete stdout/stderr as a file-backed artifact, not chat context. Redirect it to a timestamped file under a disposable `build/`, `run/`, or `work/logs/` directory and preserve the real exit code.
- Return only a compact summary to the agent: command, exit code, duration, full-log path, readiness/startup time, unique ERROR/FATAL/exception signatures, relevant test or content counts, and clean-shutdown evidence.
- Read a narrow window around a concrete failure from the saved log only when diagnosis requires it. Do not paste an entire `latest.log`, Gradle transcript, download list, or crash report into the conversation.
- Keep stdin attached for long-running servers so they can receive `stop`. Prefer a wrapper that writes every line to disk while emitting only rare readiness/failure markers, and prefer completion/readiness notifications over frequent polling.
- Apply the same rule to Forge/NeoForge/Fabric and Packwiz installers: retain full download and patch progress on disk, but report only success/failure, item counts, and the log path.
- Terminal-side truncation is not filtering: a truncated 50 KiB Java log can still consume roughly 15k model tokens while omitting the line that matters.
- Batch modpack migrations and related fixes before launching Minecraft, then perform consolidated runtime validation instead of restarting after every edit.
