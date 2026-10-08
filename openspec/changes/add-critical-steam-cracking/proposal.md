# Proposal

## Why

Critical custom steam currently has no oil-cracking recipes: the WIP registers separate cracked materials without a usable recipe chain. Add a faster, residue-producing route that trades immediate yield for a larger total yield while retaining GTCEu's existing cracked fluids and downstream distillation.

## What Changes

- Add Cracking Unit recipes for non-dense Supercritical Steam (SUPERHOT) and Most Hellish Steam (HELLISH), covering Light Fuel, Heavy Fuel, Naphtha, and Refinery Gas with light and severe cracking.
- Preserve the corresponding regular steam-cracking feedstock and cracking-agent amounts, circuits, and EU/t; halve duration and output 50% of the regular cracked-fluid amount plus 100 mB of the variant's residue.
- Add residue recipes consuming another regular feedstock batch plus 1,000 mB residue, with regular duration, circuits, and EU/t, producing the corresponding existing steam-cracked fluid and no further residue.
- Dynamically calculate residue output so ten critical-steam crafts plus one residue craft yield exactly 1.5 times (SUPERHOT) or 1.7 times (HELLISH) the output of eleven equivalent regular steam-cracking crafts.
- Register two shared liquid residues with explicit names, approved purple colors, and regular steam temperature (373 K in GTCEu 7.5.1).
- Remove/rework unused WIP custom cracked-material registrations and regenerate their language output. **BREAKING (WIP only):** removal of those prototype registry IDs makes any prototype fluids already saved under them unavailable; released steam IDs remain unchanged.
- Leave ordinary steam, hydrogen cracking, dense variants, Chemical Reactor recipes, moderate gas cracking, and existing distillation unchanged.

## Capabilities

### New Capabilities

- `critical-steam-cracking`: Critical-steam and residue Cracking Unit processing, residue identity/properties, and balanced eleven-craft yield for the four supported oil products.

### Modified Capabilities

None. The current OpenSpec capability inventory is empty.

## Impact

- `common/SteamRecord.java`: replace unused cracked-pair registration with retained residue registration and integrate cracking recipes into the existing steam recipe lifecycle.
- `common/SteamConfiguration.java` and `TrueSteamSteams.java`: attach the yield coefficient to the steam variant and supply variant-specific residue metadata through the existing builder pattern.
- A small focused cracking recipe helper, if needed, owns feedstock/output pairing and shared yield arithmetic; no new recipe type or machine is required.
- Generated material language/assets and recipes under `src/generated/resources`, plus focused JUnit/data/runtime checks.
- Use the pinned GTCEu 7.5.1 `CRACKING_RECIPES` and existing light/severe steam-cracked materials; no dependency upgrades, upstream recipe overrides, mixins, or new distillation recipes.
