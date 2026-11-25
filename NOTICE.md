# Third-party notices

Immersive Munitions is licensed under the **GNU General Public License v3.0 or later** (see [LICENSE](LICENSE)), except for the third-party material below, which stays under its original license.

This is an unofficial addon, not made or endorsed by the authors of Immersive Engineering, Timeless and Classics Zero (TaCZ) or Immersive Geology.

## Timeless and Classics Zero (TaCZ), by Serene Wave Studio / Timeless Squad

TaCZ's code is GPLv3 and its assets are CC BY-NC-ND 4.0.

- The creative tab section buttons (`client/CreativeSectionTabs.java`) are adapted from TaCZ's creative sidebar code, under the GPLv3.
- The mod calls TaCZ's API (`com.tacz.guns.api`) to create TaCZ ammo items.
- No TaCZ assets are included. The optional icon pack in `resourcepacks/immersive_munitions_ammo/` replaces TaCZ's ammo icons with this mod's own art.

Source: https://github.com/MCModderAnchor/TACZ

## Immersive Engineering, by BluSunrize and Damien A.W. Hazard

Licensed under [Blu's License of Common Sense](https://github.com/BluSunrize/ImmersiveEngineering/blob/1.20.1/LICENSE), which allows addons to use and remix IE's art for the addon, with credit.

- The zinc and brass textures are recolored from IE's metal art with this mod's own palettes (made by `art/materials/Materials.java`): the zinc ore from IE's nickel ore patches; raw zinc and the block of raw zinc from IE's raw lead; the zinc ingot, nugget and block from IE's aluminum ingot, nugget and storage block; the brass ingot, nugget and block from IE's copper ones; the dusts, brass plate, rod and wire from IE's metal dust, plate, stick and wire. The zinc ore blocks draw Minecraft's own stone and deepslate under that overlay at runtime; no vanilla texture is included.
- The 64x64 mold sprites are original art drawn to match IE's molds.
- The Ammunition Assembler's model is original, but parts of it use IE's own block textures (sheetmetal, conveyor, steel grate, treated wood, and the multiblock port tiles), referenced by name from IE at runtime. Its machine-specific details use our own sheet (`textures/block/multiblocks/ammo_assembler.png`), which also holds one tile recolored from IE's steel sheet metal (brightened, made by `art/assembler/AssemblerSheet.java`).
- The Ammunition Assembler's GUI background (`textures/gui/ammo_assembler.png`) is remixed from IE's GUI art: the refinery's steel panel, energy gauge and treated-wood inventory, and the fermenter's item slot frame (made by `art/gui/AssemblerGui.java`). Its screen and menu build on IE's container classes.
- The mod uses IE's API and machines. No IE code is included.

Source: https://github.com/BluSunrize/ImmersiveEngineering

## Immersive Geology (LGPL-3.0)

The brass gear texture is recolored from Immersive Geology's greyscale gear (made by `art/materials/Materials.java`). Zinc ore generation turns itself off when Immersive Geology is installed.

## Minecraft and Forge

Minecraft is © Mojang Studios / Microsoft and Forge is © the MinecraftForge team. The mod is built with Mojang's official mappings under their license. Neither project's code or assets are redistributed here.

---

If you're a rights holder and want something changed or removed, please open an issue.
