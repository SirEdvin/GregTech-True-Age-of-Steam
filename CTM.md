# Connected Textures (CTM) Guide

A practical reference for authoring Connected Textures for GregTech-CEu / LDLib casings in this mod. Based on reading the LDLib source (`CustomBakedModel`, `Connections`, `Submap`, `Quad`) and iterating on the `bathing_infused_casing` texture.

Use this guide when adding CTM to any new casing block.

## TL;DR

1. Put the normal 16×16 base texture at `.../textures/block/<name>.png`.
2. Write `<name>.png.mcmeta` pointing to a `<name>_ctm` sprite.
3. Author a 32×32 `<name>_ctm.png` in the **4×4 grid of 8×8 tiles** layout described below.
4. Only fill the 12 meaningful tile slots (idx 0-9, 12-13); slots 10, 11, 14, 15 are never sampled but fill them with interior content for cleanliness.
5. The four 16×16 "quadrants" of the same image act as the disconnected fallback (idx 16-19) but they sample the **base** sprite, not the CTM — so you never need to draw them in the CTM file.

## File layout

```
textures/block/
    <name>.png              16×16   standalone block appearance
    <name>.png.mcmeta       json    points at the _ctm sprite
    <name>_ctm.png          32×32   connection variants (this guide is about this file)
```

`<name>.png.mcmeta`:

```json
{
    "ldlib": {
        "connection": "<modid>:block/<name>_ctm"
    }
}
```

Additional flags that can live alongside `ldlib.connection`:

- `"ldlib": { "emissive": true }` — makes the sprite bloom-capable.
- `"shimmer": { "bloom": true }` — flag picked up by the shimmer renderer.
- `"animation": { ... }` — standard Minecraft animated texture, works *with* CTM if you build a matching animated `_ctm.png` with the same frame count (each frame is a full 32×32 CTM stacked vertically). Mixing CTM + animation is possible but tricky; the safer default is to leave animated sprites without a connection entry.

Do **not** use an alpha channel in the CTM file unless you actually want transparency — the sprite is sampled directly into the block face.

## How LDLib renders a connected face

Relevant source: `com.lowdragmc.lowdraglib.client.model.custommodel.CustomBakedModel`

For every block face, LDLib:

1. Calls `Connections.checkConnections(...)` which walks the 8 neighbouring positions (`UP`, `DOWN`, `LEFT`, `RIGHT`, and the 4 diagonals) and records which neighbours share the same connection predicate.
2. Splits the face quad into 4 sub-quads — one per face corner.
3. For each sub-quad, picks a **submap index** based on the neighbours touching that corner, then re-maps the sub-quad's UVs into that submap region.

Quadrant numbering (`Quad.UVs#getQuadrant()` — note this is geometry-based, not "reading order"):

| Quadrant index | Face corner | Connection directions checked (`submapMap`) | Base offset (`submapOffsets`) |
| --- | --- | --- | --- |
| 0 | Bottom-left  (BL) | `DOWN`, `LEFT`,  `DOWN_LEFT`  | 4 |
| 1 | Bottom-right (BR) | `DOWN`, `RIGHT`, `DOWN_RIGHT` | 5 |
| 2 | Top-right    (TR) | `UP`,   `RIGHT`, `UP_RIGHT`   | 1 |
| 3 | Top-left     (TL) | `UP`,   `LEFT`,  `UP_LEFT`    | 0 |

### Index selection (`Connections.fillSubmaps`)

For each face corner, let `dirs = { primary, secondary, diagonal }` from `submapMap`:

```
if NOT connected to primary AND NOT connected to secondary:
    use the "disconnected" fallback index  (16, 17, 18, 19 — see below)
else if connected to both primary AND secondary:
    use submapOffsets[quadrant]            (0, 1, 4, or 5)
else:
    use submapOffsets[quadrant]
        + (2 if connected to primary   else 0)
        + (8 if connected to secondary else 0)
```

The diagonal connection (`UP_LEFT`, etc.) is recorded but does **not** change the submap index — the 2×2 CTM format intentionally ignores it. If you need diagonal-aware CTM you need a different renderer.

### The 16 connected submaps (indices 0-15)

Each submap is an 8×8 pixel tile. The CTM is a 4×4 grid of these tiles, so index `n` lives at pixels `(8 * (n % 4), 8 * (n // 4))`.

