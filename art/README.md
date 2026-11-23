# art/

Source artwork and the scripts that generate it. Nothing in this folder is packaged into the mod. Textures the game uses live under `src/main/resources/assets/immersive_munitions/textures/`.

The ammo sprites and molds here are **unlabeled**. To update the mod after changing any of them, run this from the repo root:

```bat
java art/labels/Labels.java
```

It adds the caliber label to each sprite and mold and writes them into the mod and the bundled TaCZ icon pack. It also saves `art/labels/preview.png` so you can check the whole set at a glance.

| Folder | Contents |
|---|---|
| `logo.png` | Full-resolution mod logo (the in-game 64x64 copy is `src/main/resources/icon.png`) |
| `sprites/casings/` | `casing_<id>.png`: empty casing standing upright |
| `sprites/casings_sideways/` | `casing_<id>_side.png`: empty casing at a 3/4 angle (spent brass / ejection) |
| `sprites/rounds/` | `round_<id>.png`: loaded cartridge |
| `sprites/bullets/` | `bullet_<id>.png`: projectile on its own |
| `sprites/materials/` | Crafting materials (copper stick, ...) |
| `sprites/source/` | Python generator for all of the above. See `sprites/README.md` |
| `sprites/rounds/round_rpg_rocket.png` | RPG rocket, drawn by `labels/Rocket.java` (`java art/labels/Rocket.java`) |
| `molds/` | 64x64 Immersive Engineering casing molds, one per weapon category. See `molds/README.md` |
| `labels/` | `Labels.java`: the 32x9 black caliber label, its 4x7 pixel font, the caliber list and category colors, and the tool that writes the labeled textures. `preview.png` shows every result. |
| `archive/` | Older art that is no longer used, kept for reference |

The generator scripts need Python 3 with `numpy` and `Pillow`. Run them from inside their own `source/` folder; they write to scratch folders there (`out64/`, `molds_out/`), which git ignores.
