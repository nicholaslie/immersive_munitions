# art/

Source artwork and the scripts that generate it. Nothing in this folder is packaged into the mod. Textures the game uses live under `src/main/resources/assets/immersive_munitions/textures/`; copy finished sprites there when they're ready.

| Folder | Contents |
|---|---|
| `logo.png` | Full-resolution mod logo (the in-game 64x64 copy is `src/main/resources/icon.png`) |
| `sprites/casings/` | `casing_<id>.png`: empty casing standing upright |
| `sprites/casings_sideways/` | `casing_<id>_side.png`: empty casing at a 3/4 angle (spent brass / ejection) |
| `sprites/rounds/` | `round_<id>.png`: loaded cartridge |
| `sprites/bullets/` | `bullet_<id>.png`: projectile on its own |
| `sprites/materials/` | Crafting materials (copper stick, ...) |
| `sprites/source/` | Python generator for all of the above. See `sprites/README.md` |
| `molds/` | 64x64 Immersive Engineering casing molds, per weapon category and per caliber. See `molds/README.md` |
| `archive/` | Older art that is no longer used, kept for reference |

The generator scripts need Python 3 with `numpy` and `Pillow`. Run them from inside their own `source/` folder; they write to scratch folders there (`out64/`, `molds_out/`), which git ignores.
