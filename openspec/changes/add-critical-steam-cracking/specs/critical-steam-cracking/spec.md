# Spec Delta

## Purpose

Enable two-stage oil-product cracking with critical custom steam, providing faster initial processing and recoverable residue that increases total yield without introducing new cracked-fluid distillation chains.

## ADDED Requirements

### Requirement: Supported cracking combinations
The addon SHALL provide light and severe Cracking Unit recipes for Light Fuel, Heavy Fuel, Naphtha, and Refinery Gas, using either non-dense Supercritical Steam or non-dense Most Hellish Steam, and corresponding residue recipes. Each combination SHALL output the matching existing GTCEu steam-cracked fluid, not a new cracked-fluid variant.

#### Scenario: Complete recipe matrix
- **WHEN** addon recipes are registered
- **THEN** each of the two steam variants has critical-steam and residue recipes for each of the four feedstocks and two severities
- **AND** there are 16 critical-steam recipes and 16 residue recipes with distinct addon-owned IDs
- **AND** Heavy Fuel maps to steam-cracked Heavy Fuel, Light Fuel to steam-cracked Light Fuel, Naphtha to steam-cracked Naphtha, and Refinery Gas to steam-cracked Gas, with matching severity

### Requirement: Initial critical-steam processing
A critical-steam recipe SHALL preserve the corresponding regular Cracking Unit steam recipe's feedstock amount, cracking-agent amount, circuit, and EU/t, substituting the selected critical steam. It SHALL halve duration and regular cracked-fluid output, and additionally produce exactly 100 mB of that steam variant's residue.

#### Scenario: Light critical-steam cracking
- **WHEN** any supported feedstock is lightly cracked with either supported critical steam
- **THEN** the recipe consumes 1,000 mB feedstock and 1,000 mB selected critical steam, uses circuit 1, runs for 40 ticks at 240 EU/t, and outputs 500 mB matching lightly steam-cracked fluid plus 100 mB matching residue

#### Scenario: Severe critical-steam cracking
- **WHEN** any supported feedstock is severely cracked with either supported critical steam
- **THEN** the recipe consumes 1,000 mB feedstock and 1,000 mB selected critical steam, uses circuit 3, runs for 80 ticks at the regular severe steam-cracking EU/t, and outputs 500 mB matching severely steam-cracked fluid plus 100 mB matching residue
- **AND** recipe EU/t is not additionally reduced; the base energy saving comes from halving duration

### Requirement: Residue processing
A residue recipe SHALL consume 1,000 mB of one supported residue plus another regular-sized feedstock batch, use the corresponding regular steam-cracking circuit, duration, and EU/t, and produce only the matching existing steam-cracked fluid at the calculated boosted yield. It SHALL require neither fresh steam nor hydrogen and SHALL produce no residue.

#### Scenario: Light residue cracking
- **WHEN** any supported feedstock is lightly cracked with 1,000 mB matching residue
- **THEN** the recipe also consumes 1,000 mB feedstock, uses circuit 1, runs for 80 ticks at 240 EU/t, and produces 11,500 mB lightly steam-cracked fluid for Supercritical residue or 13,700 mB for Most Hellish residue

#### Scenario: Severe residue cracking
- **WHEN** any supported feedstock is severely cracked with 1,000 mB matching residue
- **THEN** the recipe also consumes 1,000 mB feedstock, uses circuit 3, runs for 160 ticks at the regular severe steam-cracking EU/t, and produces 11,500 mB severely steam-cracked fluid for Supercritical residue or 13,700 mB for Most Hellish residue

#### Scenario: Insufficient residue
- **WHEN** a Cracking Unit has sufficient feedstock but less than 1,000 mB residue
- **THEN** it cannot start the residue recipe

### Requirement: Eleven-craft yield accounting
The addon SHALL calculate residue yield from the regular recipe output R, initial output A = R / 2, residue accumulation count N = 1,000 / 100, and the steam variant coefficient C, using B = (N + 1) × C × R − N × A. C SHALL be 1.5 for SUPERHOT and 1.7 for HELLISH. Yield calculations SHALL preserve exact integer-mB results for the supported recipes, rather than storing separate hardcoded boosted yields per recipe.

#### Scenario: Supercritical cycle
- **WHEN** ten identical Supercritical Steam cracking crafts are followed by one residue craft of the same feedstock and severity
- **THEN** ten crafts produce 5,000 mB cracked fluid and 1,000 mB residue, the residue craft consumes that residue and an eleventh 1,000 mB feedstock batch to produce 11,500 mB cracked fluid, and total cracked output is 16,500 mB versus 11,000 mB for eleven regular crafts

#### Scenario: Most Hellish cycle
- **WHEN** ten identical Most Hellish Steam cracking crafts are followed by one residue craft of the same feedstock and severity
- **THEN** total cracked output is 5,000 + 13,700 = 18,700 mB versus 11,000 mB for eleven regular crafts, and no residue remains

#### Scenario: Coefficient affects both severities
- **WHEN** light and severe yields are calculated for one steam variant
- **THEN** both use the same variant coefficient, baseline-dependent formula, and residue accumulation count

### Requirement: Residue identity and properties
The addon SHALL register one liquid cracking residue per supported critical-steam variant, shared across its four feedstocks and two severities. Both residues SHALL have regular GTCEu steam temperature (373 K for the pinned dependency), explicit displayed names, and the approved purple colors.

#### Scenario: Supercritical residue metadata
- **WHEN** Supercritical Steam Cracking Residue is inspected
- **THEN** its registry ID is `gttruesteam:supercritical_steam_cracking_residue`, its displayed name is `Supercritical Steam Cracking Residue`, its RGB color is `#9666CC`, its state is liquid, and its temperature is 373 K

#### Scenario: Most Hellish residue metadata
- **WHEN** Most Hellish Steam Cracking Residue is inspected
- **THEN** its registry ID is `gttruesteam:most_hellish_steam_cracking_residue`, its displayed name is `Most Hellish Steam Cracking Residue`, its RGB color is `#663399`, its state is liquid, and its temperature is 373 K

#### Scenario: Shared but distinct residues
- **WHEN** residue is accumulated from different feedstocks or severities using the same critical steam
- **THEN** it stacks as the same residue and can fuel any supported residue recipe of that variant
- **AND** Supercritical and Most Hellish residues remain separate fluids with separate boosted-yield coefficients

### Requirement: Preserve existing processing routes
The addon SHALL leave existing GTCEu steam/hydrogen cracking and distillation recipes unchanged. New recipes SHALL be exclusive to the Cracking Unit and the two non-dense critical variants. The unused WIP addon-specific cracked fluids SHALL be removed rather than used as outputs; existing released steam registry IDs SHALL remain unchanged.

#### Scenario: Unchanged ordinary routes
- **WHEN** existing regular steam, hydrogen, or cracked-fluid distillation recipes are inspected
- **THEN** their inputs, outputs, circuits, duration, EU/t, and IDs are unchanged
- **AND** addon recipe outputs can directly enter the existing matching distillation routes

#### Scenario: Excluded variants and recipe families
- **WHEN** new addon cracking recipes are inspected
- **THEN** none consumes dense critical steam, basic custom steam, or dense basic steam
- **AND** no new Chemical Reactor or moderate cracking recipe is added

#### Scenario: Prototype material cleanup
- **WHEN** addon materials and generated localization are inspected
- **THEN** the unused WIP custom lightly/severely critical-steam-cracked materials and their obsolete localization entries are absent
- **AND** the two residues have readable localized names with correctly separated words
