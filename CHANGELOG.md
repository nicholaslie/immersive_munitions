# Changelog

## Unreleased

Changes since the last release. The next version number follows from them: anything under Added or Changed raises the middle number (0.6.0-beta), only Fixed raises the last (0.5.1-beta).

### Added

### Changed

### Fixed

- The Ammunition Assembler's front posts no longer lose their tops with Sodium: the posts, belt rails and the base's band and posts drew from pixels next to see-through ones on IE's texture, so mipmaps blended holes in.
- The corners of the Ammunition Assembler's base no longer flicker: the side and end walls each had a post on the same corner.

## 0.5.0-beta (2026-11-26)

Louder Ammunition Assembler sounds. The mod is now also available for Minecraft 1.21.1 (NeoForge), as its own jar.

### Changed

- The Ammunition Assembler's sounds are 10% louder.

## 0.4.0-beta (2026-11-26)

Support for the Immersive Armorer gun pack: all its ammo is now made in IE's machines.

### Added

- **Immersive Armorer support:** ammo for the Immersive Armorer gun pack, made the IE way. Its .454 Casull, AP rifle and 20mm cased telescoped rounds get casings, projectiles and molds like TaCZ's, on blueprints of their own (Immersive Armorer Molds and Projectiles), and are finished in the Ammunition Assembler. Its other ammo comes from IE's machines: cut steel rods from steel rods in the Metal Press (rod mold), the burst, stream and high power capacitor cells on a new Energy Cells blueprint, and flammable chemical from glass bottles filled with creosote, ethanol or biodiesel in the Bottling Machine. Without the gun pack, all of this is hidden and its recipes switch off.
- The icon pack covers the Immersive Armorer ammo too, with labels in the inventory and flat models in the world.

## 0.3.0-beta (2026-11-25)

Ammunition now comes only from the Ammunition Assembler: TaCZ's ammo recipes and Immersive Engineering's guns are switched off (both configurable), the Gunsmith villager sells this mod's blueprints, and there's a new logo.

### Added

- **The Gunsmith villager sells our blueprints:** a casing mold or projectile blueprint for one gun type per level (pistol and shotgun at Apprentice, rifle at Journeyman, heavy at Expert, grenade at Master), and buys brass ingots and gunpowder (Novice) and lead ingots (Journeyman). With IE's guns removed, its bullet, blueprint and revolver part trades are gone; the wooden grip stays.

### Changed

- **Ammunition now comes from the Ammunition Assembler:** the gun smith table's ammo recipes are removed (TaCZ's default gun pack and any other gun pack). Config: `remove_tacz_ammo_recipes`.
- **Immersive Engineering's guns are removed:** the revolver, railgun, chemthrower and both turrets can no longer be crafted, along with IE's bullets, empty casings and shells, the revolver parts, both bullet blueprints (regular and special) and the casing mold, and the upgrades only these guns use (revolver bayonet, magazine and electro, railgun scope and capacitors, chemthrower focus and multitank). They're hidden from JEI and IE's creative tab, and no longer turn up in chest loot or the Gunsmith villager's trades; any already in a world keep working. Shared parts stay (wooden grip, iron and steel rods, graphite electrodes). Config: `remove_ie_weapons`.
- **New mod logo:** a 5.56 round in the badge frame the Immersive addons share, as pixel art. The frame is Immersive Farming's (MIT licensed, credited in NOTICE.md). An unused copy of the old logo is gone from the jar.
- The mod jar now includes LICENSE and NOTICE.md.

## 0.2.0-beta (2026-11-25)

Immersive Munitions now steps aside for Create and Immersive Geology, and the unused brass parts are gone.

### Added

- **Create compatibility:** with Create installed, our zinc (ore, raw zinc, ingot, nugget and blocks) and the brass items Create also makes (ingot, nugget, block and plate) are hidden from the creative tab and JEI, and their recipes switch off, so Create's are used instead. Zinc dust stays, since Create has none, and smelts into Create's zinc ingot. Our zinc ore stops generating too (config: `disable_when_create_present`).
- **Immersive Geology:** with Immersive Geology installed, all our zinc items (including zinc dust) are now hidden the same way, alongside the zinc ore that already stepped aside for it.
- IE's machines now give Create's or Immersive Geology's brass and zinc, not ours, when those mods are installed.

