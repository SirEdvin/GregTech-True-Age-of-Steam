## Purpose

Give players a live, read-only view of the heat state of the multiblock owning a heat hatch, with clear units and explicit unavailable-owner states.

## ADDED Requirements

### Requirement: The hatch exposes its owner's thermal values
Opening a valid heat hatch SHALL show the owning controller's current temperature in kelvin, current stored heat in joules, structure-dependent safe heat capacity in joules, and structure-dependent maximum safe temperature in kelvin. Labels SHALL distinguish current stored energy from safe capacity. Overcapacity heat and temperature SHALL be displayed truthfully rather than visually capped at safe limits. Display rounding SHALL NOT change authoritative values. Opening the hatch SHALL NOT require the player to open the controller UI or receive a chat-only message instead.

#### Scenario: Display the requested example
- **WHEN** a player opens a hatch attached to a controller storing 500 J of a 1000 J capacity with a 310 K maximum temperature
- **THEN** the UI shows 305 K, 500 J stored, 1000 J maximum capacity, and 310 K maximum temperature with unambiguous labels

### Requirement: Hatch tier and nominal conductance are visible
Each hatch UI SHALL identify its own HV, EV, IV, or LuV tier and corresponding nominal sending coefficient of 2, 8, 32, or 128 J/(K * update). Tier and nominal conductance SHALL be read-only hatch properties, visibly distinct from the owning controller's heat capacity in joules. The display SHALL identify the coefficient as applying when this hatch sends heat, not as a limit on incoming heat. The display MUST NOT promise that the full tier-limited amount is always transferred: donor-energy and equilibrium limits still apply. Tier information SHALL remain available even when owner thermal values are unavailable.

#### Scenario: View each tier
- **WHEN** a player opens an HV, EV, IV, or LuV heat hatch
- **THEN** the UI identifies that tier and its nominal coefficient with units independently of the controller's stored heat and capacity

#### Scenario: Different tiers share the same controller
- **WHEN** differently tiered hatches belong to the same controller
- **THEN** their owner thermal values agree after synchronization while their own tier/conductance labels reflect their respective variants

#### Scenario: Receiving from a higher-tier hatch
- **WHEN** an HV hatch receives heat from a LuV hatch
- **THEN** its own 2 J/(K * update) label remains identified as its sending coefficient and does not imply that incoming heat is throttled to that coefficient

### Requirement: The open UI follows server state
An open hatch UI SHALL update from server-authoritative controller state as heat, structure-dependent characteristics, or melting state changes, without reopening. Multiple hatches attached to the same controller SHALL present the same thermal state after synchronization. The UI MUST NOT depend on the client having the remote controller chunk loaded, and MUST NOT cause server-side chunk loading to obtain data.

#### Scenario: Heat changes while the menu is open
- **WHEN** network exchange or an authorized server heat mutation changes the controller's energy
- **THEN** the existing menu refreshes stored heat and derived temperature on its normal synchronization cycle

#### Scenario: Owner outside client tracking
- **WHEN** the hatch and its valid controller are loaded on the server but the client has no local controller instance
- **THEN** the hatch menu still displays the server-provided values

### Requirement: Unavailable and invalid owners are explicit
An unattached hatch or a hatch with an unsupported, unformed, unloaded, ambiguous, or invalidly configured owner SHALL show a localized unavailable status instead of stale controller values. Invalid thermal configuration SHALL be distinguishable from a valid empty 300 K store. When ownership becomes valid again, the same open menu SHALL refresh to the current owner state without retaining old heat values.

#### Scenario: Nonmelting structure becomes invalid
- **WHEN** a nonmelting controller loses formation while its hatch UI is open
- **THEN** the UI changes to an unavailable-owner state and does not continue presenting old numbers as current

#### Scenario: Owner becomes valid again
- **WHEN** a valid formed owner is attached or restored
- **THEN** the open hatch UI displays that owner's current values after synchronization

### Requirement: Thermal information is read-only and localized
The hatch UI SHALL provide no controls for setting temperature, capacity, maximum temperature, or stored heat, and no input/output direction toggle. Labels and state messages SHALL use localization keys; essential meaning SHALL remain visible in text rather than rely only on color.

#### Scenario: Viewing does not mutate heat
- **WHEN** a player opens, interacts with, or closes the hatch UI
- **THEN** the UI supplies no heat-changing action and does not alter the controller's thermal state

### Requirement: Melting and recovery are visible
A hatch with a valid melting owner SHALL show a localized melting warning and the remaining explosion countdown in game ticks. Melting MUST NOT be presented as an invalid thermal configuration or an ordinary unavailable-owner state. After sufficient cooling cancels melting, the open UI SHALL clear the warning and pending countdown on its normal synchronization cycle. UI actions MUST NOT reset or extend the countdown.

#### Scenario: Owner begins melting
- **WHEN** the owning formed multiblock exceeds its safe heat capacity
- **THEN** its hatch UI shows the actual overcapacity heat, above-limit temperature, melting warning, and remaining countdown

#### Scenario: Cooling rescues the owner
- **WHEN** a heat exchange returns the owner within its safe limit before expiry
- **THEN** the open UI clears the melting warning and no longer presents a pending explosion

#### Scenario: Last-chance recovery keeps the hatch menu available
- **WHEN** a scheduled heat exchange rescues the owner on its countdown's final tick before the expiry check
- **THEN** the surviving hatch menu clears the warning/countdown on its normal synchronization cycle and continues showing current thermal values
- **AND** a client-side countdown estimate does not close the menu or trigger destruction

#### Scenario: Melting state resumes after reload
- **WHEN** a melting controller reloads and its valid hatch UI synchronizes again
- **THEN** the UI shows the restored melting state and remaining countdown without treating reload as recovery or a fresh full countdown

#### Scenario: Dismantling destroys the viewed hatch
- **WHEN** breaking a casing, the controller, or any attached heat hatch during melting triggers the immediate explosion of the controller and the viewed owned hatch
- **THEN** the destroyed hatch no longer offers an active heat UI; the menu MUST NOT act as if the break merely made a surviving owner unavailable
