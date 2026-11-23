# Third-party notices

Immersive Munitions is free software, licensed under the **GNU General Public License v3.0 or later** (see [LICENSE](LICENSE)), except for the third-party material listed below. That material stays under its original license and belongs to its original authors.

Immersive Munitions is an unofficial addon. It is not made by or endorsed by the authors of Immersive Engineering, Timeless and Classics Zero (TaCZ), Scorched Guns, or Immersive Geology.

---

## Scorched Guns Brass, by Ribs (GPLv3)

- **What we use:** The zinc and brass textures (ores, raw zinc, ingots, nuggets, storage blocks) are recolored derivatives of Scorched Guns Brass textures. `tools/gen_resources.py` produces them.
- **License:** GNU GPL v3. Our derived textures are distributed under the same license.
- **Where:** `src/main/resources/assets/immersive_munitions/textures/block/`, `src/main/resources/assets/immersive_munitions/textures/item/`

## Timeless and Classics Zero (TaCZ), by Serene Wave Studio / Timeless Squad

TaCZ is licensed **GPL v3 for code** and **CC BY-NC-ND 4.0 for assets**.

- **Code (GPL v3):** The creative-menu side tabs (`src/main/java/dev/immersive_munitions/client/CreativeSectionTabs.java`) adapt TaCZ's creative sidebar tab code: how the tabs are laid out and how their sprite UVs are picked. This is allowed under the GPL v3, and the adapted code is distributed under the GPL v3 as part of this mod. The mod also calls TaCZ's public API (`com.tacz.guns.api`) to build real TaCZ ammo items.
- **Assets (CC BY-NC-ND 4.0):** The 32x8 caliber label in the top-left corner of the casing, bullet, round and mold sprites is copied pixel-for-pixel from TaCZ's ammo slot icons (`assets/tacz/textures/ammo/slot/<id>.png`). All rights to those labels belong to the TaCZ authors. They are not covered by this mod's GPL license, and they are credited to TaCZ here. Immersive Munitions is free and non-commercial. CC BY-NC-ND does not allow modified versions to be shared, so the maintainer intends to get the TaCZ authors' permission for this use, or replace the labels with original ones.
- **Where:** `src/main/resources/assets/immersive_munitions/textures/item/ammo/`, `.../textures/item/molds/`, `src/main/resources/resourcepacks/immersive_munitions_ammo/`, `art/`
- **Source:** https://github.com/MCModderAnchor/TACZ

## Immersive Engineering, by BluSunrize and Damien A.W. Hazard

Licensed under **Blu's License of Common Sense**: https://github.com/BluSunrize/ImmersiveEngineering/blob/1.20.1/LICENSE

- **What we use:** The mod is an Immersive Engineering addon. It uses IE's API and machines, and its metal-press recipes follow the forms IE defines. No Immersive Engineering code or texture files are copied into this repository. The 64x64 mold sprites are original art made to match the look of IE's molds.
- The license allows addons built on IE and its API. Its rules for addons are followed here: credit is given, the source is public, and nothing is monetized.
- **Source:** https://github.com/BluSunrize/ImmersiveEngineering

## Immersive Geology

- **What we use:** Optional compatibility only. Zinc ore generation turns itself off when Immersive Geology is installed. No Immersive Geology code or assets are included.

## Minecraft and Forge

Minecraft is © Mojang Studios / Microsoft. Forge is © the MinecraftForge team. This mod uses Mojang's official mappings under Mojang's mapping license. Neither project's code or assets are redistributed here.

---

If you are a rights holder and want something here changed or removed, please open an issue on this repository.
