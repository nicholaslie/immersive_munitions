# Ammo art, 64x64 (ietacz)

For every TACZ ammo id that has a casing, there are four sprites. All are 64x64 RGBA, hard-edged (alpha 0 or 255), and use 48 colors in total (six 8-tone ramps).

| Folder | File | What it is |
|---|---|---|
| `casings/` | `casing_<id>.png` | Empty casing standing upright, open mouth visible |
| `casings_sideways/` | `casing_<id>_side.png` | Empty casing at a 3/4 angle, mouth to the right: for ejection particles and spent-brass items |
| `rounds/` | `round_<id>.png` | Loaded cartridge (casing + bullet) |
| `bullets/` | `bullet_<id>.png` | The bullet / projectile on its own |

## Calibers (25 TACZ ids)

`rpg_rocket` is the one TACZ ammo id with no casing, so it has only `rounds/round_rpg_rocket.png`, drawn by `art/labels/Rocket.java` with the same ramps and lighting. (`20x82` has a slot icon in the pack but no ammo display and no gun uses it; it's skipped.)

| TACZ id | Cartridge | Case | Bullet |
|---|---|---|---|
| `22wmr` | .22 WMR | Brass, rimmed, straight | Copper jacket, round nose |
| `300blk` | .300 Blackout | Brass, rimless (extractor groove), bottleneck | Copper jacket, spitzer nose |
| `308` | .308 / 7.62x51 | Brass, rimless (extractor groove), bottleneck | Copper jacket, spitzer nose |
| `30_06` | .30-06 | Brass, rimless (extractor groove), bottleneck | Copper jacket, spitzer nose |
| `338` | .338 Lapua | Brass, rimless (extractor groove), bottleneck | Copper jacket, spitzer nose |
| `357mag` | .357 Magnum | Brass, rimmed, straight | Copper jacket, flat nose |
| `45_70` | .45-70 Government | Brass, rimmed, straight | Copper jacket, flat nose |
| `45acp` | .45 ACP | Brass, rimless (extractor groove), straight | Copper jacket, round nose |
| `46x30` | 4.6x30 | Brass, rimless (extractor groove), bottleneck | Copper jacket, spitzer nose |
| `500mag` | .500 S&W Magnum | Brass, rimmed, straight | Copper jacket, flat nose |
| `50ae` | .50 AE | Brass, rimless (extractor groove), straight | Copper jacket, round nose |
| `50bmg` | .50 BMG | Brass, rimless (extractor groove), bottleneck | Copper jacket, spitzer nose, dark tip |
| `545x39` | 5.45x39 | Brass, rimless (extractor groove), bottleneck | Copper jacket, spitzer nose |
| `556x45` | 5.56x45 | Brass, rimless (extractor groove), bottleneck | Copper jacket, spitzer nose |
| `57x28` | 5.7x28 | Brass, rimless (extractor groove), bottleneck | Copper jacket, spitzer nose |
| `58x42` | 5.8x42 | Brass, rimless (extractor groove), bottleneck | Copper jacket, spitzer nose |
| `68x51fury` | 6.8x51 FURY | Brass, rimless (extractor groove), bottleneck | Copper jacket, spitzer nose |
| `6x43` | 6x43 | Brass, rimless (extractor groove), bottleneck | Copper jacket, spitzer nose |
| `762x25` | 7.62x25 Tokarev | Brass, rimless (extractor groove), bottleneck | Copper jacket, spitzer nose |
| `762x39` | 7.62x39 | Brass, rimless (extractor groove), bottleneck | Copper jacket, spitzer nose |
| `762x54` | 7.62x54R | Brass, rimmed, bottleneck | Copper jacket, spitzer nose |
| `792x57` | 7.92x57 | Brass, rimless (extractor groove), bottleneck | Copper jacket, spitzer nose |
| `9mm` | 9x19mm | Brass, rimless (extractor groove), straight | Copper jacket, round nose |
| `12g` | 12 gauge | Red plastic hull, brass head, fluted. Loaded: crimped top | Lead buckshot: a pile of 10 round pellets (bullet sprite only) |
| `40mm` | 40mm grenade | Short wide brass cup | Olive grenade with copper driving band |

Extra: `casing_12g_brass.png` and `casing_12g_brass_side.png` are all-brass 12 gauge hulls (old-style brass shell) in case you want one that matches the rest of the brass.

## Caliber label

The sprites in this folder have **no label**. `art/labels/Labels.java` adds the caliber label (a 32x9 black bar with the caliber in a category color) in the top-left corner when it writes the mod textures. The label is original art. The sprites used to carry labels copied from TaCZ's slot icons; those were removed.

## Look and scale

- Each sprite was rendered from a real-dimension profile of the cartridge (rim, groove, body taper, shoulder, neck, bullet), lit from the upper left and quantized to pixel-art tones, so the shapes follow the real case geometry.
- Sizes across calibers are compressed to fit 64 px: order and proportions are right, absolute ratios are not (a .50 BMG is about twice the height of a 9mm in these, not 4.7x).
- Standalone rifle bullets are drawn about 30% wider than true so they read as bullets, not needles. Loaded rounds use true proportions.
- Correction from the earlier 16 px set: .357 Magnum is now drawn rimmed, as it really is (a revolver round).

## Palette (matches the brass ingot ramp where it overlaps)

brass `#3b2a1d #5c432f #8c5f3a #af794f #d7aa5e #eec168 #fbdc7d #fff9bf`; copper, red plastic, grey (lead), olive (grenade) and dark steel ramps are in `source/engine.py`.

## Origin

The sprites are original: built from real cartridge dimensions, not from the AI sheet and not from TACZ textures.

## source/
Optional. `python3 build.py [id ...]` regenerates the unlabeled sprites into `out64/` (needs numpy + Pillow). Edit `calibers.py` to change a size or shape. Copy the results here, then run `java art/labels/Labels.java` from the repo root to update the mod.
