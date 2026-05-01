# KubeJS Cooling Coils

GT: True Age of Steam exposes its cooling coil system to KubeJS so modpack authors can add new coil blocks and use them in custom GTCEu multiblocks.

This page follows the same KubeJS patterns used by the GregTech Modern modpack examples, but uses GT True Steam's cooling coil predicate and machine classes.

## Creating cooling coils

Create cooling coils in `kubejs/startup_scripts`. The block type is `gttruesteam:coil`.

```js title="kubejs/startup_scripts/cooling_coils.js"
StartupEvents.registry('block', event => {
    event.create('superconductive_cooling_coil_block', 'gttruesteam:coil')
        .level(4)
        .coolingCapacity(120000)
        .coolingRate(40)
        .activeCoolingReduction(0.6)
        .coilMaterial(() => GTMaterials.get('neutronium'))
        .texture('kubejs:block/superconductive_cooling_coil')
        .hardness(5)
        .requiresTool(true)
        .material('metal')
})
```

The texture path must point at the inactive texture. The active model also looks for the same texture path with `_bloom` appended, so the example above expects both files:

```text
kubejs/assets/kubejs/textures/block/superconductive_cooling_coil.png
kubejs/assets/kubejs/textures/block/superconductive_cooling_coil_bloom.png
```

| Method | Meaning |
|---|---|
| `level(int)` | Coil tier. Machines can use this for parallel limits, overclock logic, or gating. |
| `coolingCapacity(int)` | Maximum stored passive cooling before machine-specific multipliers. |
| `coolingRate(int)` | Passive cooling generated each tick before machine-specific multipliers. |
| `activeCoolingReduction(float)` | Runtime reduction factor used by active cooling machines. Lower values are stronger. |
| `coilMaterial(() => Material)` | Material used for the coil name and tooltip data. |
| `texture(string)` | Base texture for generated block models. |

Built-in GT True Steam cooling coils use these stats for reference:

| Coil | Level | Capacity | Rate | Active cooling reduction |
|---|---:|---:|---:|---:|
| Frostbite Magnalium | 1 | 10,000 | 5 | 0.9 |
| Cooling Cometal | 2 | 30,000 | 10 | 0.8 |
| Estranged Steel | 3 | 60,000 | 20 | 0.7 |

!!! note
    `gttruesteam:coil` registers the block as a valid cooling coil automatically. Any multiblock pattern using `TrueSteamPredicates.coolingCoils()` will accept both built-in and KubeJS-created cooling coils.

## Custom multiblock using cooling coils

Use `TrueSteamPredicates.coolingCoils()` in the multiblock pattern. If the machine needs to read the selected coil type, construct it with `CoolingCoilElectricMachine` or `CoolingCoilMachine` instead of the default KubeJS multiblock machine.

```js title="kubejs/startup_scripts/cryo_compressor.js"
const CoolingCoilElectricMachine = Java.loadClass('site.siredvin.gttruesteam.machines.shared.cooling.CoolingCoilElectricMachine')
const TrueSteamPredicates = Java.loadClass('site.siredvin.gttruesteam.TrueSteamPredicates')
const ModifierFunction = Java.loadClass('com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction')

GTCEuStartupEvents.registry('gtceu:machine', event => {
    event.create('cryo_compressor', 'multiblock')
        .machine(holder => new CoolingCoilElectricMachine(holder))
        .rotationState(RotationState.NON_Y_AXIS)
        .recipeTypes('compressor')
        .recipeModifiers([
            GTRecipeModifiers.PARALLEL_HATCH,
            (machine, recipe) => {
                const coil = machine.getCoilType()

                return ModifierFunction.builder()
                    .durationMultiplier(coil.getActiveCoolingReduction())
                    .build()
            }
        ])
        .appearanceBlock(GTBlocks.CASING_STEEL_SOLID)
        .pattern(definition => FactoryBlockPattern.start()
            .aisle('CCC', 'CCC', 'CCC')
            .aisle('COC', 'S#S', 'CIC')
            .aisle('CMC', 'CKC', 'CCC')
            .where('K', Predicates.controller(Predicates.blocks(definition.get())))
            .where('M', Predicates.abilities(PartAbility.MAINTENANCE))
            .where('I', Predicates.autoAbilities(definition.getRecipeTypes()))
            .where('O', Predicates.autoAbilities(definition.getRecipeTypes()))
            .where('C', Predicates.blocks(GTBlocks.CASING_STEEL_SOLID.get()))
            .where('S', TrueSteamPredicates.coolingCoils())
            .where('#', Predicates.air())
            .build())
        .workableCasingModel(
            'gtceu:block/casings/solid/machine_casing_solid_steel',
            'gtceu:block/multiblock/vacuum_freezer'
        )
})
```

