# Proposal

## Why

Multiblock-specific operating values cannot currently drive configurable redstone automation through an addon-owned hatch. A typed controller interface and ordered rules let players automate boiler heat and pressurizer readiness without coupling the hatch to individual machine implementations.

## What Changes

- Add LV, MV, HV, EV, IV, and LuV redstone output hatches, supporting respectively 1, 2, 3, 4, 5, and 6 rules.
- Provide a controller interface exposing stable value identifiers, display labels, types, and current values: integer, float, string, and boolean.
- Evaluate rules in UI order; the first matching rule determines output strength 0–15, including a matching zero-strength rule. No match or unavailable controller produces zero.
- Support six numeric comparisons (<, >, ==, <=, >=, !=), exact float equality, string equality/inequality, and boolean true/false checks.
- Provide a hatch UI for adding, editing, deleting, and reordering rules, with type-specific controls and server-side validation.
- Emit from the oriented front face only; persist configuration and safely clear output when detached or invalidated.
- Expose the Infernal Boiler's integer heat counter and string heat level, and the Industrial Gas Pressurizer's boolean perfect-condition state.
- Permit at most one optional hatch per supported multiblock, replacing its primary casing material in its pattern; do not require a hatch for existing structures.
- Add tier registrations, models, localized UI/tooltips, survival recipes, and verification coverage.

## Capabilities

### New Capabilities

- `multiblock-observable-values`: Typed controller value contract and initial boiler/pressurizer integrations.
- `tiered-redstone-hatches`: Tiered pattern parts, ordered rule evaluation, UI, persistence, and front-face redstone behavior.

### Modified Capabilities

None. The project currently has no existing capability specifications.

## Impact

- New API types under `site.siredvin.gttruesteam.api` and hatch implementation under `machines`.
- Changes to `TrueSteamMachines`, `TrueSteamRecipes`, `TrueSteamLang`, machine assets, and generated resources.
- Changes to `InfernalBoilerMachine`, `IndustrialGasPressurizerMachine`, and their pattern definitions; existing recipe/heat/readiness semantics remain unchanged.
- Retain pinned Java 17, Minecraft 1.20.1, Forge 47.4.10, GTCEu 7.5.1, and LDLib 1.0.40.b. No dependency upgrade or general-purpose scripting engine is proposed.
- Planning only in this workflow; implementation begins only after explicit approval through the apply workflow.
