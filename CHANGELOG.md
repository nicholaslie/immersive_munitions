# Changelog

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
