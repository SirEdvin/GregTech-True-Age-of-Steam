## Purpose

Allow future heat-capable multiblocks to exchange stored energy through bidirectional hatches and physical, bounded connections made from existing GTCEu heat vents.

## ADDED Requirements

### Requirement: A bidirectional hatch represents one formed heat controller
The system SHALL register a bidirectional heat hatch family with HV, EV, IV, and LuV variants sharing an addon-owned part ability usable in opt-in multiblock patterns. Each participating hatch MUST belong to exactly one loaded, formed, valid heat-capable controller. A hatch SHALL have no independent heat buffer and no input/output mode. Unattached, unsupported, invalid, ambiguously owned, unloaded, or unformed endpoints MUST NOT exchange energy. Hatches belonging to the same controller MUST NOT exchange energy with one another. Tiering SHALL control nominal heat-transfer conductance, not introduce electrical power requirements or voltage-overload mechanics.

#### Scenario: Form and invalidate a heat multiblock
- **WHEN** a heat hatch joins a valid formed heat controller
- **THEN** it exposes that controller's shared heat state and can send or receive according to temperature
- **AND** invalidating or unloading its controller makes it unavailable before the next exchange

#### Scenario: Unsupported or ambiguous owner
- **WHEN** a hatch has no owner, a non-heat owner, or more than one owner
- **THEN** it remains nonparticipating rather than selecting an arbitrary heat store

#### Scenario: Multiple hatches on one controller
- **WHEN** several heat hatches belong to the same controller
- **THEN** all reference the same stored energy and no self-transfer occurs

### Requirement: Hatch tiers define nominal transfer coefficients
The registered tiers SHALL be HV, EV, IV, and LuV, with nominal coefficients of 2, 8, 32, and 128 J/(K * update), respectively. For every connected pair, the tier-limited package SHALL be the sending hatch's coefficient multiplied by the current positive temperature difference in kelvin. The receiving hatch's tier MUST NOT limit incoming transfer. The sender SHALL be determined from current controller temperatures at the time of exchange, so a direction reversal SHALL select the new sending hatch's coefficient. Actual transferred heat SHALL remain capped by donor energy and pairwise equilibrium; the tier coefficient is not an unconditional package size, a receiver storage limit, or a new heat buffer. All variants SHALL retain the same front-face connection rules, 32-step range, controller ownership, and melting behavior. Additional hatches between the same controller pair MUST NOT sum their coefficients or create extra packages; the previously confirmed one-package-per-controller-pair rule remains in effect.

#### Scenario: All requested variants are registered
- **WHEN** the heat hatch family is registered
- **THEN** distinct HV, EV, IV, and LuV block/item variants are available under the shared heat part ability with coefficients 2, 8, 32, and 128 J/(K * update)
- **AND** no lower or higher hatch tiers are introduced by this change

#### Scenario: Same-tier conductance scales packages
- **WHEN** otherwise comparable connected same-tier pairs have the same positive temperature difference and neither donor-energy nor equilibrium caps bind
- **THEN** their HV, EV, IV, and LuV package energies equal respectively 2, 8, 32, and 128 times that difference in joules

#### Scenario: A higher-tier package reaches equilibrium first
- **WHEN** a tier-limited package would exceed the energy needed to equalize the two controllers
- **THEN** only the equilibrium amount is transferred, conserving energy without reversing their temperature ordering

#### Scenario: Higher-tier sender feeds a lower-tier receiver
- **WHEN** a hotter controller sends heat through a LuV hatch to a cooler controller's HV hatch
- **THEN** the requested package uses 128 J/(K * update), not the receiving hatch's 2 J/(K * update)
- **AND** donor-energy and equilibrium limits still apply, while received energy above safe capacity is retained and evaluated for melting

#### Scenario: Reversing direction selects the new sender's tier
- **WHEN** the HV-side controller becomes hotter than the LuV-side controller on that same connection
- **THEN** the next exchange uses the HV sending hatch's 2 J/(K * update), not the former LuV sender's coefficient

#### Scenario: Receiver tier does not change transfer allowance
- **WHEN** otherwise equivalent eligible connections use the same sending tier and current thermal state but different receiving hatch tiers
- **THEN** they have the same tier-limited package allowance regardless of receiver tier

### Requirement: Only physical vent paths establish a connection
Two participating hatches SHALL connect through a continuous path of face-adjacent `gtceu:computer_heat_vent` blocks in the same dimension. Paths SHALL allow bends, vertical sections, and branches. Intermediate blocks MUST be vents, not arbitrary solids, air, pipes, or intermediate hatches. Each hatch SHALL conduct only through its single designated front face. The path MUST leave the source through that face and enter the destination through its designated front face; side, rear, top, or bottom contacts not designated as front MUST NOT connect. Directly adjacent hatches SHALL connect without intermediate vents only when their conducting front faces face each other. Vent blocks SHALL neither store heat nor lose heat to the environment.

