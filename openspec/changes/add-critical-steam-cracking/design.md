# Design

## Context

See proposal.md and the delta spec for the corrected contract. The corrected three-stage implementation is now verified on PR #15. This follow-up replaces its copied upstream recipe data with discovery of GTCEu-generated recipes, without changing material identities or the agreed balance.

The project pins GTCEu 7.5.1, Java 17, ForgeGradle conventions, and JUnit 5.10.2. Reuse `SteamRecord.registerRecipes`, the addon provider, existing `CrackingYield`, and opt-in `crackingTest` fixtures.

Pinned `PetrochemRecipes.java:235-400` defines eight relevant steam-cracked Tower recipes. Each consumes 1,000 mB and runs 120 ticks at VA[MV]. Their fluid quantities include odd values (3, 15, 25, 65, 75, 85), so halving each product in a 1,000 mB recipe is not exact. Carbon uses rational chance strings (1/9 or 1/3), not deterministic half-items. The user approved doubled custom distillation batches and expected-yield solid accounting.

## Goals / Non-Goals

Goals: implement the intended material chain; preserve exact proportional fluid yield; retain normal machine modifiers and ordinary routes; test actual distillation/residue recovery rather than merely recipe definitions.

Non-goals: new machines, mixins, dependency upgrades, runtime adaptation to arbitrary pack overrides, migration of malformed prototype IDs, or unrelated PR/history changes.

## Decisions

### Retain custom cracked material pairs

Extend `SteamRecord` to retain one light/severe custom material pair for each feedstock, as well as its residue. Share one explicit mapping to regular cracked materials so Heavy Fuel and Refinery Gas cannot accidentally use Light Fuel outputs.

Use corrected addon IDs such as `lightly_most_hellish_steam_cracked_light_fuel`, readable names, and explicit fluid-builder state/temperature from pinned corresponding regular material definitions. At material creation, fluids may not yet be registered; inspect queued properties/source rather than calling unregistered fluid getters or adding reflection. Use a simple deterministic RGB blend (equal weights as a minor implementation default) with critical steam and regular cracked-material colors. Retain residue properties and coefficients unchanged.

Rejected: reusing ordinary cracked fluids initially; that loses variant identity and cannot select reduced-yield distillation.

### Separate the three stages

Initial recipes retain the current 16 IDs where practical but output 1,000 mB custom cracked fluid only. Recovery retains its 16 IDs and ordinary output identities. Add 16 addon-owned Tower recipes for the custom fluids through the existing provider; no upstream overrides.

Custom Tower input is 2,000 mB, fluid outputs equal one regular 1,000 mB recipe's outputs, and residue output is 200 mB. Use pinned regular distillation EU/t and proportionally scaled duration (240 ticks for double input), preserving ordinary modifiers. Do not synthesize independent Distillery fraction routes for custom fluids: giving residue on each fraction would duplicate it. Explicitly inspect GTCEu's automatic Distillery generation controls during apply and disable that generation for these recipes using supported API.

Rejected: 1,000 mB distillation with rounded halves, which breaks per-product balance; residue in the cracker, which contradicts the intended chain.

### Discover generated upstream definitions

Install idempotent `GTRecipeType.onRecipeBuild` callbacks during addon initialization, before GTCEu generates recipes. Chain the previous callback, especially the Tower's native Distillery generator. Match GTCEu-owned Cracking Unit recipes by ordinary Steam, supported raw material, and matching regular cracked output; match Tower recipes by their regular cracked input. Do not scan or mutate the final RecipeManager.

Copy the matched builder, clear its callback on the derived copy to avoid recursion, and replace only the stage-specific fluid identities/amounts and duration. Preserve upstream circuits, EU/t, remaining content, chance metadata, data, and conditions. Append deterministic residue to doubled custom distillation and retain `disableDistilleryRecipes(true)`. Keep stable addon IDs and only the explicit four-feedstock/light-severe material mapping; remove product arrays and Carbon/timing/energy tables from production code.

Generate directly through the callback's consumer without retaining recipe snapshots across reloads. Repeat generation must emit the same 48 unique recipes. Real-loader probes vary generated circuits, Steam quantities, durations, EU/t, products, and Carbon chance to prove derivation rather than agreement with another copied table. Preserve upstream definitions and native Distillery routes; exclude addon-owned, hydrogen, and unsupported-feed recipes. Later KubeJS/datapack edits are not promised to propagate.

### Scale carbon by equivalent input, not twice

For a 2,000 mB custom batch, preserve the regular one-batch Carbon stack and exact rational probability. This already halves expected carbon per 1,000 mB equivalent. Halving that probability again would incorrectly yield only 25% of normal. Keep chance boosts consistent with the pinned recipe (zero) and verify rational chance metadata rather than Monte Carlo outcomes.

### Balance final products

For each regular final product Y, ten initial crafts plus five custom distillations yield 5Y and 1,000 mB residue. Recovery cracked-fluid quantity B solves `5 + B/1000 = 11*C`, hence `B = 1000*(11*C - 5)` (11,500/13,700 mB). Existing exact decimal arithmetic can be reused, but rename/document its half-yield quantity as ordinary-distillation-equivalent volume, not initial cracked output.

Carbon follows the same equation in expectation. Ordinary Tower recipes still consume full 1,000 mB inputs: recovery leaves fractional-batch fluid in storage. Use repeated whole cycles (ten cycles accommodate both variants) for physical exact-yield verification, retaining remainder fluid rather than inventing fractional recipes or discarding fluid. Compare every final fluid separately and carbon analytically. This is not a claim that each individual eleven-feedstock cycle immediately distills every last mB.

### Correct evidence and publication

Replace assertions that initial cracking emits residue or ordinary cracked fluids. Extend fixtures with real formed Tower processing, residue transfer, normal recovery, and final product quantities. Preserve upstream definition comparisons, blocked-output/insufficient-residue checks and client metadata assertions. Document custom names, two-batch distillation, chance semantics and retained ordinary fluid remainders. Earlier reports remain historical, not proof of this revision.

Update PR #15 by normal follow-up commits after corrected implementation and fresh tests; do not rewrite history or merge automatically.

## Risks / Trade-offs

- Additional Tower output (residue) → verify native recipe output limit and test a fully equipped Tower. The user explicitly declined an addon guard: register ordinary recipes and leave GTCEu missing-output/voiding behavior unchanged.
- Automatic fraction recipe generation → explicitly prevent residue duplication through custom Distillery routes.
- Integer fluid/chance arithmetic → use doubled custom batches, exact rational chance metadata and checked yield calculations; no rounding.
- Queued fluid registration → inspect pinned builder/property lifecycle before choosing explicit inherited properties.
- Large recovery outputs and residual partial batches → verify capacities, blocked outputs, retention and full repeated-cycle throughput.
- WIP ID changes → preserve released IDs, document backups and no migration guarantee.
- Lingering datagen JVM → verify providers/HashCache and resources; report timeout separately.

## Migration Plan

Reopen affected tasks rather than retaining obsolete completion claims. Implement and test the revised chain, regenerate resources, update documentation and PR #15. Back up WIP saves; no automatic prototype-fluid migration. Rollback restores prior code, not stacks already discarded while loading a save.
