# Apply checkpoint

Status: implementation and acceptance checks complete with the two user-approved
environmental exceptions below. Consult tasks.md for the authoritative checklist.
Changes remain uncommitted; the change has not been archived.

## Implemented

- Opt-in controller API, managed energy/melting/countdown persistence, authoritative
  mutations, structure callbacks and post-exchange countdown finalization.
- HV/EV/IV/LuV hatches, validated ownership, sender-only coefficients, synchronized
  read-only thermal UI and generated production resources.
- Fresh bounded vent BFS, stable pair grouping, strongest connected sending hatch,
  conservative fractional transfer and guarded identity-based destruction ledger.
- Isolated development source/resources, runnable fixture and placement/heat
  commands, loader-aware suites and docs/modpacks/heat-multiblocks.md.

## Observed verification

- Build, fixture compilation/resources, Spotless and pure JUnit tests passed.
- Opt-in HeatClientChecks passed 13 real integrated-client assertions: the
  controller chunk and instance are absent on the client; the same open hatch menu
  receives heat, limits, melting/recovery, invalidation and reattachment updates;
  expiry and immediate dismantling both close it and deliver exactly one nearby
  hatch explosion sound to the client sound engine. OpenAL used its null output
  backend because no physical audio device was available; packets were not mocked.
  The client saved and exited normally. Report: run-heat-test/heat-client-results.json.
- Lifecycle report: 152 passing assertions. The complete suite runs with original
  and reversed controller/hatch registration, including sufficient/insufficient
  final-tick cooling and separate casing/controller/hatch dismantling cases.
  Server sound events occur once per destroyed target, without repeated/reentrant
  sounds or sounds after rescue/recovered dismantling. Nearby vulnerable entities
  retain health, position and zero velocity, and neither they nor surrounding
  blocks catch fire. Server events do not establish connected-client sound receipt.
- Network report: 395 passing assertions, including all sender/receiver tiers,
  duplicate paths, invalid ownership/configuration, display snapshots and
  three-controller fan-out with an intra-update donor reversal. Reversed
  registration order produces the same independently calculated final energies.
- Separate-JVM persistence prepare/resume reports: 24/46 passing assertions.
  Overcapacity energy, identities and exact countdown survive; invalid saved
  values normalize without discarding finite overcapacity heat.
- Real chunk eviction/reload report: 25 passing assertions. Countdown pauses
  offline and resumes per entity-ticking tick; unloaded targets stay unloaded;
  deferred owned hatch removal completes after reload; unrelated identities survive.
  The test waits for the unload event and three actual resumed active ticks, not
  an assumed fixed wall/game-tick chunk activation delay. Passed two consecutive
  runs after correcting that test timing assumption.
- Natural eviction exposed world-level lookup ticket renewal. Heat discovery,
  owner lookup and removal reconciliation now read already-loaded chunks directly;
  fixture characteristic getters use the same non-loading approach. Pending
  removal waits until getChunkNow returns a chunk, rather than treating a not-yet
  available chunk as a missing machine.
- A development-only long fixture places its owned hatch 256 blocks away.
  Actual ChunkEvent.Unload plus absent chunk lookup confirms remote hatch eviction
  while the controller stays entity-ticking. The countdown remains at 20 after
  20 active ticks and expires at 40; the hatch stays unloaded until explicitly
  reloaded, then its retained identity is removed despite the controller being gone.
  Adjacent remote casing survives. A three-chunk separation was insufficient:
  Minecraft retained that chunk at ticket level 34 without a genuine unload event.
- Private graphical client: fixture/controller models and all four hatch menus
  rendered; coefficients and thermal units appeared; an already-open menu updated
  after scheduled heating, displayed melting, cleared its warning after scheduled
  cooling, and closed when its hatch was destroyed. The client was saved/exited.
- Production packaging was checked for hatch assets and exclusion of development
  classes/resources. No existing production cooling/boiler patterns or recipes
  were deliberately changed.

Reports live in ignored run-heat-test/; rerun commands and destructive fixture
coordinates are documented in docs/modpacks/heat-multiblocks.md. Require fresh
report files with passed=true: orderly server shutdown alone does not prove success.

## Accepted environmental exceptions

The user explicitly instructed: "Just ignore this issues, for data generations
just use timeout and continue working". The following are accepted exceptions,
not claims that the underlying dependency problems have been fixed:

1. Data providers completed after fixing the hatch model namespace, but the
   baseline runData process retained a non-daemon thread and needed termination.
   This is not a successful clean runData exit.
2. Dedicated-server startup still reports client-classloading errors. Functional
   server suites do not establish a clean common-side smoke test.
   Log attribution: Rhino's declared-method reflection fails on GTCEu
   GTMaterialBlocks/GTMaterialItems (ItemColor), GTItems (ItemPropertyFunction),
   and LDLib widget classes (GuiGraphics/WorldSceneRenderer). Resolving dependency
   compatibility is outside the pinned/no-new-mixins feature scope. The datagen
   executor owner remains unproven; do not patch speculative shutdown behavior.

The bounded 120-second runData run completed all providers and cache writing
(written: 0), then exited 124 on the known shutdown hang. Build and Spotless passed
afterward. Production JAR inspection found all 12 hatch assets and no fixture or
JUnit entries. OpenSpec strict validation and git diff --check passed.

Final network coverage adds real-world inclusive 32/exclusive 33 range, live
rotation and vent reconnection, unsupported owners/tiers, intermediate chunks
unavailable with both endpoints loaded, and endpoint chunks unavailable with
source/intermediate loaded. Reconnection transfers one scheduled package without
offline catch-up and conserves energy. These FULL-chunk availability tests complement
the separate genuine ChunkEvent.Unload/eviction tests; they do not conflate the two.
Both attached hatch positions are broken across the two lifecycle passes, verifying
controller/sibling removal and one sound per target. Persistence additionally
verifies all four tier identities/coefficients and restored display countdown.

EULA acceptance is already authorized and recorded only in run-heat-test.
