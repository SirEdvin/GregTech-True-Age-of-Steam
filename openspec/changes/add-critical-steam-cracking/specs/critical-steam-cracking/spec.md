# Spec Delta

## Purpose

Provide a three-stage critical-steam cracking chain: custom cracked fluids distill into reduced regular products and reusable residue, whose recovery raises total final-product yields.

## ADDED Requirements

### Requirement: Complete processing matrix
The addon SHALL support Light Fuel, Heavy Fuel, Naphtha, and Refinery Gas, each lightly and severely cracked with non-dense Supercritical Steam or Most Hellish Steam. It SHALL provide 16 initial cracking recipes, 16 custom Distillation Tower recipes, and 16 residue recovery recipes with distinct addon-owned IDs.

#### Scenario: Matching products
- **WHEN** any supported feedstock/variant/severity combination is processed
- **THEN** initial cracking produces its distinct custom cracked fluid
- **AND** custom distillation uses the corresponding regular steam-cracked distillation product identities
- **AND** recovery produces the corresponding ordinary GTCEu steam-cracked fluid, with Refinery Gas mapped to cracked Gas

### Requirement: Initial critical-steam cracking
Initial cracking SHALL consume 1,000 mB feedstock and 1,000 mB critical steam, preserve the regular Cracking Unit circuit and EU/t, halve duration, and produce only 1,000 mB matching custom cracked fluid. It SHALL NOT produce residue directly.

#### Scenario: Light cracking
- **WHEN** a supported light initial recipe runs
- **THEN** it uses circuit 1, 40 ticks and 240 EU/t and produces 1,000 mB matching lightly critical-steam-cracked fluid

#### Scenario: Severe cracking
- **WHEN** a supported severe initial recipe runs
- **THEN** it uses circuit 3, 80 ticks and regular severe cracking EU/t and produces 1,000 mB matching severely critical-steam-cracked fluid
- **AND** ordinary coil/overclock modifiers apply

### Requirement: Reduced-yield custom distillation
Custom Tower distillation SHALL consume 2,000 mB custom cracked fluid and produce the fluid amounts of one corresponding regular 1,000 mB distillation batch plus 200 mB variant residue. Its expected solid yield SHALL equal one regular batch's expected solid yield, giving 50% of every regular product per equivalent input volume without fractional-mB rounding.

#### Scenario: Odd fluid output
- **WHEN** 2,000 mB lightly critical-steam-cracked Heavy Fuel is distilled
- **THEN** it produces 3 mB Propane rather than rounding a hypothetical 1.5 mB output per 1,000 mB
- **AND** every other fluid product equals its regular one-batch amount, plus 200 mB residue

#### Scenario: Chance-based carbon
- **WHEN** a 2,000 mB custom distillation batch runs
- **THEN** its Carbon quantity and drop probability match one regular 1,000 mB batch
- **AND** its expected Carbon output is half that of regular processing of the same 2,000 mB volume
- **AND** no exact Carbon count is promised for an individual cycle

#### Scenario: Residue origin and output safety
- **WHEN** custom cracked fluid is distilled
- **THEN** residue is produced only by the custom distillation recipe, not initial cracking or separate single-fraction recipes
- **AND** insufficient input or blocked product/residue storage prevents processing without losing inputs
- **AND** the addon adds no height/hatch guard; missing output layers and voiding retain ordinary GTCEu behavior

### Requirement: Residue recovery
Recovery SHALL consume 1,000 mB feedstock and 1,000 mB shared variant residue, with no fresh steam/hydrogen. It SHALL preserve regular cracking circuit, duration and EU/t, produce only ordinary matching steam-cracked fluid, and emit no residue.

#### Scenario: Recovery amounts
- **WHEN** Supercritical or Most Hellish residue is used in a supported recovery recipe
- **THEN** it produces respectively 11,500 or 13,700 mB ordinary steam-cracked fluid
- **AND** light uses circuit 1/80 ticks/240 EU/t; severe uses circuit 3/160 ticks/regular severe EU/t

#### Scenario: Insufficient residue
- **WHEN** less than 1,000 mB residue is available
- **THEN** recovery cannot start and inputs remain unconsumed