| idx | Grid (col, row) | Pixels            | Face corner | Primary connected | Secondary connected |
| --- | --- | --- | --- | --- | --- |
| 0   | (0, 0) | (0..7,   0..7)   | TL | UP   | LEFT  |
| 1   | (1, 0) | (8..15,  0..7)   | TR | UP   | RIGHT |
| 2   | (2, 0) | (16..23, 0..7)   | TL | UP   | —     |
| 3   | (3, 0) | (24..31, 0..7)   | TR | UP   | —     |
| 4   | (0, 1) | (0..7,   8..15)  | BL | DOWN | LEFT  |
| 5   | (1, 1) | (8..15,  8..15)  | BR | DOWN | RIGHT |
| 6   | (2, 1) | (16..23, 8..15)  | BL | DOWN | —     |
| 7   | (3, 1) | (24..31, 8..15)  | BR | DOWN | —     |
| 8   | (0, 2) | (0..7,   16..23) | TL | —    | LEFT  |
| 9   | (1, 2) | (8..15,  16..23) | TR | —    | RIGHT |
| 10  | (2, 2) | (16..23, 16..23) | unused |   |       |
| 11  | (3, 2) | (24..31, 16..23) | unused |   |       |
| 12  | (0, 3) | (0..7,   24..31) | BL | —    | LEFT  |
| 13  | (1, 3) | (8..15,  24..31) | BR | —    | RIGHT |
| 14  | (2, 3) | (16..23, 24..31) | unused |   |       |
| 15  | (3, 3) | (24..31, 24..31) | unused |   |       |

Mental shortcut:

- **Row 0** — top face corners with `UP` connected.
- **Row 1** — bottom face corners with `DOWN` connected.
- **Row 2** — top face corners with only horizontal connection.
- **Row 3** — bottom face corners with only horizontal connection.
- **Col 0, 1** — both primary and secondary directions are connected (fully interior corners).
- **Col 2, 3** — only the vertical direction is connected (half-connected, with a horizontal border showing).

### The 4 disconnected fallbacks (indices 16-19)

When both primary and secondary are disconnected, the quadrant falls back to one of these:

| idx | Quadrant | Comes from |
| --- | --- | --- |
| 16  | TL | base sprite top-left 8×8    |
| 17  | TR | base sprite top-right 8×8   |
| 18  | BL | base sprite bottom-left 8×8 |
| 19  | BR | base sprite bottom-right 8×8 |

Crucial detail: `CustomBakedModel.buildCustomQuads` does `ctm[ctmid] > 15 ? bakedQuad.getSprite() : connection`. When the index is ≥ 16 it samples the **base sprite**, not the CTM. So you do **not** need to draw anything in the CTM for disconnected corners — the renderer reads from the base file. This is why a block in full isolation always looks exactly like the base texture.

## Authoring the 12 meaningful tiles

Every 8×8 tile represents one face corner with a specific connection state. Design each tile as the base's corresponding 8×8 quadrant with the border preserved on the **disconnected** sides and replaced with interior content on the **connected** sides.

For each corner (TL / TR / BL / BR), identify which 2 sides of the tile are "external" (border visible) vs "internal" (no border). Then build the tile.

### Per-tile rule table

