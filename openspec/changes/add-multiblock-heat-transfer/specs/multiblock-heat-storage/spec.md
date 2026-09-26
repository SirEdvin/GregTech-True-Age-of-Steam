## Purpose

Define a reusable multiblock heat store with structure-dependent safe thermal limits, derived temperature, and reversible melting before an overcapacity machine explodes.

## ADDED Requirements

### Requirement: Subclasses derive thermal characteristics from the structure
The system SHALL provide an interface and an abstract multiblock base exposing stored heat in joules, safe heat capacity in joules, current temperature in kelvin, and maximum safe temperature in kelvin. Subclasses SHALL implement abstract capacity and maximum-temperature getters based on the formed structure. The API MUST NOT expose setters for capacity, maximum temperature, or current temperature. The characteristics SHALL be reevaluated when the formed structure changes rather than assumed constant for the controller's lifetime. For a valid formed structure, capacity SHALL be finite and strictly positive and maximum temperature SHALL be finite and greater than 300 K. Invalid characteristics MUST disable heat mutations and exchange without propagating non-finite temperatures or crashing the server. Legitimate heat above capacity MUST NOT be treated as an invalid configuration.

#### Scenario: Implement a heat-capable multiblock
- **WHEN** an integration subclasses the heat base
- **THEN** it supplies maximum capacity and maximum temperature through abstract getters and changes stored heat only through the heat mutation API
- **AND** neither scripts nor the hatch UI receive setters for thermal characteristics or temperature

#### Scenario: Invalid thermal definition
- **WHEN** a machine declares zero or negative capacity, a maximum temperature at or below 300 K, or a non-finite characteristic
- **THEN** it is unavailable for heat exchange and mutations are rejected without changing other machines
- **AND** its UI reports invalid thermal configuration rather than displaying NaN or infinity

### Requirement: Temperature is derived relative to 300 K
A newly created heat machine SHALL contain zero stored heat and have an ambient temperature of 300 K. For valid characteristics, temperature SHALL equal `300 + (storedHeat / heatCapacity) * (maxTemperature - 300)`. Stored heat SHALL be nonnegative and finite but SHALL be allowed to exceed safe capacity. The same formula SHALL apply above capacity, producing temperatures above the maximum safe temperature rather than clamping the display or stored energy. Temperature MUST NOT be an independently persisted or mutable quantity.

#### Scenario: User-specified midpoint example
- **WHEN** a machine has a capacity of 1000 J, maximum temperature of 310 K, and stored heat of 500 J
- **THEN** its temperature is 305 K

#### Scenario: Empty and safe-capacity endpoints
- **WHEN** that same machine contains zero stored heat or exactly its safe capacity
- **THEN** its temperature is respectively 300 K or 310 K
- **AND** exactly reaching capacity does not start melting

#### Scenario: Above the safe temperature
- **WHEN** stored heat exceeds the valid structure's capacity
- **THEN** the same conversion formula reports a temperature above its maximum safe temperature and the machine enters melting

### Requirement: Heat mutations are server-authoritative and can exceed capacity
The API SHALL allow a finite signed change in stored heat, return the signed amount actually accepted, and support simulation without mutation. Positive requests MUST NOT be capped by safe capacity; negative requests SHALL be limited to stored heat. Requests that produce non-finite state SHALL be rejected without mutation. Client-side mutation attempts SHALL make no authoritative changes. Non-finite requests SHALL be rejected without mutation. Fractional joules SHALL be supported so sub-joule exchanges are not lost through integer rounding. Real mutations SHALL reevaluate melting against current valid thermal characteristics; simulation MUST NOT alter stored heat, melting state, or its countdown.

#### Scenario: Incoming energy exceeds safe capacity
- **WHEN** a finite accepted heat addition takes a machine above its safe capacity
- **THEN** all accepted energy remains stored and melting starts rather than rejecting or discarding the excess

#### Scenario: Extraction cannot create negative stored heat
- **WHEN** a caller requests removal of more heat than a machine contains
- **THEN** only its stored energy is removed, the returned signed delta is negative, and the machine ends at zero heat

#### Scenario: Simulation and invalid inputs
- **WHEN** a caller simulates a valid mutation, requests a mutation from the client, or requests a NaN or infinite change
- **THEN** stored heat remains unchanged
- **AND** a simulation reports the bounded result while rejected requests report no accepted change

