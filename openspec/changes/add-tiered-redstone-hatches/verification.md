# Redstone hatch verification

## Executed results

- JUnit: 36 tests, zero failures/errors/skips; XML under `build/test-results/test`.
- Final `./gradlew --no-daemon spotlessApply test build` and subsequent `spotlessCheck build`: passed.
- `timeout 120s ./gradlew --no-daemon runData`: all providers and HashCache completed, then exit 124 from timeout. This is the project's accepted workflow; no shutdown fix is claimed. Final generation wrote zero changed files and removed no stale files.
- Dedicated-server initial fixture: 143 passing assertions covering both multiblocks, zero/one/two hatches, all tiers/capacities, first-match output including zero, direction/rotation, a real lamp, unchanged-output notification suppression, hatch break/reform, observation thresholds/readiness, six crafting recipes, actual chunk unload/reload, and an injected association with a formed non-observable Electric Blast Furnace failing safely to zero.
- Separate world-restart fixture: 3 passing assertions covering saved rules and recomputed output.
- Final client fixture: 20 passing assertions using real UI clicks/typing for add/reorder/delete, zero output, invalid strength rejection, string/boolean selection, disconnect/reconnect and oversized strength-packet rejection. All six item models resolve; screenshots show the localized six-slot editor.

## Pinned integration audit

Dependencies remain unchanged: GTCEu 7.5.1 and LDLib 1.0.40.b.

- Lifecycle: `TieredPartMachine`, `controllerPositions`, `removedFromController`, `onLoad`, `onUnload`, `onRotated`, `subscribeServerTick`.
- Non-loading lookup: `ServerChunkCache.getChunkNow` and `MetaMachine.getMachine(chunk, pos)`.
- Output: `IRedstoneSignalMachine.updateSignal`, `getOutputSignal`, `getOutputDirectSignal`, `canConnectRedstone`. Minecraft query directions oppose the emitting face. Notify only on output change or rotation.
- Patterns: shared `PartAbility` and `TraceabilityPredicate.setMaxGlobalLimited(1)` across eligible casing alternatives. Other predicates remain unchanged.
- Persistence: `saveCustomPersistedData` / `loadCustomPersistedData` store ordered rules, never authoritative output.
- UI: LDLib selectors, text fields, labels and native widget synchronization. Popup widgets are last for reverse-order input dispatch. Oversized field packets are rejected before LDLib's default truncation.
- Recipes: existing `IGTAddon.addRecipes` hook registers them at runtime, not as six exported recipe JSON files. Runtime matching and assembly are verified instead.

## Reproduction and evidence

Fixtures are opt-in and excluded from release artifacts. Use only the disposable `run-redstone-test/` world, never production saves; accept its EULA yourself and choose a free server port if needed. Delete the relevant old report before each run and require a fresh `passed: true` report, not merely Gradle exit zero.

1. `./gradlew --no-daemon test spotlessCheck build`
2. `timeout 180s ./gradlew --no-daemon -PredstoneTest runServer`
3. `timeout 180s ./gradlew --no-daemon -PredstoneTest -PredstoneTestPhase=reload runServer`
4. Copy `run-redstone-test/world-extra/` into a fresh `run-redstone-test/saves/redstone-client/`, keeping `run-redstone-test/redstone-positions.json`. Set TMPDIR to an existing scratch directory, then run `timeout 180s xvfb-run -a ./gradlew --no-daemon -PredstoneTest -PredstoneTestPhase=client runClient`.

Server reports: `run-redstone-test/redstone-initial.json` and `redstone-reload.json`. Client report/screenshots: `$TMPDIR/redstone-client/`. This session's TMPDIR is `/home/siredvin/.hermes/profiles/albina/cache/scratch`; logs there include `redstone-runtime.log`, `redstone-client.log`, `redstone-rundata-final.log`, and `redstone-final-build.log`.

Artifact: `build/libs/gttruesteam-forge-1.20.1-0.3.3.jar` (development build, no version bump).

## Coverage scope

The final client fixture renders all six tier items beside the shared LuV editor; the inspected screenshot shows textured casings and front overlays without missing-texture placeholders. Integer/string/boolean screens use the same editor. Float behavior is unit-tested because neither initial controller exposes a float. Pattern exclusions and unchanged recipe/readiness mechanics are additionally checked by source review. These are focused integration checks, not an exhaustive modpack playtest.

No commits, pushes or publication have been performed.
