# Industrial Coating Line

<div style="float: right; margin: 0 0 1rem 1.5rem; max-width: 260px;" markdown>
| | |
|---|---|
| **Type** | Multiblock |
| **Unlock at** | HV |
| **Energy input** | HV |
</div>

The Industrial Coating Line is the **HV-tier upgrade** of the [Coating Shrine](./coating-shrine.md). It delivers far higher throughput and no longer consumes the fluid source — instead it draws a small amount from fluid input hatches.

## How it works

The base structure is a **3×3×3 cube**: a front face with the controller and I/O hatches, one or more middle fluid-cell layers, and a solid back wall.

- Place a fluid source block in each middle cell. Each cell can hold a **different fluid**, enabling the machine to run multiple coating recipes simultaneously.
- Connect a **fluid input hatch** on the front face and supply the required fluid — **2 mB is consumed per craft** (the source block in the cell is never depleted).
- The number of parallels equals the **count of cells holding the fluid required by the current recipe**. A machine with 3 lava cells and 2 ice cells can run 3 lava-coating parallels or 2 ice-coating parallels.

## Expanding the structure

Extend the machine by adding more middle fluid-cell layers behind the first. Up to **12 fluid cells** are supported, giving a maximum machine depth of 14 blocks.

!!! tip
    All recipes from the Coating Shrine are automatically available here. The machine accepts the same items, but requires 2 mB of the appropriate fluid via a hatch rather than a fluid source block at the center.