#### Scenario: Fractional heat remains usable
- **WHEN** a valid request adds 0.5 J to a machine with sufficient space
- **THEN** stored heat increases by 0.5 J within floating-point precision and derived temperature reflects the change

### Requirement: Stored heat survives normal lifecycle transitions
Stored heat SHALL be persisted on the controller and restored on normal save/load and chunk unload/reload. Nondestructive loss of multiblock formation SHALL stop hatch exchange without resetting stored heat; surviving controllers that reform SHALL expose the same stored heat using the new structure's thermal characteristics. Breaking a casing during melting is the explicit destructive exception: the immediate-explosion requirement applies rather than allowing later reformation to rescue that controller. Missing or non-finite saved heat SHALL initialize to zero; finite negative saved heat SHALL normalize to zero. Finite nonnegative heat above capacity SHALL be preserved, not sanitized away. Hatches MUST NOT create separate copies of controller energy. Additional persistence of heat on a dropped controller item is outside this change's scope.

#### Scenario: Reload and safe reformation
- **WHEN** a partially heated controller is saved and reloaded, or a nonmelting controller is invalidated and then reformed without replacing the controller block
- **THEN** it retains its stored heat and derives the same temperature from unchanged characteristics

#### Scenario: Invalid or missing saved value
- **WHEN** a controller loads without stored heat or with a non-finite stored value
- **THEN** it starts at zero stored heat
- **AND** finite negative values normalize to zero, but legitimate positive overcapacity heat is retained

### Requirement: Structure changes preserve energy and reevaluate thermal safety
When a surviving controller forms with changed capacity or maximum temperature, it SHALL retain its stored joules and derive temperature from the newly formed structure. It MUST NOT preserve the old temperature by creating or deleting heat. Retained heat exceeding the new valid capacity SHALL start melting rather than be clamped. If valid changed characteristics bring a surviving machine back within its safe limits, active melting SHALL stop and its countdown SHALL reset. This MUST NOT defer or cancel the immediate explosion caused by breaking a casing while already melting.

#### Scenario: Rebuild with lower capacity
- **WHEN** a heated controller reforms with a valid capacity below its retained stored heat
- **THEN** it retains that energy and starts melting under the new structure's characteristics

#### Scenario: A changed structure is within safe limits
- **WHEN** a controller's newly formed valid structure can safely hold its retained heat
- **THEN** the machine derives its new temperature from the same joules and is not melting

### Requirement: Overcapacity triggers reversible melting
A valid heat machine SHALL enter melting when stored heat strictly exceeds its structure-dependent capacity, equivalently when its temperature exceeds its maximum safe temperature. If it remains melting for 40 active game ticks, the controller and its own heat hatches SHALL explode. The countdown SHALL be measured in game ticks, not heat-transfer updates, with unloaded/offline time excluded by the pause requirement below. Cooling to or below the safe threshold before expiry SHALL stop melting and completely reset the countdown. Merely decreasing temperature while still above the safe threshold MUST NOT cancel melting. Additional heating while continuously overheated MUST NOT restart the countdown. A later overheating episode SHALL start a fresh 40-game-tick countdown. Melting SHALL NOT itself disable heat exchange needed to cool the machine. A casing break during melting SHALL trigger the separate immediate-explosion rule without waiting for this deadline.

#### Scenario: Continuous overheating reaches the deadline
- **WHEN** a loaded, formed controller remains continuously above capacity for the full 40-game-tick melting interval
- **THEN** it and its own heat hatches explode, without an earlier explosion solely from crossing the threshold

#### Scenario: Another heat exchange rescues the machine
- **WHEN** a subsequent heat exchange brings a melting machine to or below its safe temperature before expiry
- **THEN** melting stops, its pending explosion is cancelled, and the countdown resets completely

#### Scenario: Partial cooling is not enough
- **WHEN** an exchange lowers temperature but leaves the machine above its safe limit
- **THEN** it remains melting and its countdown continues rather than resetting

#### Scenario: Heating does not indefinitely postpone failure
- **WHEN** a melting machine receives additional heat without having cooled within its safe limit
- **THEN** its existing countdown continues instead of restarting

#### Scenario: Overheat again after recovery
- **WHEN** a recovered machine later exceeds capacity again
- **THEN** it starts a fresh 40-game-tick countdown rather than resuming the old remainder

