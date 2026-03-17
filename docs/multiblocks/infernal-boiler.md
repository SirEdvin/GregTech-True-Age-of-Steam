# Infernal Boiler

<div class="grid" markdown>
<figure markdown>
![Infernal Boiler](../images/infernal-boiler.png)
<figcaption>Infernal Boiler</figcaption>
</figure>

| | |
|---|---|
| **Type** | Multiblock |
| **Voltage tier** | MV |
| **Output** | Superheated Steam |
| **Key mechanic** | Heat levels + Charges |
</div>

Infused with the power of blazes, the Infernal Boiler is the centrepiece of the addon's steam production. It produces **superheated steam** rather than ordinary steam, making it far more energy-dense than vanilla GregTech boilers.

## Mechanics

The boiler has two independent systems that interact to determine its output: **Heat** and **Charges**.

### Heat

Each completed crafting operation increases the boiler's heat. Heat is divided into levels, and crossing a threshold raises the current level.

| Effect | Description |
|--------|-------------|
| Higher heat level | More parallel recipe executions |
| **Supreme level** | Boiler begins *overheating* eligible recipes, multiplying fluid output |
| Idle cooldown | When not working, heat drops — rate depends on installed coils |

The overheat output multiplier at Supreme level follows the formula:

```
output × (3 + coil_level)
```

Better coils slow the idle heat decay, making it easier to sustain Supreme level between batches.

### Charges

The boiler requires charges to operate. When charges run out, it can **only** process recipes that restore charges — all other recipes are locked until the charge is replenished.

## Optimal operation

For maximum throughput:

1. Keep the boiler running **continuously** to maintain Supreme heat level.
2. Periodically run a charging recipe before charges are fully depleted to avoid downtime.

!!! warning
    Allowing charges to deplete fully will interrupt production. Plan your charging recipe schedule around your boiler's consumption rate.

## Unlock & tier

The Infernal Boiler is locked behind **MV**, because the infernal alloy it requires needs aluminium ingots.

## Modpack developer notes

??? info "Tuning information (modpack developers)"
    The Infernal Boiler is tuned to produce approximately **2670 EU/t** in superhot steam when running at MV with Cupronickel coils. Better coils yield proportionally better results.

    Tunable parameters:

    - **Superheat coefficient** — adjustable in the mod's configuration file.
    - **Recipe values** — edit recipes directly to change base output numbers.

    A community-maintained [tuning spreadsheet](https://docs.google.com/spreadsheets/d/1y3mynehGAV7hHjiIa9eH1HNlcbUSV-bjoIrED02l3pM/edit?usp=sharing) is available to help calculate balanced values.

    !!! note
        After changing recipes, **rebuild the boiler structure**. The boiler caches the matched recipe and reuses it for consecutive runs (standard GregTech CEu behaviour). A rebuild clears the cache and picks up the new recipe.
