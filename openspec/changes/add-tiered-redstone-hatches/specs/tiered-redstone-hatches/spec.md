# Spec Delta

## Purpose

Let players configure tier-limited, ordered multiblock conditions that emit front-face redstone signals for machine automation.

## ADDED Requirements

### Requirement: Tiered hatch availability and capacity
The system SHALL provide LV, MV, HV, EV, IV, and LuV redstone hatches with respective maximum rule counts of 1, 2, 3, 4, 5, and 6. Each tier SHALL have a distinguishable name/model, capacity tooltip, and survival crafting recipe. Hatches SHALL operate without separate energy consumption; tier controls rule capacity.

#### Scenario: Capacity is enforced
- **WHEN** a hatch already has its tier's maximum rule count
- **THEN** adding another rule is disabled in the UI and rejected by the server, while existing rules remain editable and removable

### Requirement: Optional pattern attachment
The Infernal Boiler and Industrial Gas Pressurizer SHALL each accept zero or one redstone hatch of any supported tier as a regular pattern part replacing primary casing. For the boiler this means positions accepting Infernal Alloy Casing; for the pressurizer this means positions accepting Clean Stainless Steel Casing. The limit SHALL apply across all eligible positions and all tiers together. Coils, pipes, turbine casings, frames, glass, controllers, and mandatory hatch positions SHALL NOT become eligible merely through this feature. Adjacency outside the pattern SHALL NOT attach a hatch.

#### Scenario: Backward-compatible structure
- **WHEN** an otherwise valid existing structure has no redstone hatch
- **THEN** it still forms without any new requirement

#### Scenario: Single optional hatch
- **WHEN** one eligible primary casing position is replaced by an LV through LuV redstone hatch and other existing requirements remain satisfied
- **THEN** the structure forms and the hatch observes that controller

#### Scenario: Global limit across positions
- **WHEN** two eligible primary casing positions contain redstone hatches, even in different pattern position categories or different tiers
- **THEN** structure formation fails

### Requirement: Typed rule comparisons
Each rule SHALL select one exposed value, a valid comparison, and an integer output strength from 0 through 15 inclusive. Integer and float values SHALL support <, >, ==, <=, >=, and !=. Numeric comparisons SHALL use numeric rather than lexical ordering; float equality SHALL be exact without tolerance, with positive and negative zero equal. Nonfinite floats SHALL be invalid. Strings SHALL support only == and != using exact case-sensitive comparison. Booleans SHALL support only is true and is false without a separate operand. Integer operands SHALL reject fractions and overflow. Missing values, changed types, or incompatible operators SHALL make a rule invalid and nonmatching, including inequality rules.

#### Scenario: Numeric boundaries
- **WHEN** an integer or finite float equals a rule's operand
- **THEN** ==, <=, and >= match, while <, >, and != do not

#### Scenario: String comparison
- **WHEN** a string value is BASIC and its operand is basic
- **THEN** equality does not match and inequality matches

#### Scenario: Boolean controls
- **WHEN** a boolean value is selected
- **THEN** only is true and is false are offered, and each matches only its respective available boolean value

#### Scenario: Invalid operand or missing value
- **WHEN** a rule has an unparseable operand, nonfinite float, missing value, or incompatible type
- **THEN** it does not match and the UI identifies the invalid or unavailable rule

### Requirement: Bitwise XOR output
Every rule within tier capacity SHALL be evaluated against current controller observations, with each distinct value read at most once per evaluation. The output SHALL be the bitwise XOR of all valid matching rule strengths. Nonmatching or invalid rules SHALL contribute zero. When no rule matches, output SHALL be zero. Evaluation SHALL occur on the server at least once per server tick while attached to a loaded formed controller, independently of whether its recipe is active. Reordering SHALL NOT affect output.

#### Scenario: Matching strengths combine
- **WHEN** two matching rules request strengths 15 and 3
- **THEN** the output is their bitwise XOR, 12

#### Scenario: Zero does not terminate evaluation
- **WHEN** a matching rule requests strength 0 and another matching rule requests a positive strength
- **THEN** the output equals the positive strength

#### Scenario: Equal strengths cancel
- **WHEN** exactly two matching rules request the same strength
- **THEN** the output is zero

#### Scenario: No match or inactive machine
- **WHEN** no rule matches
- **THEN** output is 0, and the hatch continues observing a formed idle machine for future matches

### Requirement: Editable ordered rule UI
The hatch UI SHALL display connection status, exposed value labels/types/current values when available, ordered rules, capacity, and current output. It SHALL support adding, editing, deleting, and moving rules up or down. Controls SHALL reflect the selected value's type. Valid edits SHALL take effect no later than the next server tick. Unattached hatches SHALL show their disconnected status and retained configuration without inventing available values.

#### Scenario: Reordering is organizational
- **WHEN** a player moves a lower matching rule above another matching rule
- **THEN** the visible order updates and output remains unchanged

#### Scenario: Save the first rule
- **WHEN** a player opens a connected empty hatch, selects heat counter greater than 15, and saves
- **THEN** the first rule is stored without requiring a separate Add or row-selection action

#### Scenario: Invalid client mutation
- **WHEN** a client submits an out-of-range strength, excess rule, incompatible operator, invalid operand, or out-of-bounds reorder
- **THEN** the server rejects the mutation without corrupting the previous valid configuration

### Requirement: Front-face output and safe lifecycle
The hatch SHALL emit its selected redstone strength only through its oriented front face and SHALL notify affected neighbors when output or orientation changes. It SHALL emit zero when unformed, detached, unsupported, or when no controller value can be used. Detachment and structure invalidation SHALL clear active output immediately. Loading SHALL start from zero until a valid attachment has been re-established; no stale output SHALL be restored as authoritative state. Observation SHALL NOT force-load controller chunks.

#### Scenario: Directional signal
- **WHEN** a hatch emits strength 9
- **THEN** a redstone receiver at its front receives 9 and receivers at its other faces receive no signal from this hatch

#### Scenario: Rotation and invalidation
- **WHEN** a powered hatch rotates or its multiblock becomes invalid
- **THEN** the previous output face is notified and no longer retains the old signal, and an invalidated hatch emits zero

### Requirement: Durable and defensive configuration
Rule identifiers, types, operators, operands, output strengths, and ordering SHALL persist across chunk unload/reload and world restart. Malformed saved configuration SHALL NOT crash loading or bypass tier/type/strength constraints. Unknown values SHALL remain visibly unavailable rather than silently binding to another value by list position. Saved active output SHALL NOT override lifecycle safety.

#### Scenario: Reload ordered rules
- **WHEN** a configured hatch's chunk or world reloads
- **THEN** its valid rules and ordering are restored and output is recomputed only after attachment is valid

#### Scenario: Corrupt or excessive saved rules
- **WHEN** saved data contains invalid entries or more entries than the tier permits
- **THEN** loading remains safe, invalid entries cannot emit, and no rules beyond the tier limit participate in evaluation
