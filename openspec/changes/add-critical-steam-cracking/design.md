# Design

## Context

See `proposal.md` for motivation and `specs/critical-steam-cracking/spec.md` for the behavioral contract. This design is warranted by material/configuration changes and the two-stage balance calculation.

Observed repository state:

- `gradle.properties:47` pins GTCEu 7.5.1; `build.gradle.kts` currently uses the project's `site.siredvin.forge`/ForgeGradle conventions, Java 17, and JUnit Jupiter 5.10.2. Do not replace the build system with the older overview's ModDevGradle description.
- `TrueSteamSteams.java:14-20` builds SUPERHOT with critical name `supercritical` and HELLISH with critical name `most_hellish`.
- `SteamRecord.java:181-195` creates unretained prototype cracked pairs, inserts no separator before `steam`, fixes their temperature to 775 K, and incorrectly uses LightFuel for Heavy Fuel. `SteamRecord.registerRecipes` currently only supplies steam fuel, boiling, and pressurization recipes.
- `TrueSteamRecipes.java:632` already calls every steam record's recipe registration; `GTTrueSteamGTAddon.addRecipes` supplies the existing addon lifecycle. Reuse it rather than creating an additional event path.
- `SteamConfiguration` currently holds steam/pressurization metadata, with no cracking coefficient.
- The existing JVM test example is `src/test/java/site/siredvin/gttruesteam/machines/redstone/RedstoneRuleTest.java`; `tasks.test` already enables JUnit Platform.

Pinned dependency source was inspected from the locally cached official 7.5.1 sources artifact. In `PetrochemRecipes.java:25-32,429-458,491-519`, the four feedstocks have light/severe Cracking Unit steam routes. Each consumes 1,000 mB feedstock and 1,000 mB steam, yielding 1,000 mB of the matching existing cracked fluid. Light uses circuit 1, 80 ticks, 240 EU/t; severe uses circuit 3, 160 ticks, and `GTValues.VA[HV]`. Chemical Reactor variants differ and are excluded by user decision. `GTRecipeTypes.java:615` permits two fluid inputs and two fluid outputs. `GTMultiMachines.CRACKER` uses ordinary fluid abilities and the standard coil/overclock modifiers, with no steam-only input predicate. `FirstDegreeMaterials.java:1300-1307` defines Steam at 373 K.

## Goals / Non-Goals

**Goals:**

- Keep material registration and recipe emission in their existing lifecycles.
- Define variant metadata once and compute boosted amounts from a shared baseline and the variant coefficient, not 32 independently tuned recipes.
- Keep yield arithmetic independently testable without initializing Forge materials in a plain JVM.
- Preserve GTCEu cracker modifiers and downstream compatibility; compare recipe definitions before coil/overclock modifiers.

**Non-Goals:**

- Runtime interception of GTCEu or arbitrary KubeJS recipe edits, dependency upgrades, or a general recipe-transformation framework.
- New machines, recipe types, custom cracked fluids, new distillation routes, or a configurable balancing UI.
- Migration support for removed, unshipped WIP cracked-fluid IDs; changes to published steam IDs.

## Decisions

### 1. Retain one residue Material and one coefficient per steam record

Extend the existing builder/configuration pattern with the cracking coefficient (1.5 and 1.7 at `TrueSteamSteams`) and explicit residue color/name metadata. Retain the registered residue in `SteamRecord` for recipe use. Derive its ID from the existing critical name plus `_steam_cracking_residue`, yielding the exact approved IDs. Use liquid storage/state, explicit RGB colors `0x9666CC` and `0x663399`, and explicitly set 373 K through the fluid builder. The user approved this adjustment during apply: materials are declared before fluids register, the upstream queued builder has no temperature getter, and no dynamic temperature lookup or separate Steam-equality verification is needed.

Use the existing material localization mechanism, with explicit display names where automatic formatting cannot guarantee the required names. Let data generation produce `en_us`/`en_ud` and other applicable material resources; do not hand-edit generated names.

Remove `registerCrackedPair`, `registerOilCracking`, and color-mixing code if it has no remaining callers. Alternative: preserve custom cracked materials and invent distillation chains. Rejected because it conflicts with the requested reuse of existing outputs and adds unnecessary registries.

### 2. Use a focused feedstock mapping and severity baseline

A small recipe-local helper under `site.siredvin.gttruesteam.recipe` (proposed `CriticalSteamCrackingRecipes.java`) maps the four raw materials to their existing lightly/severely steam-cracked materials. One light/severe descriptor supplies circuit, baseline duration, and EU/t. A shared baseline supplies the regular input/output amounts. `SteamRecord.registerRecipes` delegates once to this helper, using the record's critical steam, residue, and coefficient.