All cooling coil positions in one formed multiblock must be the same cooling coil type. If the player mixes coil types, GT True Steam reports the same structure error used by its built-in Cooling Box, Cooling Tower, and Regulated Cryo Chamber.

## Passive cooling multiblocks

For machines that should accumulate passive cooling like the built-in Cooling Box, use `PassiveCoolingMachine`.

```js title="kubejs/startup_scripts/passive_chiller.js"
const PassiveCoolingMachine = Java.loadClass('site.siredvin.gttruesteam.machines.shared.cooling.PassiveCoolingMachine')
const TrueSteamPredicates = Java.loadClass('site.siredvin.gttruesteam.TrueSteamPredicates')

GTCEuStartupEvents.registry('gtceu:machine', event => {
    event.create('passive_chiller', 'multiblock')
        .machine(holder => new PassiveCoolingMachine(holder, 2, 3))
        .rotationState(RotationState.NON_Y_AXIS)
        .recipeTypes('gttruesteam:fluid_cooling')
        .appearanceBlock(GTBlocks.CASING_STEEL_SOLID)
        .pattern(definition => FactoryBlockPattern.start()
            .aisle('CCC', 'CSC', 'CCC')
            .aisle('CIC', 'S#S', 'COC')
            .aisle('CMC', 'CKC', 'CCC')
            .where('K', Predicates.controller(Predicates.blocks(definition.get())))
            .where('M', Predicates.abilities(PartAbility.MAINTENANCE))
            .where('I', Predicates.abilities(PartAbility.IMPORT_FLUIDS))
            .where('O', Predicates.abilities(PartAbility.EXPORT_FLUIDS))
            .where('C', Predicates.blocks(GTBlocks.CASING_STEEL_SOLID.get()))
            .where('S', TrueSteamPredicates.coolingCoils())
            .where('#', Predicates.air())
            .build())
        .workableCasingModel(
            'gtceu:block/casings/solid/machine_casing_solid_steel',
            'gtceu:block/multiblock/vacuum_freezer'
        )
})
```

The two constructor numbers are GT True Steam multipliers:

| Argument | Meaning |
|---|---|
| `capacityFactor` | Multiplies `coolingCapacity` to determine maximum stored cooling. |
| `rateFactor` | Multiplies `coolingRate` to determine cooling generated each tick. |

For example, `new PassiveCoolingMachine(holder, 2, 3)` with the built-in Estranged Steel coil stores up to `120,000` cooling and gains `60` cooling per tick while idle.

## Shape info

GTCEu shape info previews can show a specific cooling coil block, just like the GregTech Modern coil multiblock examples show a specific heating coil.

```js
.shapeInfo(controller => MultiblockShapeInfo.builder()
    .aisle('CCC', 'CSC', 'CCC')
    .aisle('CIC', 'S#S', 'COC')
    .aisle('CMC', 'CKC', 'CCC')
    .where('K', controller, Direction.SOUTH)
    .where('C', GTBlocks.CASING_STEEL_SOLID.get())
    .where('S', Block.getBlock('kubejs:superconductive_cooling_coil_block'))
    .where('I', GTMachines.FLUID_IMPORT_HATCH[1], Direction.SOUTH)
    .where('O', GTMachines.FLUID_EXPORT_HATCH[1], Direction.SOUTH)
    .where('M', GTMachines.MAINTENANCE_HATCH, Direction.SOUTH)
    .where('#', Block.getBlock('minecraft:air'))
    .build())
```

## When to use each class

| Class | Use case |
|---|---|
| `CoolingCoilElectricMachine` | Electric multiblocks that need `machine.getCoilType()` in recipe modifiers or display code. |
| `CoolingCoilMachine` | Non-electric workable multiblocks that need `machine.getCoilType()`. |
| `PassiveCoolingMachine` | Passive cooling machines that store cooling capacity while idle and expose `getCoolingCapacity()`. |

Use the plain KubeJS `multiblock` machine only when the pattern needs to accept cooling coils but the machine logic does not need to read coil stats.
