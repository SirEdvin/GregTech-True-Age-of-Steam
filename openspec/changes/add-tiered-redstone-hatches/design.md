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

A rule stores value identifier, expected type, operator, typed operand where applicable, and output strength. Keep the ordered evaluator separate from world/UI code. Reject incompatible types instead of coercing strings or booleans. Exact numeric equality uses numerical equality, including equal signed zero; reject NaN and infinities. Strings use exact case-sensitive equality. Alternative: generic untyped maps or expression parsing would make validation and persistence less reliable without serving this scope.

### Tiered regular multiblock part

Add a dedicated redstone hatch part implementation and addon-owned part ability, registered through `TrueSteamMachines` for LV through LuV. Store the explicit tier-to-capacity mapping rather than deriving it from unrelated voltage arithmetic. Accept all six tiers on both initial machines, without energy draw or controller-tier gating.

Use a shared optional predicate with one global maximum of one across every primary-casing alternative. Boiler eligibility is C/E/I/O; pressurizer eligibility is d/x/i. Preserve existing mandatory positions and other ability alternatives. Do not enable the new ability through a broad automatic-abilities path for unrelated machines. Alternative: adjacent block scanning violates regular hatch attachment and makes ownership ambiguous.

### Server evaluation and fail-safe redstone

Subscribe to server ticks through the pinned machine lifecycle. Resolve the attached formed controller via the regular part relationship; do not cache a controller reference across detachment. Evaluate while formed even when recipes are idle, because the pressurizer's cooldown changes without recipe activity. Up to six rules makes per-tick evaluation bounded.

Use GTCEu's supported redstone output mechanism after verifying side conventions and neighbor notification behavior. Only the front face can output. Recompute by the next tick after valid configuration edits. Clear output immediately on detachment/invalidation; start loaded parts at zero until attachment is valid. Notify affected old/new faces on rotation and notify on actual strength changes, not every unchanged tick. Do not force-load chunks. Persist rules, not an authoritative active signal.

Alternative: recipe completion callbacks miss idle heat decay/readiness transitions; emitting on all faces conflicts with the user's chosen behavior.

### Compact ordered-rule UI and defensive persistence

Use existing LDLib/GTCEu modular UI conventions with a scrollable ordered list, selected-row editor, add/delete, and explicit up/down buttons. Show connection status, current values, tier capacity, validation state, and output. Operator selection follows value type; boolean checks have no operand field. Show stable heat-level tokens clearly so equality rules are understandable.

Serialize ordered rules as bounded structured entries in the hatch's persisted data using supported pinned APIs. Validate the same model on load and on every server mutation. Reject invalid UI submissions atomically, retaining the previous configuration. Preserve unknown identifiers as invalid visible entries rather than rebinding by index. Drop excess tail entries on malformed over-capacity saves; disable malformed entries within the retained capacity. Bound strings to 256 characters and encoded identifiers to 128 characters; reject oversized UI payloads and safely invalidate oversized saved entries. Reordering is a validated server operation, not a client-only list change.

Alternative: independent widgets mutating fields directly risks partially valid rules and tier-limit bypass.

### Registration, recipes, and resources

Use addon-namespaced tier-specific identifiers, tier casing visuals, and a recognizable front output overlay. Add localization for type/operator labels, error states, rule controls, and capacity. Proposed survival default: one same-tier machine hull, one same-tier circuit, and one comparator in a shaped recipe per hatch; resolve exact ingredient symbols from the pinned dependency rather than inventing APIs. No upgrades or energy buffers are needed. Generate resources through existing providers and inspect their diff.

### Verification structure

Introduce focused automated tests for the pure rule model/evaluator and serialization boundaries with the smallest compatible test setup. Exercise live pattern attachment, global limit, redstone direction/rotation, detached zero output, and save/reload through GameTests if runnable or a documented client/server test world. Test all capacity tiers and all four data types; fixture providers cover float values even though initial controllers expose no floats. UI verification must include reordering and server-side rejected mutations, not just screenshots or compilation.

## Risks / Trade-offs

- [Pinned GTCEu API uncertainty] → Audit the exact 7.5.1 part lifecycle, ability predicate counting, redstone side convention, and LDLib widget/persistence APIs before implementing the corresponding adapters. This is a targeted compatibility check, not a scope decision.
- [One-per-pattern limit accidentally scoped per character] → Share one ability/predicate identity as supported by the pinned API and test two hatches across different eligible character classes.
- [Readiness changes with wall-clock time] → Reuse `getState()` and snapshot it per evaluation; do not alter existing semantics.
- [Invalidation/rotation leaves powered neighbors] → Cover old-face notifications and clear-before-detach behavior with runtime tests.
- [No established standalone test harness identified] → Keep rule logic independent and add only the minimal runnable testing support; clearly distinguish build success from gameplay validation.
- [Recipes and passive operation were not specified by the user] → Treat the simple recipe, no-energy operation, exact case-sensitive strings, and zero fallback as explicit proposed defaults for review.

## Migration Plan

This is additive: keep existing registry identifiers and machine state fields unchanged. Existing structures with zero hatches continue to form. New hatches start with an empty rule list and zero output. Validate data generation, build, and runtime behavior before release. For rollback, remove newly placed hatches and restore their casing blocks before loading a version without their registry entries; retain a world backup.
