# Critical-steam oil cracking

The Cracking Unit can crack Light Fuel, Heavy Fuel, Naphtha, and Refinery Gas with non-dense Supercritical Steam or Most Hellish Steam. This is a three-stage chain: custom cracked fluid, reduced-yield Tower distillation with residue, then boosted cracking using that residue.

## First stage

Each craft consumes 1,000 mB feedstock and 1,000 mB critical steam. It produces only 1,000 mB matching custom cracked fluid, such as **Lightly Most Hellish Steam Cracked Light Fuel**. It does not produce residue yet.

- Light: circuit 1, 40 ticks, 240 EU/t.
- Severe: circuit 3, 80 ticks, the same EU/t as regular severe steam cracking.

These base durations are half the corresponding regular steam recipe durations. EU/t is unchanged; the reduced duration provides the energy reduction. The normal Cracking Unit coil/overclock modifiers still apply.

Custom fluids inherit the corresponding regular cracked fluid's state and temperature (775 K in GTCEu 7.5.1). Cracked Refinery Gas is gaseous; the other three feeds produce liquids. Colors are equal-weight RGB blends of regular cracked fluid and critical steam, rounded to integer channels.

## Reduced-yield distillation

The Distillation Tower consumes **2,000 mB custom cracked fluid** and returns the products of one ordinary 1,000 mB steam-cracked distillation batch, plus **200 mB variant residue**. This is exactly 50% of ordinary product yield, and 100 mB residue per equivalent 1,000 mB input.

The doubled input avoids rounding fractional mB: for example, lightly cracked Heavy Fuel's 3 mB Propane remains 3 mB per doubled custom batch rather than rounding 1.5 mB per single batch. Base duration is 240 ticks and EU/t matches the regular Tower recipe (MV); normal modifiers apply.

Carbon uses the same rational drop probability as one regular batch. Because custom input is doubled, this already halves expected Carbon per input volume. Carbon totals are probabilistic, not guaranteed per cycle.

These custom recipes are Tower-only; no independent Distillery fraction recipes are generated. Provide an output hatch for each fraction, including residue: oil products have 12 fluid outputs, and Refinery Gas has 6. The addon adds no height/hatch guard and leaves ordinary GTCEu missing-output and voiding behavior unchanged.

## Residue recovery

A recovery craft consumes another 1,000 mB feedstock batch and 1,000 mB residue, with no fresh steam or hydrogen. It produces only the matching **ordinary GTCEu steam-cracked fluid**, ready for unchanged regular distillation:

| Residue | Cracked output | Color | Temperature |
| --- | --- | --- | --- |
| Supercritical Steam Cracking Residue | 11,500 mB | Purple `#9666CC` | 373 K |
| Most Hellish Steam Cracking Residue | 13,700 mB | Dark purple `#663399` | 373 K |

Both residues are liquids. Their registry IDs are `gttruesteam:supercritical_steam_cracking_residue` and `gttruesteam:most_hellish_steam_cracking_residue`.

Light recovery uses circuit 1, 80 ticks, 240 EU/t. Severe recovery uses circuit 3, 160 ticks, and regular severe steam-cracking EU/t. Make room for the large recovery output; recovery does not produce more residue.

Each steam variant has one shared residue across all four feedstocks and both severities. Residue can therefore be pooled and used with a different supported feedstock or severity; it does not remember where it came from.

## Total yield

Ten initial crafts produce 10,000 mB custom cracked fluid. Five custom distillations produce five regular batches' worth of final products plus exactly 1,000 mB residue. One recovery consumes that residue and an eleventh feedstock batch:

- SUPERHOT: five initial-route product batches plus 11.5 recovery product batches = 16.5 equivalent product batches versus 11 ordinary batches: **1.5× each final product**.
- HELLISH: five plus 13.7 = 18.7 equivalent product batches: **1.7× each final product**.

Recovery volume is derived from `B = 1000 × (11 × C − 5)`. The comparison endpoint is final distillation products, not intermediate cracked-fluid volume. Ordinary Tower recipes consume whole 1,000 mB batches, so retain the remainder from recovery for subsequent cycles. Ten full cycles consume all recovery fluid and give exact fluid-product ratios; solid-product ratios refer to expected Carbon yield. Comparisons use the same feedstock and severity; mixed-feed residue pooling does not guarantee the same ratio for each individual product.

This feature does not add Chemical Reactor, moderate gas-cracking, basic-steam, or dense-steam routes. Existing ordinary steam, hydrogen cracking, and distillation are unchanged.

## WIP save compatibility

Custom cracked fluids now have corrected IDs with separators, such as `gttruesteam:lightly_most_hellish_steam_cracked_light_fuel`; these are not aliases for the earlier malformed prototype IDs. Back up development worlds containing prototype fluids: there is no automatic migration of stored prototype stacks. Reverting after loading cannot restore discarded stacks without the backup. Published steam and approved residue IDs remain unchanged.

## Verification

Corrected three-stage chain verified on 2026-10-09 against pinned GTCEu 7.5.1:

- JUnit: 55 tests passed, including 18 cracking-yield cases and product-by-product whole-cycle accounting.
- `./gradlew build spotlessCheck compileCrackingTestJava --no-daemon`: passed.
- `./gradlew runData --no-daemon`: all providers and HashCache completed; 16 custom cracked-fluid and two residue translations are present in en_us/en_ud, and no stale resources were removed. The data JVM lingered and was terminated by an 80-second timeout; this was not a clean Gradle shutdown.
- `./gradlew runServer -PcrackingTest --no-daemon`: 176 recorded checks passed using normal recipe search and accelerated recipe ticks in an isolated world. The fixture checks 48 emitted/loaded recipes, exact Carbon metadata, unchanged upstream definitions, absence of custom Distillery routes, ten actual complete cycles for each of 16 feedstock/variant/severity combinations, all final fluid yields and retained remainders, and blocked/insufficient-input behavior in actual formed Cracking Unit and Tower machines.
- `xvfb-run -a ./gradlew runClient -PcrackingTest --no-daemon`: 18 checks passed for all custom and residue fluid names/IDs/colors, plus inherited custom state/temperature.

The opt-in fixtures live under `src/crackingTest` and are not shipped in the production mod. Reports are written to `build/cracking-test/cracking-server.json`, `cracking-client.json`, and `cracking-recipes.json`. These are automated loader/machine checks, not a manual JEI walkthrough.