### Requirement: Final-product balance
For same-feedstock/same-severity cycles, ten initial batches and their five custom distillations plus one recovery batch SHALL yield 1.5 times (SUPERHOT) or 1.7 times (HELLISH) every regular final distillation product relative to eleven ordinary batches. Fluid accounting SHALL be exact over whole executable batches; solid accounting SHALL use expected yield. Recovery quantities SHALL be derived, not independently tuned.

#### Scenario: Product-by-product accounting
- **WHEN** a regular 1,000 mB distillation batch yields product quantity Y
- **THEN** the initial ten-batch custom route yields 5Y
- **AND** recovery contributes 11.5Y or 13.7Y after ordinary distillation, giving 16.5Y or 18.7Y versus the baseline 11Y
- **AND** fractional ordinary input batches are retained as fluid, not rounded or discarded; full-cycle verification uses enough repeated cycles to distill complete batches

#### Scenario: Pooled residues
- **WHEN** residue from different feeds/severities within one variant is combined
- **THEN** it can fuel any supported recovery of that variant
- **AND** per-product coefficients apply to same-feed/same-severity comparisons, not arbitrary mixed-feed cycles

### Requirement: Custom fluid identities and properties
The addon SHALL register 16 distinct custom cracked fluids with readable severity/critical-steam/feedstock names, the corresponding regular cracked fluid's state and temperature, and a color blended from that fluid and the critical steam. Registry identifiers SHALL separate words correctly and belong to gttruesteam.

#### Scenario: Hellish Light Fuel identity
- **WHEN** Light Fuel is lightly cracked with Most Hellish Steam
- **THEN** its output is named Lightly Most Hellish Steam Cracked Light Fuel, not Lightly Steam Cracked Light Fuel
- **AND** its state/temperature match regular lightly steam-cracked Light Fuel and its color incorporates Most Hellish Steam

### Requirement: Residue properties
The addon SHALL retain one distinct liquid residue per critical steam, shared across feeds/severities, explicitly set to 373 K with approved names and colors.

#### Scenario: Approved residues
- **WHEN** residues are registered
- **THEN** gttruesteam:supercritical_steam_cracking_residue is named Supercritical Steam Cracking Residue with RGB #9666CC
- **AND** gttruesteam:most_hellish_steam_cracking_residue is named Most Hellish Steam Cracking Residue with RGB #663399
- **AND** both are liquids at 373 K

### Requirement: Preserve ordinary routes and scope
The addon SHALL leave existing GTCEu steam/hydrogen cracking and distillation IDs and definitions unchanged, preserve released steam IDs, and exclude dense/basic custom steam, Chemical Reactor cracking and moderate cracking. Custom distillation SHALL add routes for custom fluids without overriding ordinary routes.

#### Scenario: Ordinary processing remains available
- **WHEN** ordinary cracking and distillation recipes are inspected
- **THEN** their original inputs/outputs/circuits/duration/EU/t remain unchanged
- **AND** residue recovery output can use ordinary distillation

#### Scenario: Prototype compatibility
- **WHEN** corrected custom materials are registered
- **THEN** malformed unused WIP prototype IDs are not silently treated as migrated fluids
- **AND** documentation warns to back up prototype-containing worlds and distinguishes these IDs from preserved released steam IDs

### Requirement: Discover GTCEu-generated recipe definitions
The addon SHALL derive supported cracking and distillation recipes from GTCEu's native recipe-build callbacks rather than duplicate upstream quantities, circuits, duration, EU/t, fluid-product tables, or solid chance metadata in production code. It SHALL retain explicit supported-material mappings, stable addon recipe IDs, existing native callbacks, and reload-safe generation without mutating source definitions. Adaptation to subsequent datapack/KubeJS overrides SHALL remain outside this requirement.

#### Scenario: Changed generated source definition
- **WHEN** a supported GTCEu-owned recipe builder changes its circuit, Steam amount, duration, EU/t, distillation products, or Carbon chance
- **THEN** derived recipes inherit those source properties except for the explicitly required stage transformations
- **AND** source definitions and ordinary Distillery generation remain unchanged

#### Scenario: Repeated generation and excluded sources
- **WHEN** GTCEu recipe generation repeats or unsupported, hydrogen, or addon-owned recipes are built
- **THEN** the normal source set regenerates the same 48 unique addon recipes without recursive or accumulated additions
- **AND** excluded sources do not generate additional critical-steam recipes
