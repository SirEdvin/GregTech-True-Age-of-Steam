## Why

Future multiblocks need a reusable, persistent heat store and a way to exchange heat without one-way input/output hatches. Existing passive cooling is a separate recipe-specific resource and cannot express the requested ambient-to-maximum temperature model or physical vent connections.

## What Changes

- Add an interface and abstract workable multiblock base with stored heat in joules and abstract getters for structure-dependent heat capacity in joules and maximum safe temperature in kelvin. Temperature is read-only and derived as `300 + (storedHeat / heatCapacity) * (maxTemperature - 300)`, including above the safe threshold.
- Expose server-side heat addition/removal, but no public temperature, heat-capacity, or maximum-temperature setters. Heat cannot become negative, but additions are not capped at the structure's capacity. New machines start at zero stored heat and 300 K.
- Add one bidirectional heat hatch family with HV, EV, IV, and LuV variants, backed by its owning formed heat multiblock rather than independent hatch heat buffers, and a shared addon-owned part ability for future multiblock patterns. Tier transfer coefficients are respectively 2, 8, 32, and 128 joules per kelvin of temperature difference per transfer update, replacing the flat coefficient.
- Connect hatches through their designated front face using existing GTCEu heat vent blocks. Support face-adjacent turns and branches, no wireless or diagonal conduction, and an inclusive shortest-path limit of 32 adjacency steps between endpoint hatches.
- Transfer heat from hotter controllers to cooler controllers in temperature-difference-dependent packages using only the sending hatch's tier coefficient; the receiving hatch's tier does not throttle incoming heat. Conserve energy and limit transfer by equilibrium and donor heat, not receiver capacity. Each controller pair has its own package allowance; duplicate paths and multiple hatches must not multiply transfers for that same pair, while additional distinct peers can increase total transfer.
- Exceeding the structure's heat capacity starts reversible melting. If overheating continues for 40 game ticks, the controller and its own heat hatches explode, not all hatches on the vent network. Cooling back to or below the safe threshold stops melting and resets the countdown; subsequent overheating starts a fresh countdown. Reducing structure capacity below retained heat also triggers melting instead of discarding excess energy.
- Give cooling a final chance on the deadline tick: complete that tick's scheduled heat exchange before checking countdown expiry. Recovery within the safe limit cancels destruction and resets the countdown; a machine still overheated after exchange is destroyed when its existing interval expires. This does not postpone immediate destruction caused by dismantling a melting machine.
- Breaking a casing, the controller itself, or any attached heat hatch while its multiblock is melting immediately explodes that controller and its own heat hatches, without waiting for the remaining countdown. Breaking an external connecting vent only removes the affected heat-transfer path and does not trigger immediate destruction or cancel melting. Ordinary chunk unload and server shutdown are not destructive dismantling: preserve melting state and the remaining countdown, pause while unloaded/offline, and resume without resetting or offline catch-up.
- Provide a live, read-only hatch UI showing hatch tier and nominal transfer coefficient separately from owner temperature, stored heat, safe capacity, maximum safe temperature, melting/countdown state, and unavailable-owner state.
- Melting explosions destroy only the affected controller and its own heat hatches, with explosion sound effects. They do not damage surrounding blocks or entities, apply knockback, or start fires. This applies to both countdown expiry and immediate destruction during dismantling; no damaging blast radius or explosion-strength tuning is needed.
- Include persistence, lifecycle safety, focused tests, generated registration resources, and developer/modpack integration documentation.
- The controller owns and advances its countdown. Unloading another structure part, an owned hatch, or a vent path does not pause a still-ticking controller; only the controller's own unload or stopped server pauses its saved remaining time.

## Capabilities

### New Capabilities

- `multiblock-heat-storage`: Abstract machine/API, structure-dependent thermal characteristics, ambient-relative temperature derivation, server-side heat mutations, persistence, reversible melting with a delayed controller/owned-hatch explosion, immediate explosion on breaking a casing/controller/attached heat hatch during melting, and persistent pause/resume across unload/restart.
- `heat-hatch-network`: Hatch ownership, vent-path discovery, range, branching, conservative bidirectional transfer, and lifecycle behavior.
- `heat-hatch-ui`: Synchronized, localized read-only presentation of the owning multiblock's heat and melting state.

### Modified Capabilities

None. The main spec directory currently has no capability specs, and existing cooling, boiler heat levels, recipes, and multiblock patterns remain unchanged.

## Impact

- New Java types under `site.siredvin.gttruesteam.api` and `machines.shared.heat`, plus a multiblock-part implementation and heat transfer helpers.
- Extend existing registration and language owners (`TrueSteamMachines`, `TrueSteamLang`) and introduce the small addon-owned heat part-ability declaration. Add hatch resources through the project's registration/data-generation conventions.
- Reuse pinned Minecraft 1.20.1, Forge 47.4.10, Java 17, GTCEu 7.5.1, and LDLib 1.0.40.b; no dependency upgrades, new pipe implementation, global capability system, or mixins are planned.
- Use the actual `build.gradle.kts` ForgeGradle-based `site.siredvin.forge` setup, not the older Legacy ModDevGradle description in AGENTS.md. Focused test wiring may be added during implementation; no test source set contents or JUnit configuration were found in the inspected tree/build.
- This is infrastructure only: no existing multiblock adopts heat and no new production heat generator/consumer or recipe integration is introduced. The requested HV–LuV transfer tiers are in scope; survival hatch crafting recipes remain pack-provided. All four hatch variants will be obtainable through registration/creative access for integration. Tiering does not introduce electrical power consumption or voltage-overload behavior.
- Confirmed routing/throughput decisions: shortest connected hatch-to-hatch paths, front-face-only hatch connections, independent per-controller-pair packages, and sending-hatch HV/EV/IV/LuV coefficients of 2/8/32/128 J/(K * update). LuV sending to HV uses 128 J/(K * update); reversing the temperature ordering makes HV the sender and uses 2 J/(K * update). Receiver tier does not constrain either package. Vents have no heat storage/loss or passive ambient exchange. Direct front-to-front contact and the existing 20-tick transfer cadence remain proposal defaults.
- No extra portable-heat persistence on dropped controller items is in scope. Controller-owned countdown progression, last-chance cooling before same-tick deadline checks, targeted controller/own-hatch destruction with sound but no collateral blast effects, immediate destruction on breaking a casing/controller/attached heat hatch, external-vent disconnection without immediate explosion, and controller-unload/restart pause/resume are confirmed. Remaining lifecycle bookkeeping belongs to implementation and regression tests, not another round of product-design blockers. Tier coefficients are now requirements; gameplay verification must not silently retune them.