Recipe IDs are addon-owned and deterministic, incorporating critical variant, stage, severity, and feedstock, for example `gttruesteam:lightly_supercritical_steam_crack_heavy_fuel` and `gttruesteam:lightly_supercritical_residue_crack_heavy_fuel`. Save through the existing provider; emit no upstream overrides. Total new recipes: two variants × four feedstocks × two severities × two stages = 32.

Alternative: dynamically re-run/capture/transform upstream recipe providers or inspect the live recipe manager. Rejected: only yield calculations need to be dynamic; the pinned eight upstream baselines are known, and recursive provider capture or reload-time synthesis complicates lifecycle and KubeJS behavior. Regression checks compare definitions against the pinned upstream baselines so an eventual dependency upgrade surfaces drift.

### 3. Compute recovery yield from the eleven-batch balance equation

For regular output R, residue per first craft P = 100, residue per recovery craft Q = 1,000, N = Q/P = 10, and coefficient C:

- A = R/2 (first-stage cracked output)
- B = (N + 1) × C × R − N × A (residue-stage cracked output)
- first-stage duration = regular duration/2; recovery duration = regular duration

For the current R = 1,000, A = 500, B = 11,500 for SUPERHOT or 13,700 for HELLISH. Ten first crafts produce exactly Q residue; the eleventh craft consumes another feedstock batch, no additional steam, and returns no residue.

Use exact decimal or rational arithmetic with checked integer conversion (Java stdlib, e.g. `BigDecimal.valueOf`/`intValueExact`), not a floating-point cast that silently loses mB. Validate positivity, exact divisibility and integer outputs for the shared balance inputs; fail clearly for unsupported fractional or overflowing definitions rather than silently rounding. Keep this narrow calculation separate from material bootstrap for unit tests. Do not create a generic balance engine or hardcode B per recipe.

The coefficient contract covers eleven same-feedstock, same-severity batches. Residues intentionally pool across feeds and severities within a variant; there is no provenance tracking and no requirement that mixed-feed cycles have an identical per-product ratio.

### 4. Keep standard cracker behavior and existing output identities

Use `CRACKING_RECIPES` directly. First recipes have two inputs and two outputs; recovery recipes have two inputs and one output. Reuse each feedstock's corresponding upstream steam-cracked Material, including the distinct gas mapping. Keep severe EU/t as `VA[HV]`, not a guessed numeric constant.

Existing distillation consumes the same outputs, so no distillation changes are necessary. Base EU/t remains unchanged; the initial base energy reduction follows from duration only. Standard coil and overclock reductions continue to apply normally.

## Risks / Trade-offs

- [Large, bursty recovery outputs] → Approved yields are 11,500/13,700 mB per recovery craft; test sufficient output storage and blocked-output behavior. The first stage requires storage for two distinct output fluids.
- [Material/bootstrap code in JVM tests] → Test pure production balance logic in JUnit, and inspect registered fluids/emitted recipes through data generation and a real Forge runtime rather than bootstrapping world state in plain JUnit.
- [Pinned upstream baselines drift on upgrade] → Keep inputs, outputs, circuits, timing, and voltage centralized and checked against the pinned definitions; do not claim support for arbitrary pack overrides.
- [Residue mixing enables cross-feed recovery] → This is explicitly approved: one residue per critical variant, shared across feeds and severities. Validate cycles with identical feeds when checking the numeric multiplier.
- [Removing prototype material IDs from an existing WIP world] → Disclose the registry removal; use disposable test worlds or back up WIP saves before switching. Preserve every published steam/material ID outside the prototype set.
- [Generated resources retain removed entries] → Verify only the obsolete prototype material language/assets disappear, all residue entries appear, and unrelated generated resources remain intact.
- [runData can linger after providers finish in this project] → Verify provider/HashCache completion and output files; report a timeout honestly rather than treating it as a clean exit or expanding scope into shutdown repair.

## Migration Plan

Implement as one coherent feature through the existing addon lifecycle, regenerate affected resources, then verify formatting, build/tests, emitted recipes, and real cracker processing in a disposable world. No production code or generated resources change during this proposal workflow.

Back up any WIP saves containing prototype cracked fluids before testing. There is no mapping of those unused prototype fluids into standard cracked fluids in this change. Reverting the feature restores the old registrations but cannot recover fluid stacks discarded by loading a save after removal; retain the backup for rollback. No special migration is needed for released steam IDs or upstream recipes.
