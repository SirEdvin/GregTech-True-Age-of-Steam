# Tasks

## 1. Typed observation and rule evaluation

- [x] 1.1 Add the provider interface and immutable typed observable descriptors/values under `src/main/java/site/siredvin/gttruesteam/api`; verify all four types, stable identifiers, and explicit unavailable values with focused tests and Java compilation.
- [x] 1.2 Implement the ordered rule model and pure evaluator under a new `machines/redstone` package; add the smallest runnable test support and verify all six numeric operators, exact float equality, signed zero, string case sensitivity, boolean true/false, XOR combination, order independence, matching zero contribution, empty/no-match fallback, and once-per-value snapshots.
- [x] 1.3 Implement shared rule validation and bounded serialization; verify round trips preserve order and types, and tests reject fractional/overflow integers, nonfinite floats, invalid strengths/operators, oversized strings, malformed entries, and over-capacity rule lists without crashing.

## 2. Hatch lifecycle and registration

- [x] 2.1 Verify the exact GTCEu 7.5.1 part attachment/detachment hooks, redstone setter/side semantics, global ability counting, and LDLib 1.0.40.b persistence/UI signatures against pinned source; record concrete symbols used and compile the selected adapters without upgrading dependencies.
- [x] 2.2 Implement the regular hatch part, server-tick observation, and front-only output; verify with a runtime fixture that idle formed controllers are sampled, XOR output changes by the next tick, and detached controllers and unavailable values contribute zero.
- [x] 2.3 Wire invalidation, removal, rotation, and reload handling; verify powered neighbors clear on the former front, output never restores stale saved strength, unloaded controllers are not force-loaded, and unchanged output does not send redundant updates.
- [x] 2.4 Register the addon-owned hatch ability and six tier definitions through `TrueSteamMachines`; verify registry entries exist for LV/MV/HV/EV/IV/LuV with capacities 1/2/3/4/5/6, front orientation, and no energy requirement.

## 3. Controller integrations and patterns

- [x] 3.1 Implement the observation contract on `InfernalBoilerMachine` using existing `getCycleCounter()` and `getHeatLevel()`; verify integer `heat_counter` and string `heat_level` follow existing heating, cooling, and exact-threshold behavior without altering recipe logic.
- [x] 3.2 Implement the contract on `IndustrialGasPressurizerMachine` using `getState() == PerfectConditionState.REACHED`; verify true for REACHED and false for cooldown, unreachable, low-fluid, and high-fluid states while preserving current readiness mechanics.
- [x] 3.3 Add the shared optional globally limited hatch predicate to boiler C/E/I/O casing alternatives and pressurizer d/x/i casing alternatives; verify zero-hatch structures still form, one hatch of every tier is accepted, two hatches across different categories fail, and non-primary-casing or mandatory positions remain unchanged.

## 4. UI and player-facing resources

- [x] 4.1 Build independent type-aware rule widgets with value/operator selection, operand input where appropriate, strength 0–15, and per-row Save/Delete without a shared rule selection; verify sixth-row scrolling/dropdowns and editing after middle-row deletion.
- [x] 4.2 Add server-authoritative mutation validation, synchronized current values/output, connection status, and invalid-rule feedback; verify malformed or out-of-range client edits cannot corrupt configuration or exceed tier capacity, including edits after detach/reconnect.
- [x] 4.3 Add localized labels/tooltips and tiered models with a visible front output overlay through the existing resource conventions; verify all six items and hatch screens render without missing translations/textures and dedicated-server loading avoids client-only class failures.
- [x] 4.4 Add six shaped recipes in `TrueSteamRecipes`, each using a same-tier hull, same-tier circuit, and comparator; run data generation and verify generated recipe identifiers, ingredient tags, output tiers, and successful recipe viewing/crafting.

- [x] 4.5 Reproduce and fix fresh-hatch saving of `heat_counter > 15`, including deleting and recreating the last rule.
- [x] 4.6 Expose boiler cycles until throttle and shared recipe progress/duration/ID; verify defaults on a formed controller without the custom interface, without broadening patterns.
- [x] 4.7 Register the formed model property and verify controller casing inheritance/restoration on both multiblocks at every tier, plus client-side appearance synchronization.

## 5. End-to-end verification and handoff

- [x] 5.1 Run the focused automated suite covering every comparison/type and capacity tier, serialization corruption, unavailable values, and zero-strength XOR contribution; inspect actual test results and require nonzero executed test counts.
- [x] 5.2 Exercise both real multiblocks in a runnable GameTest or documented test world: optional/global-limit attachment, live heat/readiness transitions, front-face strengths 0/1/15, rule reorder/delete, rotation, break/reform, chunk reload, and world restart; record pass/fail evidence and any environment blockers rather than claiming unrun runtime coverage.
- [x] 5.3 Run `./gradlew --no-daemon spotlessApply`, `./gradlew --no-daemon runData`, `./gradlew --no-daemon spotlessCheck`, and `./gradlew --no-daemon build`; inspect generated diffs for unrelated churn and report exact outcomes.
- [x] 5.4 Update player-facing documentation/changelog with capacities, priority, boolean checks, casing eligibility, and exposed values; verify documentation matches the implementation and both capability specs before requesting review.
