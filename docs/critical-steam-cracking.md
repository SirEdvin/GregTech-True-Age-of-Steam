# Critical-steam oil cracking

The Cracking Unit can crack Light Fuel, Heavy Fuel, Naphtha, and Refinery Gas with non-dense Supercritical Steam or Most Hellish Steam. The products are GTCEu's existing lightly/severely steam-cracked fluids, so their usual distillation recipes still work.

## First stage

Each craft consumes 1,000 mB feedstock and 1,000 mB critical steam. It produces 500 mB of the matching cracked fluid and 100 mB of that steam's cracking residue. Provide storage for both output fluids.

- Light: circuit 1, 40 ticks, 240 EU/t.
- Severe: circuit 3, 80 ticks, the same EU/t as regular severe steam cracking.

These base durations are half the corresponding regular steam recipe durations. EU/t is unchanged; the reduced duration provides the energy reduction. The normal Cracking Unit coil/overclock modifiers still apply.

## Residue recovery

A recovery craft consumes another 1,000 mB feedstock batch and 1,000 mB residue, with no fresh steam or hydrogen. It produces only the matching cracked fluid:

| Residue | Cracked output | Color | Temperature |
| --- | --- | --- | --- |
| Supercritical Steam Cracking Residue | 11,500 mB | Purple `#9666CC` | 373 K |
| Most Hellish Steam Cracking Residue | 13,700 mB | Dark purple `#663399` | 373 K |

Both residues are liquids. Their registry IDs are `gttruesteam:supercritical_steam_cracking_residue` and `gttruesteam:most_hellish_steam_cracking_residue`.

Light recovery uses circuit 1, 80 ticks, 240 EU/t. Severe recovery uses circuit 3, 160 ticks, and regular severe steam-cracking EU/t. Make room for the large recovery output; recovery does not produce more residue.

Each steam variant has one shared residue across all four feedstocks and both severities. Residue can therefore be pooled and used with a different supported feedstock or severity; it does not remember where it came from.

## Total yield

Ten first-stage crafts produce 5,000 mB cracked fluid and exactly 1,000 mB residue. One recovery craft consumes that residue plus an eleventh feedstock batch:

- SUPERHOT: 5,000 + 11,500 = 16,500 mB, versus 11,000 mB from eleven regular crafts: 1.5× total yield.
- HELLISH: 5,000 + 13,700 = 18,700 mB: 1.7× total yield.

The implementation derives recovery output from `B = (N + 1) × C × R − N × A`, where N is the residue accumulation count (10), C is the steam variant coefficient, R is regular cracked output, and A is half of R. These comparisons use the same feedstock and severity for the full cycle; mixed-feed residue pooling does not guarantee the same ratio for each individual product.

This feature does not add Chemical Reactor, moderate gas-cracking, basic-steam, or dense-steam routes. Existing ordinary steam, hydrogen cracking, and distillation are unchanged.

## WIP save compatibility

The unused prototype custom lightly/severely critical-steam-cracked materials have been removed. Back up development worlds containing these prototype fluids before loading this version: their registry IDs no longer exist, and there is no automatic migration of stored prototype fluid stacks. Reverting after loading cannot restore discarded stacks without the backup. Published steam registry IDs remain unchanged.

## Verification

Verified on 2026-10-08 against pinned GTCEu 7.5.1:

- `./gradlew test --rerun-tasks --no-daemon`: 53 tests passed, including 16 cracking-yield cases.
- `./gradlew build spotlessCheck compileCrackingTestJava --no-daemon`: passed.
- `./gradlew runData --no-daemon`: all providers and HashCache completed; both residue translations are present, obsolete prototype translations are absent, and no stale resources were removed. The data JVM remained alive after generation and was terminated by a 100-second timeout; this was not a clean Gradle shutdown.
- `./gradlew runServer -PcrackingTest --no-daemon`: 81 recorded checks passed in an isolated world, using normal recipe search and accelerated recipe ticks. The fixture verifies all 32 emitted/loaded recipes, exact cycles for both variants/severities, insufficient-residue and blocked-output handling, standard recipe modifiers, and actual distillery processing of outputs from all 16 initial recipes. It also compares 309 pinned upstream petrochemical recipe definitions with loaded recipes.
- `xvfb-run -a ./gradlew runClient -PcrackingTest --no-daemon`: both residue material/fluid names, IDs, and colors passed client checks.

The opt-in fixtures live under `src/crackingTest` and are not shipped in the production mod. Reports are written to `build/cracking-test/cracking-server.json`, `cracking-client.json`, and `cracking-recipes.json`. These are automated loader/machine checks, not a manual JEI walkthrough.