### Changed

- **Removed the brass gear, rod, wire and dust.** Nothing in the mod used them. Any already in a world disappear when it loads (Forge warns about missing items). Zinc dust stays, for IE's ore doubling.

### Fixed

- The credits in the mod list now name the right sources: the zinc and brass textures are recolored from Immersive Engineering's art, not another mod's.

## 0.1.0-beta (2026-11-25)

The first beta. The Ammunition Assembler has been rebuilt to look and work like a real progressive ammunition press, and it now has sounds. Version numbers now follow [Semantic Versioning](https://semver.org): new features raise the middle number, fixes raise the last, and 1.0.0 will be the full release.

- **New look.** The Ammunition Assembler now matches IE's Automated Engineer's Workbench: the same base with its wood band and redstone panel, a ribbed silver housing and roof, flush roof ports, and grey conveyor trim. Its ends are open, under a short ribbed header. The red status lamp is gone.
- **New stations, modelled on real reloading tooling:**
  - **Powder:** the handwheel and a glass sight tube that shows the gunpowder while there is some, then a tapered steel nozzle with a brass tip. The powder falls straight into each casing as it passes, timed to the belt, with a puff of smoke.
  - **Projectiles:** a glass feed tube from the roof shows the hopper's projectiles stacked inside. It feeds a bullet feed die whose four spring fingers grip each projectile by its sides, opening wider for big rounds like the 12 Gauge slug and 40mm. After each placement the fingers are empty for a moment, then the next projectile drops in from the die, and the stack in the tube drops one place. The reload speeds up to keep pace when casings are close together.
  - **Press:** a round seating die under the press crosshead. It stops exactly on top of each round, so short and tall rounds are both met cleanly.
- **New structure.** The Ammunition Assembler has a new block layout (see the Engineer's Manual or JEI), and it's now formed by hitting the **Redstone Engineering Block** with the Engineer's Hammer, instead of the middle conveyor. Existing machines keep working, but break into the new layout's blocks.
- **Sounds:** a quiet running hum, plus sounds for the powder pour, the projectile drop and the press, each timed to its station and with subtitles. They're the mod's own sound events, so resource packs can replace them.


## 0.0.9-alpha (2026-11-25)

Projectiles are now assembled like real bullets, and the 40mm grenade is closer to the real thing. JEI shows everything.

- **Projectiles are made on IE's Engineer's Workbench** (or the Automated Engineer's Workbench, for factories), from new blueprints. Instead of pressing lead, each bullet is a lead core in a copper jacket:
  - small pistol calibers: copper plate + lead wire → 2
  - .50 AE and .500 Magnum: copper plate + 2 lead wire → 3
  - rifles: copper plate + lead wire → 1
  - .338 Lapua and .50 BMG: copper plate + 2 lead wire → 1
  - 12 Gauge: lead wire → 1 (bare lead shot)
  - 40mm: steel plate + copper plate + Iron Mechanical Component + gunpowder → 1 warhead (steel body, copper shaped-charge liner, fuze and high explosive)

  Lead wire and copper plates are IE's own, made in the metal press. Each projectile uses the same amount of lead as before.
- **Five projectile blueprints:** Pistol, Rifle, Heavy, Shotgun and Grenade Projectiles. They're crafted like the mold blueprints, but with **copper plates** instead of brass on the top row.
- The molds now press **casings only**.
- The **40mm grenade casing is aluminium**, as on real 40mm grenade cartridges. It's pressed from an aluminium ingot and has a new silver texture, on the empty casing and on the finished round's case. In a cartridge, the Ammunition Assembler's gunpowder is the propellant; the grenade's explosive is in its warhead.
- **JEI support:** with JEI installed, the Ammunition Assembler has its own JEI tab showing every caliber's recipe, with its energy and time. The press, workbench and blueprint recipes show in IE's and JEI's own tabs. JEI is optional.

## 0.0.8-alpha (2026-11-25)

Projectiles can now be made, which completes the ammo factory: press the casings and projectiles, then put them through the Ammunition Assembler.

- Each caliber's mold now makes both parts of the cartridge in IE's metal press:
  - brass ingot → casings
  - lead ingot → projectiles
  - steel ingot → 40mm grenade warhead (its explosive filling is the gunpowder the Ammunition Assembler adds)

  Projectiles come in the same numbers as casings, so one ingot of each makes a matching set.
- Molds are renamed after their caliber, e.g. "9mm Mold" instead of "9mm Casing Mold".
- The mold blueprints are now **Pistol Molds**, **Rifle Molds**, **Heavy Molds** (.338 Lapua, .50 BMG), **Shotgun Molds** (12 Gauge) and **Grenade Molds** (40mm). The top row of their crafting recipe tells them apart:
  - Pistol Molds: one brass plate in the middle
  - Rifle Molds: two brass plates at the sides
  - Heavy Molds: three brass plates across
  - Shotgun Molds: one brass plate in a corner
  - Grenade Molds: two brass plates side by side

  Blueprints already crafted keep working. Heavy Molds no longer lists 12 Gauge or 40mm; craft the new blueprints for those.

## 0.0.7-alpha (2026-11-25)

New zinc and brass textures, and fixes for the casing molds.

- New textures for every zinc and brass item and block, based on IE's own metal art in this mod's colours: the ingots, nuggets, raw zinc, dusts, plate, rod, wire, gear and storage blocks. The block of brass now has bands like IE's metal blocks.
- New zinc ore, made from IE's nickel ore in blue-grey zinc. It draws Minecraft's own stone and deepslate underneath, so it matches vanilla and any resource pack that changes stone.
- Removed all textures that came from Scorched Guns Brass.
- Fixed a crash when taking the Casing Molds blueprint out of IE's Engineer's Workbench. The workbench only fits about 12 recipes per blueprint, so the casing molds are now on three blueprints:
  - **Pistol Casing Molds**: brass plate in the middle of the top row
  - **Rifle Casing Molds**: brass plates at both ends of the top row
  - **Shotgun & Heavy Casing Molds**: brass plates across the whole top row

  The old Casing Molds blueprint no longer makes anything; craft the new ones.
- Casing molds now press **brass ingots** instead of brass rods, like IE's copper ingot to casings. The number of casings per press is unchanged.

## 0.0.6-alpha (2026-11-24)

Casings can now be made in IE's metal press.

- Press a brass rod in a casing mold to get that caliber's empty casings:
  - 4 per rod: small pistol calibers
  - 3 per rod: .50 AE and .500 Magnum
  - 2 per rod: rifles and 12 Gauge
  - 1 per rod: .338 Lapua, .50 BMG and 40mm
- Casing molds are made on IE's Engineer's Workbench from 3 steel plates and a wire cutter, like IE's own molds, using the new **Casing Molds** blueprint. Craft the blueprint like IE's Molds blueprint, but with a brass plate on top.
- Casings, projectiles and casing molds now use TaCZ's caliber names, e.g. "Empty 5.56x45mm Casing" and ".30-06 Springfield Projectile". Searching a caliber now finds TaCZ's round and ours together.
- 12 Gauge shells are now all brass, both the empty casing and the finished round in the icon pack. The red art is kept in `art/` as a backup.
- Removed the 8 category molds (pistol, SMG, rifle and so on). They were only creative tab icons and had no use. Any left in an existing world disappear on load.

## 0.0.5-alpha (2026-11-24)

The Ammunition Assembler gets a GUI.

- Right-click either of the machine's front screens to open it. It's styled like IE's machines (remixed from IE's GUI art).
- It shows the machine's stored energy on IE's gauge. Hover for the exact amount, out of 16,000.
- The gunpowder and projectile hoppers are two slots: put items in, take them out, or shift-click. Each slot only takes its own item, and an empty slot names what goes in it.
- When the belt is jammed, a red line says what it's waiting for, e.g. "Needs: 9MM Projectile" when the wrong projectiles are loaded.
- **Clear Belt** takes every casing that hasn't reached the press off the belt. You get back each casing with its gunpowder and projectile, if it had them yet. They go into your inventory, or drop at your feet when it's full. Rounds already at the press finish as normal.

## 0.0.4-alpha (2026-11-24)

The Ammunition Assembler gets its finished model and animations.

- New IE-style model (built in Blender, `art/assembler/`): steel panels with treated-wood bands, IE ports, machined station heads, and a plain back.
- The belt runs smoothly on the client between syncs, and casings no longer jump back or flicker (ghosting).
- Animated stations:
  - The gunpowder head drops and pours a quick stream of grains with a small puff of smoke into each casing.
  - The feeder's claws hold the projectile from its hopper and seat it on the casing.
  - The press comes down and the finished round appears after it.
- 12g shells show no projectile on the casing between the feeder and the press. Any recipe can do this with `"hide_projectile": true`.
- Two front screens show what each hopper holds: the item and its count, or EMPTY.
- The red power lamp on the front is lit while the machine has energy stored.
- Hopper intakes moved to the front of the roof, right above their screens.
- Power now goes in through IE's power port on top of the roof, at the back over the press, like IE's machines. Stand a wire connector on it. Machines built in 0.0.3 need their hoppers and power connector moved.
- Faster belt that keeps up with a hopper: 60-tick recipes, up to 8 casings at once, 1200 FE per round.
- Caliber labels on casings, projectiles and molds now show only in inventories. In the world and on the belt they are plain.

## 0.0.3-alpha (2026-11-23)

- New multiblock: the **Ammunition Assembler** (3 long, 3 tall, 2 deep). Casings ride a belt, get filled with gunpowder under the hopper, then a projectile is pressed in under the piston, and the finished TaCZ round comes out the far end. Runs on IE power, with a redstone off switch. One recipe per caliber (`data/immersive_munitions/recipes/ammo_assembly/`). The model and belt animation are still placeholders.
- Fixed: dropped, held and conveyed TaCZ rounds showed a scrambled texture when the bundled icon pack was on (the pack was replacing TaCZ's 3D model skins with flat icons).
- With the icon pack on, TaCZ rounds in the world (dropped, held, on conveyors) now show this mod's round art as a normal flat item, like the casings. With the pack off, TaCZ's own 3D ammo model is used. Tracers, ejected casings and hit effects are unchanged either way.

## 0.0.2-alpha (2026-11-23)

- New caliber labels: a plain 32x9 black bar in the top-left corner with the caliber in a 4x7 pixel font. The text color shows the weapon category: green pistol, blue SMG, orange rifle, red sniper, purple shotgun, olive launcher. The label design (font and colors) is original to this mod. Sprite backgrounds stay transparent.
- Replaced every label copied from TaCZ (rounds, casings, projectiles, molds and the bundled icon pack). The mod no longer contains any TaCZ art.
- New RPG rocket icon (`rpg_rocket`) in the bundled TaCZ icon pack, with a matching `RPG-7` label.
- Art pipeline: sprites in `art/` are now stored unlabeled, and `java art/labels/Labels.java` regenerates all labeled textures.

## 0.0.1-alpha (2026-11-23)

New alpha base, built from the 0.2.19 source ("remove spent shells, revert tab icon").

- Repo restructured: the Gradle project is now at the repo root and the art moved to `art/`.
- Dev environment: Immersive Engineering and TaCZ now load inside `runClient` / `runServer` (deobfuscated from `libs/`), and the mixin refmap settings are in place for TaCZ.

## 0.2.19 and earlier

Pre-alpha builds. From the 0.2.x README:

- Four left-side creative inventory categories: Brass & Zinc, Molds, Shells, and Projectiles.
- Finished-round items are no longer registered by Immersive Munitions; the assembly processor outputs TaCZ's native ammunition stacks.
- Optional bundled resource pack, **Immersive Munitions — TaCZ Ammunition Icons**, that overrides TaCZ ammo slot/UV textures.
- Dedicated icon-only items for the creative tab and side selectors so caliber labels aren't drawn on tab icons.
- Spent shells removed (0.2.19).
