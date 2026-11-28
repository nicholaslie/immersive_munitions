# Third-party notices

Immersive Munitions is licensed under the **GNU General Public License v3.0 or later** (see [LICENSE](LICENSE)), except for the third-party material below, which stays under its original license.

This is an unofficial addon, not made or endorsed by the authors of Immersive Engineering, Timeless and Classics Zero (TaCZ), Immersive Armorer, Immersive Farming or Immersive Geology.

## Timeless and Classics Zero (TaCZ), by Serene Wave Studio / Timeless Squad

TaCZ's code is GPLv3 and its assets are CC BY-NC-ND 4.0.

- The creative tab section buttons (`client/CreativeSectionTabs.java`) are adapted from TaCZ's creative sidebar code, under the GPLv3.
- The mod calls TaCZ's API (`com.tacz.guns.api`) to create TaCZ ammo items.
- No TaCZ assets are included. The optional icon pack in `resourcepacks/immersive_munitions_ammo/` replaces TaCZ's ammo icons with this mod's own art.

Source: https://github.com/MCModderAnchor/TACZ

## Immersive Engineering, by BluSunrize and Damien A.W. Hazard

Licensed under [Blu's License of Common Sense](https://github.com/BluSunrize/ImmersiveEngineering/blob/1.20.1/LICENSE), which allows addons to use and remix IE's art for the addon, with credit.

- The zinc and brass textures are recolored from IE's metal art with this mod's own palettes (made by `art/materials/Materials.java`): the zinc ore from IE's nickel ore patches; raw zinc and the block of raw zinc from IE's raw lead; the zinc ingot, nugget and block from IE's aluminum ingot, nugget and storage block; the brass ingot, nugget and block from IE's copper ones; the zinc dust and brass plate from IE's metal dust and plate. The zinc ore blocks draw Minecraft's own stone and deepslate under that overlay at runtime; no vanilla texture is included.
- The 64x64 mold sprites are original art drawn to match IE's molds.
- The Ammunition Assembler's model is original, but much of it uses IE's own block textures, referenced by name from IE at runtime: the Automated Engineer's Workbench sheet (its base, ribbed housing, roof ports and trim, which the assembler is styled after), IE's steel, aluminium and blue sheet metal, the conveyor, and treated wood. Its machine-specific details use our own sheet (`textures/block/multiblocks/ammo_assembler.png`, made by `art/assembler/AssemblerSheet.java`), which also holds one tile recolored from IE's steel sheet metal (brightened) and copies of three small pieces of the Automated Engineer's Workbench sheet (its grey trim line, base post and base band), edge-padded so they render without seams.
- The Ammunition Assembler's sounds are the mod's own sound events, but for now they play existing Immersive Engineering sounds (the Automated Assembler and the Metal Press) and Minecraft sounds, referenced by name at runtime (`assets/immersive_munitions/sounds.json`). No sound files are included.
- The Ammunition Assembler's GUI background (`textures/gui/ammo_assembler.png`) is remixed from IE's GUI art: the refinery's steel panel, energy gauge and treated-wood inventory, and the fermenter's item slot frame (made by `art/gui/AssemblerGui.java`). Its screen and menu build on IE's container classes.
- The mod uses IE's API and machines. No IE code is included.

Source: https://github.com/BluSunrize/ImmersiveEngineering

## Immersive Farming, by Etyl

The mod logo (`icon.png`, made by `art/logo/Badge.java`) uses Immersive Farming's badge frame: its wood border, orange trim and panel, taken from its icon with the hoe removed (`art/logo/frame_immersive_farming.png`). The 5.56 round inside is this mod's own sprite. Immersive Farming is released under the MIT License:

> Copyright (c) Etyl and the Immersive Farming contributors
>
> Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
>
> The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
>
> THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

Source: https://github.com/Etyl/ImmersiveFarming

## Immersive Armorer, by Koei

A TaCZ gun pack (CC BY-NC-ND 4.0). None of its assets or data are included. The mod refers to its ammo by id (`immersive_armorer:...`) to make it in IE's machines (`compat/ArmorerCompat.java`), and the icon pack replaces its ammo icons with this mod's own art. Without the pack installed, that support stays hidden.

## Immersive Geology and Create

No Immersive Geology or Create code or assets are included. When either is installed, this mod steps aside for its materials: with Immersive Geology, our zinc ore stops generating and our zinc items are hidden; with Create, our zinc and the brass items Create also makes are hidden the same way (`compat/MaterialCompat.java`).

## Minecraft and Forge

Minecraft is © Mojang Studios / Microsoft and Forge is © the MinecraftForge team. The mod is built with Mojang's official mappings under their license. Neither project's code or assets are redistributed here.

---

If you're a rights holder and want something changed or removed, please open an issue.
