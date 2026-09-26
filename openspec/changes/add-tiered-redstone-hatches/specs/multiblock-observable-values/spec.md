# Spec Delta

## Purpose

Expose typed, stable multiblock operating values so configurable redstone automation can observe supported controllers without knowing their implementation.

## ADDED Requirements

### Requirement: Typed controller value contract
Custom controller observations SHALL implement an interface exposing a list of observable values, each with a unique stable identifier, display label, declared type, and readable current value. Shared recipe observations SHALL be available on recipe-capable controllers without requiring this custom interface. Supported types SHALL be integer, float, string, and boolean. Reads SHALL be observational and SHALL NOT modify machine operation. Unavailable values SHALL be distinguishable from valid zero, empty string, or false values. Identifiers and types SHALL NOT depend on display language.

#### Scenario: Discover and read typed values
- **WHEN** a hatch is attached to a formed supporting controller
- **THEN** it can enumerate the controller's values and read values matching their declared types using stable identifiers

#### Scenario: An unavailable value is not false
- **WHEN** an exposed boolean value is unavailable
- **THEN** it is not treated as boolean false and neither a true nor false check matches it

### Requirement: Infernal Boiler observables
The Infernal Boiler SHALL expose `heat_counter` as its existing integer heat counter, `heat_level` as the stable string name of its existing effective heat level, and integer `cycles_until_throttle` as its existing remaining infernal charges. Reading these values SHALL preserve current heat thresholds, decay, and recipe behavior; named levels SHALL be NONE, BASIC, ADVANCED, PROGRESSIVE, or SUPREME as returned by existing heat-level behavior.

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

### Requirement: Shared recipe observations
Any formed recipe-capable multiblock accepting the hatch SHALL expose integer `recipe_progress_ticks`, integer `recipe_duration_ticks`, and string `recipe_id`, alongside custom observations. Progress and duration SHALL use effective recipe logic ticks. The recipe ID SHALL be namespaced and represent the current recipe, not an idle cached recipe. Idle progress and duration SHALL be zero and idle recipe ID SHALL be unavailable. These shared identifiers SHALL be reserved and override colliding custom identifiers. This contract SHALL NOT expand pattern eligibility.

#### Scenario: Recipe-capable controller without custom interface
- **WHEN** the hatch is attached to a formed recipe-capable controller without custom observations
- **THEN** shared recipe observations are available

### Requirement: Recipe progress percentage
Recipe-capable controllers SHALL additionally expose FLOAT `recipe_progress_percent`, computed as 100 times current progress divided by effective duration and clamped to 0–100. Fractional percentages SHALL be preserved. Idle recipes and nonpositive durations SHALL report 0%, without division by zero. Tick observations SHALL remain available unchanged.

#### Scenario: Fractional progress
- **WHEN** an active recipe has progressed 1 tick of an effective 200-tick duration
- **THEN** percentage progress is 0.5 and decimal comparison thresholds are supported

#### Scenario: Idle percentage
- **WHEN** recipe logic is idle or reset
- **THEN** percentage progress is 0 rather than cached progress

#### Scenario: Idle recipe
- **WHEN** the controller has no current recipe
- **THEN** progress and duration read zero and recipe ID is unavailable
