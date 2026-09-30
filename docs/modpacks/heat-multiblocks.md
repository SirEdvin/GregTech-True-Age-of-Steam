# Heat multiblock integration (development)

This infrastructure is opt-in. Existing boilers and cooling machines are unchanged.
Debug heat producers/consumers, a craftable heat hatch, and insulated heat pipes
are provided. Melting can destroy machines; test pack integrations in disposable worlds.

## Debug network visualizer

Hold `gttruesteam:debug_heat_network_visualizer` in either hand to outline all
nearby physical heat networks through blocks. Each connected network has its own
color, shared by all its hatches and pipes, including dead-end branches. Nearby
means a hatch within 32 blocks of the player; the entire loaded component is
highlighted, even beyond the pairwise exchange range. Multiple networks are
shown together, without aiming or clicking. The overlay refreshes every ten
client ticks and disappears immediately when the item is put away.

This is a wiring diagnostic, not a claim that controllers are formed or currently
transferring heat. It inspects only client-loaded chunks, never loads missing
chunks, and requires no server requests. Disconnected or misoriented hatches are
also highlighted, with any pipes reachable from their front face, so incomplete
wiring remains diagnosable. The item ships in the normal mod JAR, has no crafting recipe, and
can be obtained with `/give @s gttruesteam:debug_heat_network_visualizer`.

Each hatch also has a through-wall floating label: controller temperature in K,
green incoming J/exchange, gold outgoing J/exchange, and the calculated network
coefficient in J/(K × exchange). All values are server-synced. Incoming and
outgoing amounts are actual totals for the last scheduled exchange, not J/t.
Only the hatches chosen for a controller-pair transfer receive credit; fan-out
totals accumulate. A subsequent exchange without transfer resets totals to zero.
Unavailable controllers show an explicit unavailable label, not a fake temperature.

## Insulated heat pipes

`gttruesteam:insulated_heat_pipe` is a plain block with six connection-state bits,
no block entity, no ticker, and no item/fluid/energy capabilities. Its half-block
diameter and textures follow the normal GregTech pipe. Connections form to other
heat pipes and the designated front face of heat hatches.

Connections use the default GT6-style placement and wrench behavior, regardless
of GregTech's `gt6StylePipesCables` setting. Placement connects toward the supporting
compatible pipe/hatch and picks up neighboring pipe arms already pointing toward
the new block; it does not join other adjacent closed sides. GregTech's face grid
selects the side for wrench toggles and pipe placement. Wrench toggles synchronize
both ends of adjacent heat pipes and use normal tool sounds/durability.

The six block-state properties now store explicit open ports. Neighbor updates
do not reopen closed sides; an open arm can remain open toward empty space.
Both heat transfer and the visualizer require matching open pipe ports. Existing
saved pipe states retain their connections. Command-placed pipes default to all
sides closed unless explicit state properties are supplied or a wrench is used.

The assembler produces four pipes from two Heating-Infused Cometal Plates and
two Insertion-Infused Cometal Plates, at 16 EU/t for 100 ticks. Insertion is the
combined Extraction + Polarization concept and uses the existing combined-concept
infusion progression. Computer Heat Vents no longer conduct heat; existing
networks must replace them with these pipes.

## Thermal redstone readings

Both debug heat structures accept one optional Redstone Output Hatch in place of
any iron casing block. Configure its normal rule editor using these numeric readings:

| Reading | Stable identifier | Unit |
| --- | --- | --- |
| Stored heat | `heat_joules` | J |
| Temperature | `temperature_kelvin` | K |
| Heat capacity filled | `heat_capacity_percent` | % |

Capacity fill is stored heat divided by safe capacity, multiplied by 100; it is
not clamped at 100%, so overheating thresholds work. All three support fractional
values and the existing numeric comparison operators. For example, select Heat
capacity filled (percent), greater than or equal to 90, and signal strength 15.
Invalid/unformed controllers provide no thermal reading and cannot trigger these
rules. The shared heat controller base supplies these readings to future heat
machines too; their patterns must explicitly accept a redstone output hatch.

