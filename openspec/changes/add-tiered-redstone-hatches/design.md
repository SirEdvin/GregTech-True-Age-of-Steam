# Design

## Context

See proposal.md for motivation and the two capability deltas for observable behavior.

Inspected sources establish these integration points:
- `gradle.properties` pins Minecraft 1.20.1, Forge 47.4.10, GTCEu 7.5.1, and LDLib 1.0.40.b. The actual `build.gradle.kts` uses `site.siredvin.forge` and ForgeGradle APIs, despite AGENTS.md's older ModDev description; preserve the actual build.
- `InfernalBoilerRecipeLogic` already exposes `getCycleCounter()` and `getHeatLevel()`, with persisted managed fields. Heat levels use strict `>` thresholds. MAX is a counter cap sentinel, not a returned effective level; do not change this logic.
- `IndustrialGasPressurizerMachine.getState()` returns an enum based on the existing wall-clock cooldown and input tank thresholds. Map only REACHED to true; do not recreate or alter its calculation.
- Boiler casing occurs in C and in casing alternatives E/I/O. Pressurizer primary casing occurs in d and alternatives x/i. These categories must share one global hatch limit.
- `TrueSteamMachines` owns addon machine registration, `TrueSteamRecipes.registerRecipes` owns recipes, and `TrueSteamLang` provides existing display text conventions.
- No test files were found by the initial filename scan; the inspected build enables the mod's GameTest namespace for client/server but does not declare a dedicated GameTest run or unit-test dependency.
- No existing capability specs exist. Exact upstream hatch lifecycle, redstone setter, and widget signatures have not been audited in this planning pass; implementation must verify them against 7.5.1 before use, rather than assuming APIs from newer versions.

## Goals / Non-Goals

Goals: Keep observation independent from recipes, rule evaluation independently testable, and authoritative mutation on the server. Reuse GTCEu part attachment and LDLib UI/persistence rather than adding another networking or storage framework.

Non-goals: No expression language, grouped AND/OR conditions, hysteresis, tolerance controls, arbitrary adjacent-controller discovery, new heat/readiness mechanics, extra machine integrations, dependency upgrades, or mixins.

## Decisions

### Typed controller contract and pure evaluator

Introduce an addon-owned provider interface and small immutable descriptor/value types under `api`, with stable string identifiers, translatable labels, and explicit INTEGER/FLOAT/STRING/BOOLEAN tags. Use signed 64-bit integers and finite Java doubles internally for the numeric contract; expose the existing boiler counter losslessly. Use explicit unavailable results, not fabricated defaults. An evaluation snapshots each referenced value once so repeated rules cannot observe different readiness values during one pass.

A rule stores value identifier, expected type, operator, typed operand where applicable, and output strength. Keep the evaluator separate from world/UI code. Evaluate every valid matching rule and bitwise-OR its strength into the output; zero contributes nothing, identical strengths remain unchanged, and ordering has no effect. Reject incompatible types instead of coercing strings or booleans. Exact numeric equality uses numerical equality, including equal signed zero; reject NaN and infinities. Strings use exact case-sensitive equality. Alternative: generic untyped maps or expression parsing would make validation and persistence less reliable without serving this scope.

### Shared observations and first-rule editing

Shared `recipe_progress_percent` is FLOAT, calculated from progress and effective duration as a percentage clamped to 0–100. Preserve fractions; idle recipes or nonpositive duration yield zero. Keep existing tick observations unchanged. Escape the literal percent sign in the localized label for Minecraft/LDLib formatting.

Wrap attached formed controllers with `MultiblockRedstoneObservations`. Preserve custom descriptors, then add reserved recipe progress/duration tick counts and current recipe ID for `IRecipeLogicMachine` controllers. Idle progress/duration are zero, and idle recipe ID is unavailable rather than the cached last recipe. Existing controller interfaces remain optional for these defaults. Boiler `cycles_until_throttle` delegates to its existing infernal charges. No additional patterns become eligible.

Give every saved rule its own compact editor and show one blank draft row while capacity remains. No rule selection or Add action is needed before editing. Each row saves atomically through the existing server validation; deletion refreshes shifted rows from their retained rules.

### Tiered regular multiblock part

Add a dedicated redstone hatch part implementation and addon-owned part ability, registered through `TrueSteamMachines` for LV through LuV. Store the explicit tier-to-capacity mapping rather than deriving it from unrelated voltage arithmetic. Accept all six tiers on both initial machines, without energy draw or controller-tier gating.

Use a shared optional predicate with one global maximum of one across every primary-casing alternative. Boiler eligibility is C/E/I/O; pressurizer eligibility is d/x/i. Preserve existing mandatory positions and other ability alternatives. Do not enable the new ability through a broad automatic-abilities path for unrelated machines. Alternative: adjacent block scanning violates regular hatch attachment and makes ownership ambiguous.

