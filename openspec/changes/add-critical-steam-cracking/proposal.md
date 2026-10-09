# Proposal

## Why

The first implementation misread the intended chain: it produced ordinary cracked fluid and residue directly in the Cracking Unit. Correct it so critical steam produces a distinct cracked fluid, whose distillation sacrifices immediate product yield and releases residue for boosted recovery.

## What Changes

- Support the same four feedstocks (Light Fuel, Heavy Fuel, Naphtha, Refinery Gas), two severities, and two non-dense critical steam variants.
- Initial cracking preserves regular feedstock/agent amounts, circuits, EU/t and 1,000 mB output, halves cracking duration, and outputs only the matching custom critical-steam-cracked fluid.
- Register 16 custom cracked fluids with readable variant/severity/feedstock names, corresponding regular cracked-fluid state/temperature, and colors blended with critical steam.
- Add 16 Distillation Tower recipes consuming 2,000 mB custom cracked fluid. Each produces one regular 1,000 mB distillation batch's fluid products and expected solid byproduct, plus 200 mB shared variant residue: exactly 50% yield and 100 mB residue per 1,000 mB equivalent, without rounding odd fluid amounts.
- Keep residue recovery in the Cracking Unit: 1,000 mB residue plus 1,000 mB fresh feedstock produces 11,500 mB ordinary steam-cracked fluid for SUPERHOT or 13,700 mB for HELLISH.
- Measure the 1.5/1.7 coefficients on final distillation products, with solids measured in expected yield. Compare ten initial cracking batches, five custom distillations, and one recovery batch with eleven ordinary batches.
- Retain approved residue IDs/names/colors, liquid state, and builder temperature of 373 K. Residues pool across feedstocks/severities within their variant.
- Preserve all upstream steam/hydrogen cracking and distillation definitions; exclude dense variants, Chemical Reactor cracking, and moderate cracking. Custom distillation is Tower-only so residue cannot be duplicated through independent single-fraction routes.
- **BREAKING (WIP only):** corrected custom fluid IDs do not revive malformed prototype IDs or migrate stored prototype fluids. Back up development saves; released steam IDs remain unchanged.

## Capabilities

### New Capabilities

- `critical-steam-cracking`: Custom critical-steam cracking, reduced-yield residue-producing distillation, boosted residue recovery, material properties, and final-product cycle balance.

### Modified Capabilities

None. This revises the existing unarchived change, not an established main capability.

## Impact

- `SteamRecord`, configuration and steam definitions retain residues and custom cracked-material mappings in the existing lifecycle.
- `CriticalSteamCrackingRecipes` and focused distillation registration implement 32 Cracking Unit recipes plus 16 Tower recipes using pinned GTCEu 7.5.1 APIs.
- Revise yield tests, opt-in loader/machine fixtures, localization, and feature documentation. Previous passing evidence covers the incorrect chain, not this revised contract.
- PR #15 must be corrected with follow-up commits and fresh verification. No dependency upgrades, new machines, mixins, or upstream overrides are required.