## Controller contract

Extend `site.siredvin.gttruesteam.machines.shared.heat.HeatMultiblockMachine`.
Its constructor accepts `IMachineBlockEntity holder, Object... args`, forwarding
recipe-logic arguments to GTCEu's `WorkableMultiblockMachine`. Implement abstract
`getHeatCapacity()` and `getMaxTemperature()` from the formed structure, not from
public setters. Characteristics must be finite, capacity strictly positive and
maximum temperature greater than 300 K; the conversion slope must be finite and
positive too. Finish updating derived characteristics before performing a heat
mutation in a structure-formation override. Preserve superclass lifecycle hooks
and inherit its `MANAGED_FIELD_HOLDER` when adding managed fields.

Prefer cached formation-derived characteristics. If a getter inspects blocks,
use `ServerChunkCache.getChunkNow` and the returned chunk's accessors. A world
`getBlockState`/`getBlockEntity` call can renew an UNKNOWN chunk ticket even after
`hasChunkAt` succeeds, preventing natural unload. The heat coordinator, owner
resolver and removal ledger use already-loaded chunk access instead.

`IHeatMachine` exposes stored joules, safe capacity in joules, temperature in
kelvin, maximum safe kelvin, melting status and remaining ticks. There are no
thermal-characteristic, temperature or countdown setters. Server-thread callers
use `changeHeat(deltaJoules, simulate)`; the return value is the accepted signed
energy delta. Simulation never alters energy or the melting countdown. Negative
requests extract at most the stored energy; fractional joules are supported.
Unformed/unavailable/invalid machines and client-side mutation attempts reject
changes. Non-finite requests or resulting thermal state are rejected.

Capacity is a **safe threshold**, not an incoming-energy limit and not J/K:

    T = 300 + (Q / C) * (M - 300)

For `C = 1000 J`, `M = 310 K`, `Q = 500 J`, temperature is `305 K`.
At `Q = 1000 J` it is `310 K`, still safe. At `Q = 2000 J` it is
`320 K`, above the safe limit; all stored energy is retained. New stores are
empty at 300 K. Reformation preserves joules, not temperature. Missing, negative
or non-finite saved energy normalizes to zero; finite overcapacity energy is not
clamped. Energy and melting state belong to the controller, never a hatch.
Controller item drops do not receive portable heat persistence.

## Pattern and hatch registration

Use `TrueSteamPartAbilities.HEAT` explicitly in future patterns:

    Predicates.abilities(TrueSteamPartAbilities.HEAT)

The shared `HeatHatchMachine` extends GTCEu's `TieredPartMachine`, prohibits shared
ownership and has neither an electrical nor an independent thermal buffer.
Only `gttruesteam:heat_hatch` is registered. Its HV hull is structural, not a
transfer coefficient. Legacy HV/EV/IV/LuV block, item and block-entity IDs remap
to this hatch when loading existing worlds.

Assembler, circuit 6: two Insertion-Infused Cometal Plates, two Extraction-Infused
Cometal Plates, four Heating-Infused Cometal Plates, one HV Machine Hull and one
Infernal Circuit produce two Heat Hatches (100 ticks, HV recipe power).

## Routing and exchange

Only `gttruesteam:insulated_heat_pipe` conducts between hatch endpoints. Paths leave and
enter the designated front faces and may bend, branch, loop or run vertically.
Direct front-to-front adjacency has no pipe coefficient and transfers no heat.
Hatches terminate paths; they are
not pass-through pipes. Air, computer heat vents, other blocks and diagonals do not conduct.

The inclusive limit is 32 face-adjacent hatch-to-hatch steps, counting the first
and last boundary steps: at most 31 intermediate pipes on the shortest path.
A geometrically nearby endpoint with a longer required detour is not connected.
Discovery is fresh every 20 game ticks and never requests unavailable chunks.
Rotation, pipe removal/reconnection and chunk availability affect the next update.
Pipes store no heat, lose no heat and perform no ambient exchange.