#### Scenario: Bent and branching connections
- **WHEN** one hatch connects to two other valid hatches through a branching vent layout within range
- **THEN** both destinations are discovered even if the paths bend or travel vertically

#### Scenario: A gap, diagonal, or ordinary solid
- **WHEN** all routes between two hatches require crossing air, a diagonal-only contact, or a non-vent intermediate block
- **THEN** the hatches do not exchange heat

#### Scenario: A vent touches the wrong hatch face
- **WHEN** a vent path reaches only a nonconducting face of either hatch
- **THEN** it does not establish a connection even when all other path requirements are satisfied

#### Scenario: Rotation changes connectivity
- **WHEN** a hatch rotates so its designated front face no longer meets the vent path
- **THEN** the next exchange update does not use the old connection

#### Scenario: Direct face-to-face contact
- **WHEN** two eligible adjacent hatches point their conducting front faces at each other
- **THEN** they connect directly without an intermediate vent

#### Scenario: No pass-through hatch shortcut
- **WHEN** reaching a destination would require traversing another hatch as an intermediate block
- **THEN** that route does not establish a direct connection to the destination

### Requirement: The range limit applies to shortest connected path length
A connection SHALL require a shortest valid hatch-to-hatch path of at most 32 face-adjacent steps, inclusive. Distance SHALL include the step leaving the first hatch and the step entering the second hatch; controller positions and straight-line proximity SHALL NOT replace this path measurement. Cycles and duplicate paths MUST terminate discovery without duplicate endpoints. Intermediate controllers SHALL NOT make distant endpoints directly connected, although heat can propagate through separate valid controller pairs over successive exchanges.

#### Scenario: Exact boundary
- **WHEN** two hatches have a valid path of exactly 32 adjacency steps
- **THEN** they connect
- **AND** a shortest valid path of 33 steps does not connect

#### Scenario: Nearby endpoints but long route
- **WHEN** endpoints are geometrically near but their shortest uninterrupted vent route exceeds 32 steps
- **THEN** they do not connect

#### Scenario: Loops and alternative routes
- **WHEN** vents form a loop or several valid paths reach the same hatch
- **THEN** discovery terminates and counts the endpoint only once

### Requirement: Packages flow according to current temperature
For each eligible pair, energy SHALL move only from higher temperature to lower temperature. Before donor-energy and equilibrium limits, requested package energy SHALL be proportional to the positive temperature difference using only the sending hatch's tier conductance, not one global flat coefficient or a minimum of both endpoint coefficients. Capacity SHALL affect conversion of energy to temperature and the melting threshold, not selection of direction or admission of incoming heat. Equal-temperature machines SHALL exchange no heat, even if their capacities or stored joules differ. A hatch SHALL automatically reverse direction when its controller becomes cooler than its peer. A melting but otherwise valid formed controller SHALL remain eligible to exchange heat, including releasing heat to recover before its countdown expires.

#### Scenario: Larger temperature difference
- **WHEN** otherwise equivalent pairs have different positive temperature differences and no safety cap is reached
- **THEN** the pair with the larger difference transfers a proportionally larger package

#### Scenario: Capacity does not select direction
- **WHEN** a high-capacity controller is hotter than a low-capacity controller
- **THEN** heat moves to the low-capacity controller
- **AND** reversing their temperature ordering reverses the direction without hatch configuration

#### Scenario: Equal temperatures and different energy stores
- **WHEN** connected controllers have equal temperatures but different stored energy or capacity
- **THEN** neither transfers heat to the other

### Requirement: Exchange conserves energy without overshooting equilibrium
Every pair exchange SHALL subtract and add the same accepted joule amount within floating-point precision. The transfer MUST be limited by donor energy and the amount at which that pair reaches equal temperature under each controller's current structure-dependent conversion formula. A pair exchange MUST NOT reverse the pair's temperature ordering, but SHALL be allowed to exceed the receiver's safe heat capacity and maximum safe temperature. Transfers SHALL use current state after earlier exchanges, not independently apply stale full-size packages from a shared snapshot. Incoming energy exceeding safe capacity MUST be stored and evaluated for melting rather than rejected or discarded. Numerical non-finite or unrepresentable state MUST NOT be introduced on either side.

#### Scenario: Equilibrium-limited package
- **WHEN** a requested package exceeds the energy needed to equalize its two endpoints
- **THEN** the package is reduced to that equilibrium amount and total stored energy is conserved within floating-point precision

