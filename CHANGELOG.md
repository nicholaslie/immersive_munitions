# Changelog

## 0.0.3-alpha (in development)

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