Every distinct controller pair gets one package per scheduled update. Multiple
hatches or routes between the same pair do not stack. The currently hotter side
is the sender. The slowest pipe anywhere in the connected loaded network sets
its coefficient, including dead-end branches beyond the 32-step pair range.
Insulated heat pipes provide 16 J/(K × exchange), independent of hatch tier or
flow direction. If a pair has connections through separate networks, the highest
available network coefficient is used once; rates do not stack. Pairs are
processed in stable controller-position order, using current energy after each
previous pair, rather than simultaneously or with a shared fan-out allowance.

For slopes `s = (M - 300) / C`, exchange requests:

    q = min(networkCoefficient * (Thot - Tcold),
            (Thot - Tcold) / (sHot + sCold), Qhot)

Receiver safe capacity does not cap admission. The same representable amount is
removed and added. Changes smaller than both stores can represent are skipped,
not rounded to whole joules. Temperature comparison uses a 1e-9 K tolerance;
melting uses the strict stored-joules comparison, with no tolerance allowance.
Tests assert energy conservation with a relative 1e-12 tolerance.

## Melting, cooling and failure

`Q > C` starts a full 40-active-game-tick melting interval. Further heating does
not restart it. Cooling to `Q <= C` cancels melting and clears the countdown;
a later overheating episode starts a fresh interval. Melting does not disable
otherwise-valid cooling exchange.

The network manager performs scheduled exchanges at level END. Each controller
uses its own native server-tick subscription and finalizes that tick through its
own lower-priority END listener, after exchange. There is no controller registry
in the network manager. Cooling on the deadline tick can still cancel
failure. Insufficient cooling expires that tick; no transfer scheduled or no
connected peer does not delay expiry. A newly started episode is not shortened
by the same tick's finalization.

Breaking a casing, the controller itself or one of its attached heat hatches
while melting triggers immediate targeted failure, without waiting for scheduled
cooling. Breaking an external connecting vent only disconnects affected paths:
it does not immediately destroy the structure, reset melting or pause the timer.
Alternate valid routes remain usable.

Failure removes only that controller and its own heat hatches, with explosion
sounds at affected locations. It does not invoke an area explosion, damage
surrounding casings/vents/blocks/entities, apply knockback or create fire.
Reentrant removal callbacks are guarded. Persisted target identities and a
level-local deferred-removal ledger retain unavailable hatch targets without
force-loading chunks or deleting unrelated replacement machines.

The countdown pauses when the controller itself stops ticking or the server is
stopped; unload/restart preserves its remaining interval without offline catch-up.
Another part/hatch/vent unloading does not pause a still-ticking controller.
Unavailable structure data does not itself count as cooling or dismantling.

The read-only hatch menu uses server-populated, non-persisted managed display
fields, rather than requiring the controller to exist on the client. It shows
actual stored heat/temperature (including unsafe values), safe limits and melting
warning/countdown. Unavailable owners and invalid thermal configuration have
separate localized messages. There are no heat, countdown or direction controls.

## Development integration fixture

### Shipped debug machines

`gttruesteam:debug_heat_producer` and `gttruesteam:debug_heat_consumer` are included
in the normal JAR and GTCEu machine creative tab, without crafting recipes.
Use `/give @s gttruesteam:debug_heat_producer` and
`/give @s gttruesteam:debug_heat_consumer` to obtain them directly.

Build each as a hollow 3×3×3 iron-block shell, with the controller in the center
of one side facing outward. The center block is air. Any other shell block may
be replaced by a heat hatch; point hatch fronts toward the connecting insulated
heat pipes. One optional redstone output hatch is supported. No energy, item or
fluid hatches are required.