#### Scenario: Hot donor drives the receiver above its safe maximum
- **WHEN** an equilibrium-limited package from a hotter controller takes the receiver above its safe capacity
- **THEN** the receiver accepts the package, exceeds its safe temperature, and starts melting while transferred joules remain conserved

#### Scenario: A later exchange cools a melting controller
- **WHEN** a melting controller transfers enough heat to a cooler peer to return within its safe capacity before its deadline
- **THEN** the transfer remains energy-conserving and that controller stops melting with its countdown reset

#### Scenario: Scheduled exchange gives deadline-tick cooling its last chance
- **WHEN** a valid controller pair is scheduled to exchange heat on a tick when one controller's melting countdown would expire
- **THEN** the controller remains eligible for that exchange and all scheduled pair exchanges complete before countdown-expiry destruction is evaluated
- **AND** sufficient cooling cancels destruction and resets melting, while insufficient cooling does not postpone expiry

#### Scenario: Several destinations share a donor
- **WHEN** a hot controller exchanges with several peers in one update
- **THEN** each exchange uses updated controller values so the donor cannot be overdrawn and no receiver overshoots that pair's current equilibrium

#### Scenario: Repeated isolated exchange remains safe
- **WHEN** connected controllers with unequal temperatures receive repeated updates without external heat changes or structure changes and neither crosses its melting threshold
- **THEN** their temperature difference tends toward zero within documented numerical tolerance without equilibrium overshoot or creation of energy

### Requirement: Topology and lifecycle are authoritative at exchange time
Heat exchange SHALL run only on the logical server and SHALL NOT force-load chunks. Every exchange update SHALL use currently loaded paths, front-face orientations, and currently valid owners. Breaking a vent or endpoint, rotating a hatch away from the path, losing formation, changing owner, or unloading a required chunk MUST prevent subsequent invalid transfers; restoring a valid path and surviving formed endpoints SHALL resume exchange on a later update. No heat-transfer update SHALL replay offline elapsed time after reload. Each unordered controller pair SHALL receive at most one package per transfer update regardless of duplicate paths or multiple hatches. Each distinct controller pair SHALL have an independent temperature-based allowance; adding distinct peers can increase total transfer, and there is no separate per-controller or per-hatch throughput budget. Pair processing order SHALL be stable for unchanged topology rather than depend on hatch tick order. Controller unload/restart SHALL pause its persisted melting countdown, whereas a casing break during melting SHALL immediately explode that controller and its own hatches as specified by the heat-storage capability. Removing a network edge or clearing controller associations MUST NOT cancel those safety actions or reset a pending countdown.

#### Scenario: Broken path or unloaded intermediate chunk
- **WHEN** a vent is removed or any chunk required by the only valid path unloads before an update
- **THEN** no energy crosses that path during the update and the network does not load the chunk itself
- **AND** a melting controller that remains ticking continues its own countdown regardless of that network unavailability

#### Scenario: Breaking a melting owner's heat hatch
- **WHEN** an attached heat hatch is broken while its controller is melting
- **THEN** the controller and all its own heat hatches immediately trigger the heat-storage capability's dismantling explosions, rather than merely dropping the broken hatch from the network
- **AND** clearing the broken hatch's owner association does not bypass that rule

#### Scenario: External vent removal is only a disconnection
- **WHEN** only an external connecting vent is removed while a connected controller is melting
- **THEN** no immediate dismantling explosion is triggered, invalid paths stop exchanging heat, and still-valid alternate paths remain eligible
- **AND** a still-ticking controller's melting countdown is neither cancelled, reset, nor paused by the disconnection

#### Scenario: Melting destruction preserves the surrounding network
- **WHEN** a melting controller and its own heat hatches are destroyed with explosion sound effects
- **THEN** those endpoints become unavailable, but the failure effect does not destroy surrounding vents or other controllers' hatches
- **AND** remaining valid controller pairs can continue exchanging through intact paths without a collateral blast or network-wide destruction

#### Scenario: Reconnect without offline catch-up
- **WHEN** the path and endpoints become valid again after a long unload
- **THEN** normal scheduled exchange resumes without transferring accumulated offline packages

#### Scenario: Duplicate controller pair
- **WHEN** two controllers are linked by several hatch pairs and vent routes
- **THEN** that controller pair receives only one package in the update, not multiplied throughput

#### Scenario: Additional distinct peers have separate allowances
- **WHEN** a controller connects to additional distinct eligible controllers
- **THEN** each pair can exchange its own temperature-dependent package using current values, without a shared hatch or controller throughput allowance

#### Scenario: Client-side activity
- **WHEN** a client renders hatches or opens a hatch UI
- **THEN** no client-side heat exchange or authoritative heat mutation occurs
