# Changelog

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