Each machine has one repeating 20-working-tick recipe. The controller menu's
Rate (J/t) field selects heat added by the producer or removed by the consumer
per working tick. It defaults to 1, accepts non-negative fractional values
(zero disables heat change), and is saved with the controller. The consumer
pauses without losing recipe progress when the configured amount is unavailable.
Both use a 1000 J safe capacity
and 310 K safe maximum. The controller UI shows recipe progress and thermal values;
the hatch UI shows the shared thermal state. Standard work controls apply (GTCEu
normally finishes the current recipe before disabling). Production is deliberately
not capped at safe capacity: disconnect cooling to observe melting/destruction.

These two debug machines are shipped; the automated fixtures below are not.
The real-server debug test is `./gradlew runServer --no-daemon -PheatTest
-PheatTestDebug=true`, with report `run-heat-test/heat-debug-results.json`.
It destructively uses x=512..520, y=120..122, z=512..514 in the isolated world.
Its 23 assertions cover real recipe loading/operation, vent-delivered consumption,
waiting/resumption, conservation with recipes suspended, and melting without hatches.
They also verify both controller thermal readouts, hatch UI creation, and formed
hatches inheriting the controller's iron casing. Heat hatches retain their central
vent-grille overlay over the multiblock appearance; unformed hatches use tier hulls.
Controllers explicitly implement GTCEu's `IFancyUIMachine`; the bare workable
multiblock base does not provide a right-click menu. Both menus use GTCEu's fancy
machine frame, display texture and scrollable server-synchronized text panel.
Gold identifies mode/tier, green/yellow identifies operational status, cyan shows
live thermal values, gray shows safe limits, and bold red warns of melting.
Actual right-click producer, consumer and formed-hatch menus were inspected in
the graphical client, including working production and waiting consumption.

`src/heatTest/java/site/siredvin/gttruesteam/heatfixture/HeatFixture.java` is the
compiled integration example. It uses the production base and ability with a
three-block row: controller, hatch, iron/gold block. Gold selects 2000 J / 320 K;
iron selects 1000 J / 310 K. Automated variants also accept lapis (100000 J /
310 K), diamond (100000 J / 1300 K), and redstone (intentionally invalid zero
capacity). Development-only resources supply a simple iron controller model.
There is no fixture registration, model or test control in the shipped JAR.

Compile the fixture:

    ./gradlew compileHeatTestJava --no-daemon -PheatTest

Run in the isolated `run-heat-test` working directory:

    ./gradlew runClient --no-daemon -PheatTest
    ./gradlew runServer --no-daemon -PheatTest

The dedicated server requires the user's acceptance of the Minecraft EULA.
Do not copy a valuable world into this directory. In a creative/operator session,
`/heatfixture place` places two fixture rows connected by vents near the caller.
After formation, look at the first controller and use `/heatfixture seed` to add
1100 J through the production mutation API. Open hatches to inspect the state.
`/heatfixture add <x> <y> <z> <joules>` supports operator-controlled signed heat
changes at a loaded fixture. The placement, looked-at seeding, coordinate seeding,
controller rendering and all four hatch menus have been exercised in the client.

Run the opt-in automated dedicated-server checks:

    rm -f run-heat-test/heat-runtime-results.json
    ./gradlew runServer --no-daemon -PheatTest -PheatTestAuto=true
    jq -e '.passed == true' run-heat-test/heat-runtime-results.json

This destructive fixture uses the isolated overworld at x=0..3, y=120, z=0..15,
temporarily forces chunks (0,0) and (0,1), writes a JSON assertion report, then
stops the server. Never run it against a valuable world. Check the report as well
as the Gradle exit status: assertion failure still requests orderly server shutdown.

Additional isolated suites (run one mode at a time):

    ./gradlew runServer --no-daemon -PheatTest -PheatTestNetwork=true
    ./gradlew runServer --no-daemon -PheatTest -PheatTestPersistence=prepare
    ./gradlew runServer --no-daemon -PheatTest -PheatTestPersistence=resume
    ./gradlew runServer --no-daemon -PheatTest -PheatTestChunks=true