### Requirement: Deadline-tick cooling precedes countdown expiry
On a game tick when a melting countdown would expire, all heat exchanges scheduled for that tick SHALL run before countdown-expiry destruction is evaluated. If an exchange brings the controller to or below its safe limit, melting SHALL stop and the countdown SHALL reset, including on this final tick. A controller that remains continuously overheated after the scheduled exchanges SHALL be destroyed on that tick when its existing interval expires; merely attempting or partially completing cooling MUST NOT grant an extra tick. Expiry checks SHALL use current authoritative state, not a destruction decision captured before exchange. The same ordering SHALL apply independently of controller/hatch tick or registration order. Countdown progression SHALL remain once per active game tick, including ticks without a scheduled exchange or a connected peer; this rule MUST NOT add extra exchange packages, wait for a later transfer update, or shorten a newly started full melting interval. Immediate destruction triggered by dismantling while still melting SHALL NOT wait for this last-chance exchange stage.

#### Scenario: Cooling rescues the machine on its final tick
- **WHEN** a controller enters its deadline tick still melting and that tick's scheduled exchange reduces stored heat to or below its safe capacity
- **THEN** melting stops, the countdown resets completely, and neither the controller nor its own heat hatches are destroyed
- **AND** no failure sound is emitted for the cancelled destruction

#### Scenario: Final-tick cooling is insufficient
- **WHEN** a scheduled exchange on the deadline tick lowers temperature but leaves the controller continuously above its safe capacity
- **THEN** the controller and its own heat hatches are destroyed with the specified sound-only failure effects after the exchange stage, without another grace tick

#### Scenario: Deadline tick has no usable exchange
- **WHEN** a continuously overheated controller reaches its deadline on a tick with no scheduled exchange or no eligible cooling peer
- **THEN** its countdown still expires on that tick rather than waiting for a later network update

#### Scenario: Dismantling occurs before last-chance cooling
- **WHEN** a casing, controller, or attached heat hatch is broken while the machine is still melting and before any planned final-tick cooling has rescued it
- **THEN** immediate targeted destruction occurs without waiting for the scheduled exchange stage

### Requirement: Dismantling during melting triggers immediate explosion
If a casing, the controller itself, or any attached heat hatch belonging to a melting multiblock is broken, that controller and its own heat hatches SHALL explode immediately in response to the break, without waiting for the countdown deadline or the next periodic heat-transfer update. The break MUST NOT merely pause/reset the countdown or permit removal, replacement, or reformation to cancel the destruction. Melting state, controller position, and own-hatch target identities MUST be retained before removal clears the controller or its formation associations; the block being broken MUST NOT escape its triggered explosion merely because removal has already begun. The resulting destruction MUST execute only once for that melting episode despite further removal callbacks caused by the explosions. Ordinary chunk unload, server shutdown, and temporary lack of loaded structure data MUST NOT be classified as dismantling or trigger this immediate explosion. Breaking only an external connecting vent SHALL disconnect the affected heat path without itself triggering immediate explosion, cancelling melting, or resetting/pausing the countdown of a still-ticking controller.

#### Scenario: Casing broken before deadline
- **WHEN** a casing of a melting multiblock is broken while time remains on its countdown
- **THEN** the controller and its own heat hatches explode immediately instead of waiting out the remaining interval

#### Scenario: Controller broken before deadline
- **WHEN** the controller itself is broken while its multiblock is melting
- **THEN** the controller location and its own heat hatches trigger their explosions immediately rather than allowing controller removal to cancel the melting episode

#### Scenario: Attached heat hatch broken before deadline
- **WHEN** any attached heat hatch of a melting multiblock is broken
- **THEN** the controller and all its own heat hatches, including the hatch being broken, trigger their explosions immediately using ownership captured before detachment
- **AND** hatches owned by other controllers are not selected merely because they share the vent network

#### Scenario: Explosion causes further removal callbacks
- **WHEN** targeted destruction causes further controller or owned-hatch removal callbacks
- **THEN** the same melting episode does not initiate a second explosion sequence or omit its remaining targets

#### Scenario: Dismantling after cooling recovery
- **WHEN** a controller has already cooled within its safe limit and stopped melting before a casing, the controller, or an attached heat hatch is broken
- **THEN** this melting mechanic does not trigger an immediate explosion for that break

#### Scenario: External vent broken during melting
- **WHEN** only an external connecting vent is broken while a connected controller is melting and remains ticking
- **THEN** the affected heat path is disconnected without an immediate explosion
- **AND** the controller remains melting with its existing countdown continuing normally