### Server evaluation and fail-safe redstone

Subscribe to server ticks through the pinned machine lifecycle. Resolve the attached formed controller via the regular part relationship; do not cache a controller reference across detachment. Evaluate while formed even when recipes are idle, because the pressurizer's cooldown changes without recipe activity. Up to six rules makes per-tick evaluation bounded.

Use GTCEu's supported redstone output mechanism after verifying side conventions and neighbor notification behavior. Only the front face can output. Recompute by the next tick after valid configuration edits. Clear output immediately on detachment/invalidation; start loaded parts at zero until attachment is valid. Notify affected old/new faces on rotation and notify on actual strength changes, not every unchanged tick. Do not force-load chunks. Persist rules, not an authoritative active signal.

Alternative: recipe completion callbacks miss idle heat decay/readiness transitions; emitting on all faces conflicts with the user's chosen behavior.

### Independent rule widgets and defensive persistence

Use the configurable maintenance hatch's GTCEu textures and synchronized component panels inside the inherited Fancy UI shell. A scrollable list contains independent `RedstoneRuleWidget` editors with value/operator selectors, operand, strength, Save, Delete, and current-value/validation display. There is no shared selected-rule editor or ordering control. Keep fixed widget identities for network synchronization, show a blank row while capacity remains, and preserve server validation for every atomic Save. Earlier rows receive popup input above later rows; opening a dropdown scrolls it into view, with trailing space for the last row's popup. Show connection status, tier capacity, and output separately. Boolean checks hide their operand field.

Serialize ordered rules as bounded structured entries in the hatch's persisted data using supported pinned APIs. Validate the same model on load and on every server mutation. Reject invalid UI submissions atomically, retaining the previous configuration. Preserve unknown identifiers as invalid visible entries rather than rebinding by index. Drop excess tail entries on malformed over-capacity saves; disable malformed entries within the retained capacity. Bound strings to 256 characters and encoded identifiers to 128 characters; reject oversized UI payloads and safely invalidate oversized saved entries. Reordering is a validated server operation for organization only; it does not change OR output.

Do not mutate persisted rule fields on every keystroke: independent widgets retain drafts and use the same validated atomic save operation.

### Registration, recipes, and resources

Use addon-namespaced tier-specific identifiers, tier casing visuals, and a recognizable front output overlay. Register `GTMachineModelProperties.IS_FORMED` with default false: GTCEu's part lifecycle updates it on attachment/detachment, enabling the controller's casing appearance while formed and restoring the tier hull when detached. Keep the existing overlay. Add localization for type/operator labels, error states, rule controls, and capacity. Survival recipes use one same-tier machine hull, one same-tier circuit, and one comparator. No upgrades or energy buffers are needed. Generate resources through existing providers and inspect their diff.

### Verification structure

Introduce focused automated tests for the pure rule model/evaluator and serialization boundaries. Exercise live pattern attachment, casing appearance across all tiers and both controllers, global limit, redstone direction/rotation, detached zero output, and save/reload in the isolated runtime fixture. Test all capacity tiers and all four data types; fixture providers cover float values. Client verification includes independent editing, middle-row deletion, last-row scrolling/dropdowns, and server-side rejected mutations, not just screenshots or compilation.

## Risks / Trade-offs

- [Pinned GTCEu API uncertainty] → Audit the exact 7.5.1 part lifecycle, ability predicate counting, redstone side convention, and LDLib widget/persistence APIs before implementing the corresponding adapters. This is a targeted compatibility check, not a scope decision.
- [One-per-pattern limit accidentally scoped per character] → Share one ability/predicate identity as supported by the pinned API and test two hatches across different eligible character classes.
- [Readiness changes with wall-clock time] → Reuse `getState()` and snapshot it per evaluation; do not alter existing semantics.
- [Invalidation/rotation leaves powered neighbors] → Cover old-face notifications and clear-before-detach behavior with runtime tests.
- [No established standalone test harness identified] → Keep rule logic independent and add only the minimal runnable testing support; clearly distinguish build success from gameplay validation.
- [Recipes and passive operation were not specified by the user] → Treat the simple recipe, no-energy operation, exact case-sensitive strings, and zero fallback as explicit proposed defaults for review.

## Migration Plan

This is additive: keep existing registry identifiers and machine state fields unchanged. Existing structures with zero hatches continue to form. New hatches start with an empty rule list and zero output. Validate data generation, build, and runtime behavior before release. For rollback, remove newly placed hatches and restore their casing blocks before loading a version without their registry entries; retain a world backup.