Their reports are `heat-network-results.json`, `heat-persistence-prepare.json`,
`heat-persistence-resume.json`, and `heat-chunk-results.json` under `run-heat-test`.
Delete the corresponding old report before each run and require `.passed == true`
afterward. Persistence prepare/resume must run in that order in separate JVMs.
The network suite uses x=256..259, y=120, z=256..296; persistence uses (64,120,64);
chunk tests use (1024,120,1024) and (1152,120,1024), then a long fixture extending
east from x=1024 through x=1280 at y=120, z=1024. All are destructive test areas.

## Verification status

For the opt-in connected-client suite, run
`ALSOFT_DRIVERS=null ./gradlew runClient --no-daemon -PheatTest -PheatTestClient=true`
and enter only a disposable creative world. The null audio backend is useful on
headless hosts without a physical sound device. This suite overwrites a row from
(2048,120,2048) through (2304,120,2048), teleports the player, opens the hatch menu,
and writes `run-heat-test/heat-client-results.json`. It does not exit the client;
save and quit normally after checking the report. Never enable it in a valuable world.

Pure `HeatTransferTest` and `HeatNetworkTest` cover numeric validation,
overcapacity admission, fractional exchange/conservation, face routing, loops,
branches, gaps, unloaded lookups and range boundaries. The automated server run
passed 152 assertions covering formation, mutation/simulation, vent exchange,
thresholds, reformation with reduced capacity, recovery, full countdown expiry,
successful and insufficient final-tick cooling, part-unload callbacks, and separate
casing/controller/hatch-break failures in original and reversed registration order.
Server sound events occurred once per destroyed target and not after rescue;
nearby vulnerable entities retained health, position and zero velocity, and no
surrounding fire appeared. Client sound receipt remains a separate acceptance
check. The network suite passed 395 assertions,
including uncapped tier scaling for every sender/receiver combination, nonstacked
connections, invalid/stale ownership, display snapshots and direction reversal
during sequential fan-out, repeated with reversed registration order. Live rotation,
vent removal/restoration, exact 32/33 range boundaries and unavailable intermediate
and endpoint chunks are covered, including resumed exchange without catch-up.

Persistence prepare/resume passed 24/46 assertions using actual managed-field
deserialization and separate server JVMs. Chunk checks passed 25 assertions for
natural eviction, exact paused countdown restoration, no implicit reload,
deferred hatch removal and preservation of unrelated replacement identities.
The long fixture also verifies genuine owned-hatch unload while its controller
keeps ticking, unchanged deadline progression, expiry without force-loading,
and deferred removal of the remote hatch after its controller has gone.

An isolated graphical client verified all tier menus and coefficients, live
updates without reopening, unsafe values and countdowns, warning removal after
cooling, and menu closure on destruction. The automated real-client suite passed
13 assertions with no client controller chunk/instance, live remote values,
invalidation/reattachment, and menu closure plus client sound-engine receipt for
both expiry and immediate dismantling. All four hatch tiers also retain their
identities and coefficients across the separate-JVM persistence test.

Data generation completed its providers after correcting an addon/upstream model
namespace mismatch, but the baseline non-daemon-thread shutdown hang remains:
the process required termination and is not counted as a clean `runData` pass.
The user accepted a bounded datagen run: `timeout --signal=TERM --kill-after=10s 120s
./gradlew runData --no-daemon --console=plain` completed all providers and cache
writes, then exited 124 on shutdown timeout. This is not a clean process exit.
The dedicated server starts and shuts down after the assertions, but startup logs
include dependency-side Rhino reflection errors on client-signature methods in
GTCEu/LDLib classes. These unchanged pinned-dependency errors and the datagen
shutdown hang are explicitly accepted exceptions, not heat-feature fixes.
Consult the OpenSpec task checkboxes for completion, not the presence of
implementation files or a successful Gradle exit alone.
