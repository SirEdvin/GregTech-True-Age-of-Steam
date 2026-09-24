# Spec Delta

## Purpose

Expose typed, stable multiblock operating values so configurable redstone automation can observe supported controllers without knowing their implementation.

## ADDED Requirements

### Requirement: Typed controller value contract
Supporting controllers SHALL implement an interface exposing a list of observable values, each with a unique stable identifier, display label, declared type, and readable current value. Supported types SHALL be integer, float, string, and boolean. Reads SHALL be observational and SHALL NOT modify machine operation. Unavailable values SHALL be distinguishable from valid zero, empty string, or false values. Identifiers and types SHALL NOT depend on display language.

#### Scenario: Discover and read typed values
- **WHEN** a hatch is attached to a formed supporting controller
- **THEN** it can enumerate the controller's values and read values matching their declared types using stable identifiers

#### Scenario: An unavailable value is not false
- **WHEN** an exposed boolean value is unavailable
- **THEN** it is not treated as boolean false and neither a true nor false check matches it

### Requirement: Infernal Boiler observables
The Infernal Boiler SHALL expose `heat_counter` as its existing integer heat counter and `heat_level` as the stable string name of its existing effective heat level. Reading these values SHALL preserve current heat thresholds, decay, and recipe behavior; named levels SHALL be NONE, BASIC, ADVANCED, PROGRESSIVE, or SUPREME as returned by existing heat-level behavior.

#### Scenario: Observe heating and cooling
- **WHEN** the formed boiler's heat counter changes through existing machine operation
- **THEN** subsequent observations return that current counter and the corresponding current named heat level

#### Scenario: Existing threshold behavior is preserved
- **WHEN** the heat counter is exactly at a named level's existing threshold
- **THEN** the exposed name equals the boiler's existing heat-level result rather than introducing new threshold semantics

### Requirement: Industrial Gas Pressurizer observable
The Industrial Gas Pressurizer SHALL expose `perfect_condition` as a boolean that is true exactly when its existing perfect-condition evaluation reports REACHED, and false for all other available states. Observation SHALL preserve existing cooldown, tank threshold, and recipe behavior.

#### Scenario: Ready pressurizer
- **WHEN** a formed pressurizer's existing evaluation reports REACHED
- **THEN** `perfect_condition` reads true

#### Scenario: Not-ready pressurizer
- **WHEN** a formed pressurizer reports cooldown, unreachable conditions, or unsuitable fluid levels
- **THEN** `perfect_condition` reads false