#### Scenario: Unload is not structural damage
- **WHEN** a melting multiblock becomes unavailable because its controller chunk unloads or the server shuts down, without any actual casing, controller, or attached heat hatch break
- **THEN** no immediate dismantling explosion is triggered

### Requirement: Unload and restart preserve and pause melting
The controller SHALL persist active melting state and its remaining countdown together with stored heat. While its chunk is unloaded or the server is stopped, the countdown SHALL pause rather than advance, reset, or cause an offline explosion. When the controller is loaded and resumes ticking, it SHALL resume the saved remainder, not start a fresh 40-tick interval or subtract elapsed offline time. Loading lifecycle callbacks and temporary absence of formed-structure data MUST NOT erase the saved melting state or manufacture a cooling recovery. Actual cooling within the safe threshold SHALL still cancel melting normally once valid thermal state is available.

The controller SHALL own countdown progression independently of hatch/network availability. While that controller continues ticking, unloading another part of its structure, an owned heat hatch, or an external vent path MUST NOT pause, restart, or cancel its active countdown. Such unavailability SHALL still prevent invalid heat exchange and MUST NOT be mistaken for an actual destructive break. Missing structure data alone MUST NOT manufacture cooling recovery or postpone the controller's deadline.

#### Scenario: Another structure chunk unloads while the controller keeps ticking
- **WHEN** a melting controller stays loaded and ticking while a chunk containing another structure part or an owned heat hatch unloads
- **THEN** its existing countdown continues once per active controller tick and is not gated on whole-structure availability
- **AND** unavailable endpoints do not exchange heat or cause a false immediate-dismantling trigger

#### Scenario: Chunk unload and reload during melting
- **WHEN** a melting controller unloads with time remaining and later reloads with unchanged heat and structure
- **THEN** it retains that remaining interval and continues the same melting episode without offline countdown progress or a reset

#### Scenario: Save and restart during melting
- **WHEN** the server saves a melting controller, stops, and subsequently restarts
- **THEN** the stored heat, melting state, and remaining interval are restored without an explosion caused merely by time spent offline

### Requirement: Explosion targets belong to the overheating multiblock
Melting explosions SHALL destroy only the overheating controller and its own multiblock heat hatches and SHALL play explosion sound effects at the affected locations. This SHALL apply both to countdown expiry and immediate dismantling-triggered destruction. The failure effect MUST NOT damage or destroy surrounding casings, vents, other machines, or unrelated blocks; it MUST NOT damage entities, apply knockback, or create fire. A casing already broken by the triggering action is not restored, but the failure effect MUST NOT destroy additional casings. A separate controller or hatch MUST NOT be destroyed solely because it shares the vent network or is physically nearby. Other machines SHALL require their own overheating episode to trigger their own melting destruction. Sound effects MUST NOT be implemented by introducing a damaging area explosion.

#### Scenario: Other machines share the vent network
- **WHEN** a melting multiblock reaches its explosion deadline or suffers a casing, controller, or attached heat hatch break while connected by vents to another safe multiblock
- **THEN** only the melting controller and its own heat hatches are selected for directly triggered explosions

#### Scenario: Countdown expiry destroys only owned targets with sound
- **WHEN** a melting controller reaches its deadline with intact surrounding casings, vents, unrelated blocks, and nearby entities
- **THEN** only that controller and its own heat hatches are destroyed and explosion sounds play at the affected locations
- **AND** the failure effect causes no surrounding block damage, entity damage, knockback, or fire

#### Scenario: Dismantling failure has no collateral blast
- **WHEN** breaking a casing, controller, or attached heat hatch during melting triggers immediate destruction
- **THEN** the same controller/own-hatch-only destruction and explosion sounds occur, without destroying additional casings or damaging surrounding blocks or entities
- **AND** the sound effect does not cause knockback or fire

### Requirement: Existing multiblocks remain unchanged
This infrastructure SHALL leave existing cooling machines, boiler heat-level behavior, recipes, and multiblock patterns unchanged. It SHALL NOT add passive ambient heating or cooling, automatic heat generation, or recipe-based heat consumption.

#### Scenario: Existing machine after installing the infrastructure
- **WHEN** an existing cooling or boiler multiblock operates after the change
- **THEN** it uses its existing mechanics and does not participate in the heat network unless a later change explicitly opts it in