| idx | Face corner | Show border on | Hide border on |
| --- | --- | --- | --- |
| 0   | TL | —           | top + left  |
| 1   | TR | —           | top + right |
| 4   | BL | —           | bottom + left |
| 5   | BR | —           | bottom + right |
| 2   | TL | left        | top (+ right/bottom since they're not borders of the TL quadrant) |
| 3   | TR | right       | top |
| 6   | BL | left        | bottom |
| 7   | BR | right       | bottom |
| 8   | TL | top         | left |
| 9   | TR | top         | right |
| 12  | BL | bottom      | left |
| 13  | BR | bottom      | right |

Remember that only certain edges of an 8×8 face-corner tile correspond to the base's actual borders (the base border is along rows 0/15 and cols 0/15 of the 16×16 base):

- TL tile → base rows 0-1 (top border) and cols 0-1 (left border)
- TR tile → rows 0-1 and cols 6-7 (of the tile, = base cols 14-15)
- BL tile → rows 6-7 and cols 0-1
- BR tile → rows 6-7 and cols 6-7

### Corner accents

Casings in this mod put a single light-blue accent pixel at each of `(1, 1)`, `(14, 1)`, `(1, 14)`, `(14, 14)` of the base (translates to `(1, 1)`, `(6, 1)`, `(1, 6)`, `(6, 6)` of each 8×8 corner tile).

These accents exist to mark the 4 corners of a block group. You want them in the **disconnected fallback** (which already uses the base) and **nowhere else**. When you copy border pixels into a half-connected CTM tile, strip the accent pixel and replace it with the adjacent inner-ring colour — otherwise the accent appears on every edge seam, making the wall look sprinkled.

Lesson from the bathing casing: the first pass left accents in the half-connected tiles, producing four dots at every internal seam. Stripping them fixed it.

### Interior (fully-connected) tiles

Indices 0, 1, 4, 5 render only when a face corner has both sides connected — i.e. the corner is in the middle of a multi-block wall. These tiles are tiled *many* times across large walls, so busy or asymmetric content reads as visual noise.

Two practical strategies:

1. **Sample the base's deep interior** (e.g. `base[4..11, 4..11]`). This preserves the original artistic language but tiles *every* interior detail — great for textures with a genuinely seamless background, poor for textures with a strong centred motif (a single diamond, a large logo, etc.), because the motif repeats and reads as clutter.
2. **Author a calm 2-tone filler** using the base's palette. For the bathing casing we used a medium-blue background `(22,102,143)` with a light-blue `(55,132,162)` diagonal — no dark dots. This was the final choice after the user asked for "more smooth colour".

Either way: **no dark navy, no dark inner-ring, no accent** pixels in the interior tiles. The whole point is that adjacent interior tiles join without visible seams.

### Half-connected tiles (idx 2, 3, 6, 7, 8, 9, 12, 13)

These render on a block that sits on the **edge** of a connected group — one side connects, the perpendicular side terminates. They need:

- The base's 2-pixel border layer (outer dark navy + inner ring) on the **disconnected** side.
- Interior content (same as the fully-connected tiles) on the **connected** side.
- No corner accent anywhere.

Concretely, for each pixel in the 8×8 tile:

```
if pixel is in the disconnected-edge border region AND not an accent position:
    copy from base
else if pixel is in the disconnected-edge border region AND IS an accent position:
    copy from an adjacent inner-ring pixel (strip the accent)
else:
    copy from the interior filler
```

### Unused slots

Idx 10, 11, 14, 15 are never sampled by `fillSubmaps`. Fill them with the interior filler so you don't see weird pixels in tooling that shows the whole atlas.

## Worked recipe (Python / Pillow)

This is the approach the bathing casing ships with. Adapt `BASE_PATH`, `OUT_PATH`, and `INTERIOR_LIGHT` / `INTERIOR_MEDIUM` to your texture's palette.

```python
from PIL import Image

BASE_PATH = ".../textures/block/<name>.png"
OUT_PATH  = ".../textures/block/<name>_ctm.png"

base = Image.open(BASE_PATH).convert("RGBA")
assert base.size == (16, 16)

# Smooth filler — no dark dots, no accents, just two interior tones.
INTERIOR_MEDIUM = (22, 102, 143, 255)
INTERIOR_LIGHT  = (55, 132, 162, 255)
INTERIOR = Image.new("RGBA", (8, 8), INTERIOR_MEDIUM)
for i in range(8):
    INTERIOR.putpixel((i, i), INTERIOR_LIGHT)

QUAD_ORIGIN = {"TL": (0, 0), "TR": (8, 0), "BL": (0, 8), "BR": (8, 8)}
ACCENT_POS  = {"TL": (1, 1), "TR": (6, 1), "BL": (1, 6), "BR": (6, 6)}

def base_px(x, y):
    return base.getpixel((max(0, min(15, x)), max(0, min(15, y))))

def border_tile(quadrant, *, show_top=False, show_bottom=False,
                show_left=False, show_right=False):
    ox, oy = QUAD_ORIGIN[quadrant]
    out = Image.new("RGBA", (8, 8))
    for ty in range(8):
        for tx in range(8):
            in_top    = (ty <= 1) and quadrant in ("TL", "TR")
            in_bottom = (ty >= 6) and quadrant in ("BL", "BR")
            in_left   = (tx <= 1) and quadrant in ("TL", "BL")
            in_right  = (tx >= 6) and quadrant in ("TR", "BR")

            keep = (show_top and in_top) or (show_bottom and in_bottom) \
                or (show_left and in_left) or (show_right and in_right)

            if not keep:
                out.putpixel((tx, ty), INTERIOR.getpixel((tx, ty)))
                continue

            if (tx, ty) == ACCENT_POS[quadrant]:
                # Strip the corner accent — use the adjacent inner-ring pixel.
                bx, by = ox + tx, oy + ty
                if show_left or show_right:
                    out.putpixel((tx, ty), base_px(bx, by + (1 if ty <= 1 else -1)))
                else:
                    out.putpixel((tx, ty), base_px(bx + (1 if tx <= 1 else -1), by))
            else:
                out.putpixel((tx, ty), base_px(ox + tx, oy + ty))
    return out

ctm = Image.new("RGBA", (32, 32))

def paste(idx, tile):
    ctm.paste(tile, ((idx % 4) * 8, (idx // 4) * 8))

# Fully-connected interiors.
for idx in (0, 1, 4, 5):
    paste(idx, INTERIOR.copy())

# UP only (top connected, horizontal disconnected → horizontal border).
paste(2, border_tile("TL", show_left=True))
paste(3, border_tile("TR", show_right=True))

# DOWN only.
paste(6, border_tile("BL", show_left=True))
paste(7, border_tile("BR", show_right=True))

# LEFT only (horizontal connected, vertical disconnected → vertical border).
paste(8,  border_tile("TL", show_top=True))
paste(12, border_tile("BL", show_bottom=True))

# RIGHT only.
paste(9,  border_tile("TR", show_top=True))
paste(13, border_tile("BR", show_bottom=True))

# Unused — keep the atlas clean.
for idx in (10, 11, 14, 15):
    paste(idx, INTERIOR.copy())

ctm.save(OUT_PATH)
```

## Common mistakes (learned the hard way on `bathing_infused_casing`)

- **Treating the CTM as a 2×2 of 16×16 tiles.** That's the *disconnected* layout, and those slots are never read from the CTM file anyway. The actual layout the renderer samples is 4×4 of 8×8 tiles. Walls rendered against a 2×2 layout look like the base texture stamped repeatedly because every corner of every face is falling through to the 16×16 fallback.
- **Leaving the corner accents on half-connected tiles.** Produces a cross of four bright dots at every internal seam.
- **Copying raw interior pattern into the fully-connected tiles on textures with a centred motif.** The motif repeats and reads as clutter on big walls; author a calm 2-tone filler instead.
- **Duplicating rows/cols when "shifting inward" to fill a removed border.** A formula like `shift = 2 - ty` maps `ty = 0` and `ty = 1` to the same row. Use a constant shift of 2 (or a mirror `shift = 3 - 2*ty`) so the two removed rows sample distinct source rows.
- **Forgetting to update `src/generated/resources/.../machine/<name>.json`** when you change the appearance block or overlay for a multiblock machine via `workableCasingModel(...)`. Either run the data-gen task or patch the generated JSON in place.

## Previewing without starting Minecraft

To visualise what a real face will look like, you need to replicate the `fillSubmaps` selection logic. This Python sketch renders a 3×3 block wall from the CTM file plus the base:

```python
def pick_idx(face_idx, primary_connected, secondary_connected):
    offsets  = [4, 5, 1, 0]                 # BL, BR, TR, TL
    fallback = [18, 19, 17, 16][face_idx]
    if primary_connected or secondary_connected:
        if primary_connected and secondary_connected:
            return offsets[face_idx]
        return offsets[face_idx] \
            + (2 if primary_connected   else 0) \
            + (8 if secondary_connected else 0)
    return fallback

def get_tile(ctm, base, idx, face_idx):
    if idx > 15:
        # Fallback: 8x8 from base corresponding to the face corner.
        origin = [(0, 8), (8, 8), (8, 0), (0, 0)][face_idx]  # BL, BR, TR, TL
        return base.crop((*origin, origin[0] + 8, origin[1] + 8))
    return ctm.crop(((idx % 4) * 8, (idx // 4) * 8,
                     (idx % 4) * 8 + 8, (idx // 4) * 8 + 8))

def render_face(ctm, base, up, down, left, right):
    face = Image.new("RGBA", (16, 16))
    face.paste(get_tile(ctm, base, pick_idx(3, up,   left),  3), (0, 0))  # TL
    face.paste(get_tile(ctm, base, pick_idx(2, up,   right), 2), (8, 0))  # TR
    face.paste(get_tile(ctm, base, pick_idx(0, down, left),  0), (0, 8))  # BL
    face.paste(get_tile(ctm, base, pick_idx(1, down, right), 1), (8, 8))  # BR
    return face
```

Iterating on the preview images catches the vast majority of authoring mistakes before you reload the resource pack in Minecraft.
