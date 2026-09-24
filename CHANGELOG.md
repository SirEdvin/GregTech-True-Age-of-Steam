# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added

- LV–LuV Redstone Hatches with 1–6 rules, configurable output strengths from 0 to 15, and front-face-only output without energy consumption. All matching strengths are combined with bitwise XOR; order does not matter. No match or disconnected controller outputs zero.
- Infernal Boilers expose integer `heat_counter`, integer `cycles_until_throttle`, and string `heat_level` (`NONE`, `BASIC`, `ADVANCED`, `PROGRESSIVE`, `SUPREME`). Industrial Gas Pressurizers expose boolean `perfect_condition` with true/false checks.
- Any accepting recipe-capable multiblock exposes `recipe_progress_ticks`, `recipe_duration_ticks`, and `recipe_id`. Idle progress/duration are zero; an idle recipe ID is unavailable.
- Recipe progress is also available as `recipe_progress_percent` (0–100%, with fractional thresholds); idle or nonpositive-duration recipes report 0%.
- Each supported multiblock accepts one optional redstone hatch in a primary casing position (Infernal Alloy Casing or Clean Stainless Steel Casing, including existing casing alternatives). Other blocks and mandatory hatch positions are unchanged.
- Hatch editors support numeric comparisons and case-sensitive string equality/inequality in independent rule widgets with per-rule Save/Delete, without selecting a rule first. Craft each tier using its machine hull, circuit, and a comparator.

### Fixed

- Concept Infusion Matrix now no longer consider paused machine as running
- Fresh redstone hatch editors can save the first rule directly without requiring Add first.
- Redstone hatches inherit their multiblock's casing appearance when formed and restore the tier hull when detached.

## [0.3.3] - 2029-09-05

### Fixed

- Circuits for combined cometal recipes

## [0.3.2] - 2026-09-01

### Change

- In most multis now insides use any instead of air predicate

## [0.3.1] - 2026-07-11

### Fixed

- Recipe collision for concepts
- Recipe for heating boilers

## [0.3.0] - 2026-05-01

### Added

- Concept infusion matrix multiblock
- Industrial gas pressurizer multiblock
- Compression concept material
- Extraction concept materials
- Heating concept materials
- Cooling concept materials
- Polarization concept materials
- Hellish waster and hellish steam
- Documentation (!)
- Dense steam variants
- Hellish steam
- Supercritical steam variants
- New infernal boiler textures (Thanks to MindBrain27)
- Alternative boiler charging fluid based on nether trees.
- Full loop boiler charging without any material loss
- New cooling coils from cooling cometal
- Update for infernal boiler in form of beating husks
- Better purified infernal dust recipes
- Buff boilers a little and introduce tier 3 for boilers
- Spawner extraction machine
- KubeJS plugin for generating new cooling coils
- Fluid cooling recipes
- Cooling tower
- Industrial coating line

### Changed

- RCC now use EU-free parallels per coil level
- Infernal boiler now correctly uses parallels in supreme heat state.
- Estranged metal now produces tier 3 coils and requires cooling cometal
- Cooling coil tooltip is not more informative

### Fixed

- Endlessly escalating distilled water loop'
- Husk of the boiler texture
- Issue with coating shrine rendering clipping

## [0.2.3] - 2026-03-01

### Changed

- Infernal boiler overheating function change to make it scaling not so good, so it slowly become useless at IV

## [0.2.2] - 2026-02-25

### Fixed

- RCC definition

## [0.2.1] - 2026-02-24

### Fixed

- Condition for boiler heat to going down
- Purified infernal dust stack count

## [0.2.0] - 2026-02-24

### Added

- Infernal alloy fluid pipes to handle temperature of blaze-related fluids
- Infernal drum to handle temperature of blaze-related fluids
- Cooling coils now have new, currently unused parameter
- Infernal circuit
- New multiblock: Regulated Cryo Chamber
- Second cooling coil
- Some new materials

### Changed

- Secondary colors of most of alloy
- Blaze-related fluids now have same temperature as liquid blaze

## [0.1.2] - 2026-02-21

### Added

- Purified infernal dust for future use

### Fixed

- Appearance block for infernal boiler
- Rendering logic for coating shrine ingots 
