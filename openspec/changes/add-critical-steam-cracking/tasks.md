# Tasks

## 1. Material identities and configuration

- [x] 1.1 Retain approved SUPERHOT/HELLISH coefficients and residue IDs, names, colors and liquid builder temperature of 373 K. Existing source and prior loader/client checks establish these unchanged properties; no separate dynamic Steam-temperature equality check is required.
- [x] 1.2 Add retained light/severe custom cracked materials for all four feedstocks and both variants. Inspect pinned material/builder lifecycle, explicitly inherit corresponding regular state/temperature, blend RGB colors, and set readable names/correctly separated addon IDs. Verify 16 distinct custom fluids, correct Heavy Fuel/Gas mappings, all states/temperatures/colors/names, and unchanged published steam/residue IDs in a real loader fixture.
- [x] 1.3 Regenerate en_us/en_ud resources for custom materials and residues with `./gradlew runData --no-daemon`; verify readable names, obsolete prototype entry cleanup and no unrelated deletions. Update material/save-compatibility documentation and verify it distinguishes corrected IDs from malformed prototypes without promising migration.

## 2. Cracking and exact recovery accounting

- [x] 2.1 Change the 16 initial recipes to output only 1,000 mB corresponding custom cracked fluid, preserving baseline inputs/circuits/EU/t and half duration. Update emitted/loaded recipe assertions and verify no initial residue or ordinary cracked output, exactly 32 total addon Cracking Unit recipes, and normal modifiers.
- [x] 2.2 Retain 16 recovery recipes consuming 1,000 mB residue plus 1,000 mB feedstock and outputting ordinary cracked fluid. Reframe pure yield calculation/tests around final-distillation-equivalent yield; verify 11,500/13,700 mB outputs, coefficients, invalid/fractional/overflow inputs, and per-product rational eleven-batch accounting with fresh JUnit results.
- [x] 2.3 Update cracking/recovery documentation for the three-stage chain, residue pooling, final-product coefficients and retained fractional ordinary-distillation batches. Verify examples match emitted recipes and explicitly distinguish custom initial output from ordinary recovery output.

## 3. Custom distillation and reduced products

- [x] 3.1 Register 16 addon-owned Tower recipes with 2,000 mB custom input, the corresponding one-regular-batch fluid products, 200 mB variant residue, regular EU/t and scaled 240-tick duration. Use shared pinned product mappings and test all fluid identities/amounts, including odd upstream quantities; verify native output limits and sufficient Tower outputs without dropping products.
- [x] 3.2 Preserve exact upstream rational Carbon chance/stack in the doubled custom batch, yielding half expected carbon per input volume; test each relevant probability and zero chance boost. Verify per-product fluid and expected-solid coefficients over exact normalized cycles, without halving carbon twice or claiming deterministic carbon totals.
- [x] 3.3 Inspect and disable automatic custom Distillery fraction generation using supported GTCEu APIs so residue is not duplicated. Assert custom Tower matrix and absence of residue-producing single-fraction routes; compare ordinary steam/hydrogen/distillation definitions with pinned upstream source to verify no override.
- [x] 3.4 Document two-batch custom distillation, Tower-only residue production, output storage requirements and expected Carbon yield. Verify documentation examples against recipe assertions and remove obsolete claims that residue is created in the Cracking Unit or that no custom distillation exists.

## 4. Real integration and PR correction

- [x] 4.1 Extend the opt-in server fixture to form and run actual Cracking Unit/Tower processing for all four feedstocks, both severities and variants. Verify no residue at initial cracking, 200 mB per doubled custom distillation, residue transfer/recovery, blocked product/residue output, insufficient input/residue, standard modifiers, and matching ordinary downstream processing. Execute enough whole repeated cycles to check exact final fluid yields and retained remainders; verify Carbon analytically from loaded chance metadata.
- [x] 4.2 Extend client checks for all custom and residue fluid identities, readable localized names, colors/state/temperature. Run isolated client/server fixtures and verify fresh machine-readable passing reports; previous two-stage reports are not completion evidence.
- [x] 4.3 Run `./gradlew spotlessApply --no-daemon`, datagen, fresh JUnit tests and `./gradlew build spotlessCheck compileCrackingTestJava --no-daemon`; verify generated translations, production jar exclusion of fixtures and scoped diff. Record corrected evidence, reporting provider completion separately if datagen shutdown lingers, and run strict OpenSpec validation.
- [ ] 4.4 Commit/push scoped correction on feature/critical-steam-cracking and update existing PR #15 against main with corrected chain, test evidence and compatibility notes. Verify exact remote head/PR readback and CI status, leaving unrelated files/history untouched and not merging automatically.
